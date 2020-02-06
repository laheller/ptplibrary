package com.rupiapps.ptp.connection.ptpip;

import com.rupiapps.ptp.PtpLog;
import com.rupiapps.ptp.connection.PtpConnection;
import com.rupiapps.ptp.connection.ptpip.PtpSocket.ReadStreamCallback;
import com.rupiapps.ptp.exceptions.InitFailException;
import com.rupiapps.ptp.exceptions.InitFailException.InitFailReason;
import com.rupiapps.ptp.exceptions.ReceiveDataException;
import com.rupiapps.ptp.exceptions.ResponseNotOkException;
import com.rupiapps.ptp.exceptions.SendDataException;
import com.rupiapps.ptp.packets.Packet;
import com.rupiapps.ptp.datacallbacks.DataCallback;
import com.rupiapps.ptp.constants.PtpRc;

public class PtpConnection_Ip implements PtpConnection
{
	private PtpSocket con;
	private String deviceName;


	public PtpConnection_Ip(PtpSocket con)
	{		
		this.con = con;
		deviceName = "";
	}

	private static int getPacketType(Packet p)
	{
		if(p==null || p.getBufferSize()<8)
			return PacketType.Empty;
		return p.getIntL(4);
	}

	private static short getResponseCode(Packet p)
	{
		if(p==null || p.getBufferSize()<10)
			return PtpRc.General_Error;
		return p.getShortL(8);
	}

	@Override
	public void sendRequest(short reqcode, int tid, int[] param, int timeout) throws ResponseNotOkException, ReceiveDataException, SendDataException
	{
		con.sendDataPacket(new OperationReqPacket(reqcode, tid, param!=null?param:new int[]{}));
		Packet response = con.receivePtpPacket(timeout);
		if(PtpConnection_Ip.getPacketType(response)==PacketType.OperationResp)
		{
			if(getResponseCode(response)!= PtpRc.OK)
				throw new ResponseNotOkException(getResponseCode(response));
		}
	}

//	@Override
//	public Packet requestPacket(short reqcode, int tid, int[] param, int timeout) throws ReceiveDataException, ResponseNotOkException, SendDataException
//	{
//		CreatePacketDataCallback packetCallback = new CreatePacketDataCallback();
//
//		requestData(packetCallback, reqcode, tid, param, timeout);
//
//		return packetCallback.getPacket();
//	}

	@Override
	public void requestData(DataCallback cb, short reqcode, int tid, int[] param, int timeout) throws ReceiveDataException, ResponseNotOkException, SendDataException
	{
		con.sendDataPacket(new OperationReqPacket(reqcode, tid, param!=null?param:new int[]{}));

		getDataPackets(cb, timeout);

		Packet response = con.receivePtpPacket(timeout);
		if(PtpConnection_Ip.getPacketType(response)==PacketType.OperationResp)
		{
			if(getResponseCode(response)!= PtpRc.OK)
				throw new ResponseNotOkException(getResponseCode(response));
		}
	}

	private void getDataPackets(final DataCallback cb, int timeout) throws ReceiveDataException, ResponseNotOkException
	{
		Packet response = con.receivePtpPacket(timeout);

		if(PtpConnection_Ip.getPacketType(response)==PacketType.StartData
		|| PtpConnection_Ip.getPacketType(response)==PacketType.Data)
		{
			final long totaldatasize = response.getLongL(12);
			final long[] receiveddatasize = new long[]{0};


			while(true)
			{
				Packet header = con.receivePtpHeader(true);

				if(getPacketType(header)==PacketType.Data
				|| getPacketType(header)==PacketType.EndData)
				{
					final int datalength = header.getIntL(0)-12;
					final int transid = header.getIntL(8);

					con.receiveDataStream(datalength, new ReadStreamCallback()
					{
						@Override
						public void receivedBytes(byte[] buffer, int len) throws ReceiveDataException
						{
							receiveddatasize[0]+=len;
							try
							{
								cb.receivedDataPacket(transid, totaldatasize, receiveddatasize[0], buffer, 0, len);
							}
							catch(OutOfMemoryError e)
							{
								throw e;
							}
							catch(Exception e)
							{
								throw new ReceiveDataException("Exception in Callback:"+e.getStackTrace());
							}
						}
					});

					if(getPacketType(header)==PacketType.EndData)
						return;
				}
				else
				{
					PtpLog.debug(header);
					throw new ReceiveDataException("wrong packet type");
				}
			}
		}
		else
		{
			if(getPacketType(response)==PacketType.OperationResp)
			{
				PtpLog.debug("responscecode is: "+getResponseCode(response));
				if(getResponseCode(response)!= PtpRc.OK)
					throw new ResponseNotOkException(getResponseCode(response));
			}

			//new Throwable().printStackTrace();
			throw new ReceiveDataException("wrong packet type: "+getPacketType(response));
		}

	}

	@Override
	public void sendPacket(Packet data, short reqcode, int tid, int[] param, int timeout) throws ResponseNotOkException, ReceiveDataException, SendDataException
	{
		con.sendDataPacket(new OperationReqPacket(reqcode, tid, param!=null?param:new int[]{}, true));

		con.sendDataPacket(new StartDataPacket(tid, data.getBufferSize()));
		con.sendDataPacket(new DataPacket(true, tid, data.getBuffer()));

		Packet response = con.receivePtpPacket(timeout);
		if(PtpConnection_Ip.getPacketType(response)==PacketType.OperationResp)
		{
			if(getResponseCode(response)!= PtpRc.OK)
				throw new ResponseNotOkException(getResponseCode(response));
		}
	}

	@Override
	public String getDeviceName()
	{
		return deviceName;
	}

	@Override
	public void connectAndInit(byte[] guid, String appname, Runnable waitForAck) throws InitFailException
	{
		//		if(true)//test
		//		throw new InitFailException(InitFailReason.Disconnected);

		PtpLog.debug ("try to connect "+con.getHost().toString());
		con.connectDataLine();


		try
		{
			con.sendDataPacket(new InitCmdReqPacket(guid, appname));
		}
		catch (SendDataException e)
		{
			e.printStackTrace();
			//	        con.disconnect();
			throw new InitFailException(InitFailReason.Disconnected);
		}

		Packet response;
		try
		{
			if(waitForAck!=null)
				waitForAck.run();
			response = con.receivePtpPacket(30000);
		}
		catch (ReceiveDataException e)
		{
			//        	con.disconnect();
			throw new InitFailException(InitFailReason.Timeout);
		}


		//		sessionid = -1;
		if(getPacketType(response)==PacketType.InitCmdAck)
		{
			InitCmdAckPacket ackPacket = new InitCmdAckPacket(response);

			int ptpsessionid = ackPacket.getSessionId();
			PtpLog.debug("sessionid: "+ptpsessionid);
			PtpLog.debug("guid: "+Packet.getHexString(ackPacket.getGuid()));
			deviceName = ackPacket.getCameraName();
			PtpLog.debug("devicename: "+deviceName);

			con.connectEventLine();
			try
			{
				con.sendEventPacket(new InitEventReqPacket(ptpsessionid));
			}
			catch (SendDataException e)
			{
				e.printStackTrace();
				//		        con.disconnect();
				throw new InitFailException(InitFailReason.Disconnected);
			}

			Packet eventResponse;
			try
			{
				eventResponse = con.receiveEventPacket();
			}
			catch (ReceiveDataException e)
			{
				//	        	con.disconnect();
				throw new InitFailException(InitFailReason.Timeout);
			}


			if(getPacketType(eventResponse)==PacketType.InitEventAck)
			{
				PtpLog.debug("event connection ack");
				//				con.disconnectEventLine();
			}
			else if(getPacketType(eventResponse)==PacketType.InitFail)
			{
				//				con.disconnect();
				throw new InitFailException(InitFailReason.NotAcknowledged);
			}
		}
		else
		{
			//			con.disconnect();
			throw new InitFailException(InitFailReason.NotAcknowledged);
		}

	}

	public boolean isConnected()
	{
		return con.isDataConnected() && con.isEventConnected();
	}

	@Override
	public void disconnect()
	{
		if(con.isDataConnected())
			con.disconnect();
	}

	
	class PacketType
	{
		final static short Empty = 0;
		final static short InitCmdReq = 1;
		final static short InitCmdAck = 2;
		final static short InitEventReq = 3;
		final static short InitEventAck = 4;
		final static short InitFail = 5;
		final static short OperationReq = 6;
		final static short OperationResp = 7;
		final static short Event = 8;
		final static short StartData = 9;
		final static short Data = 10;
		final static short CancelData = 11;
		final static short EndData = 12;
		final static short Ping = 13;
		final static short Pong = 14;
	}

	class OperationReqPacket extends Packet
	{
		public OperationReqPacket(short opCode, int transactionid, int[] params, boolean dataout)
		{
			super(18 + (params!=null?params.length*4:0));

			setIntL(0, 18 + (params!=null?params.length*4:0));
			setIntL(4, PacketType.OperationReq);
			setIntL(8, dataout?2:1); //set standard data phase; 2=data-out
			setShortL(12, opCode);
			setIntL(14, transactionid);

			for(int i=0; params!=null && i<params.length; i++)
				setIntL(18+i*4, params[i]);
		}

		public OperationReqPacket(short opCode, int transactionid, int[] params)
		{
			this(opCode, transactionid, params, false);
		}
	}

	class InitCmdAckPacket extends Packet
	{
		InitCmdAckPacket(Packet p)
		{
			super(p.getBuffer());
		}

		int getSessionId()
		{
			return getIntL(8);
		}

		byte[] getGuid()
		{
			return getBytes(12, 16);
		}

		String getCameraName()
		{
			return getString(28);
		}

	}

	class InitCmdReqPacket extends Packet
	{
		InitCmdReqPacket(byte[] guid, String name)
		{
			super(8+16+strLen(name)+4);
			setIntL(0, 8+16+strLen(name)+4);
			setIntL(4, PacketType.InitCmdReq);

			setBytes(8, guid);

			//		setLong(8, guid1, ByteOrder.LittleEndian);
			//		setLong(16, guid2, ByteOrder.LittleEndian);

			setString(24, name);
			int offset = 24+strLen(name);

			setShortL(offset, (short)0x0000);
			setShortL(offset+2, (short)0x0001);
		}
	}

	class InitEventReqPacket extends Packet
	{
		InitEventReqPacket(int sessionid)
		{
			super(12);
			setIntL(0, 12);
			setIntL(4, PacketType.InitEventReq);
			setIntL(8, sessionid);
		}
	}

	class StartDataPacket extends Packet
	{
		StartDataPacket(Packet p)
		{
			super(p.getBuffer());
		}

		StartDataPacket(int tid, long datalength)
		{
			super(20);
			setIntL(0, 20);
			setIntL(4, PacketType.StartData);
			setIntL(8, tid);
			setLongL(12, datalength);
		}

		int getTransactionId()
		{
			return getIntL(8);
		}

		long getDataLength()
		{
			return getLongL(12);
		}
	}

	class DataPacket extends Packet
	{
		private int datalength;

		DataPacket(Packet p)
		{
			super(0);
			setBuffer(p.getBuffer());
			datalength = getIntL(0)-12;
		}

		DataPacket(byte[] bytes)
		{
			super(bytes);
			datalength = getIntL(0)-12;
		}

		DataPacket(boolean end, int tid, byte[] data)
		{
			super(12+data.length);
			setIntL(0, data.length+12);
			setIntL(4, end? PacketType.EndData:PacketType.Data);
			setIntL(8, tid);
			setBytes(12, data);
		}

		public int getTransactionId()
		{
			return getIntL(8);
		}

		public int getDataLength()
		{
			return datalength;
		}

		public int getDataOffset()
		{
			return 12;
		}
	}

	class PingPacket extends Packet
	{
		PingPacket()
		{
			super(8);
			setIntL(0, 8);
			setIntL(4, PacketType.Ping);
		}
	}


}
