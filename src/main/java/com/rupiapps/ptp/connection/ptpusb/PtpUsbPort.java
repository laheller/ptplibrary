package com.rupiapps.ptp.connection.ptpusb;

import com.rupiapps.ptp.PtpLog;
import com.rupiapps.ptp.exceptions.InitFailException;
import com.rupiapps.ptp.exceptions.ReceiveDataException;
import com.rupiapps.ptp.exceptions.SendDataException;
import com.rupiapps.ptp.packets.Packet;
import com.rupiapps.ptp.datacallbacks.DataCallback;

import java.io.PrintWriter;
import java.io.StringWriter;

public class PtpUsbPort
{
	private PtpUsbEndpoints endpoints;
	private int maxin, maxout;
	private byte[] readbuffer, writebuffer, eventbuffer;
	private boolean initialized;
	private Thread eventThread;
	
	public PtpUsbPort(PtpUsbEndpoints endpoints)
	{
		this.endpoints = endpoints;
		initialized = false;
	}
	
	public boolean isInitialized()
	{
		return initialized;		
	}
	
	public void sendPacket(Packet p, int timeout) throws SendDataException
	{
//		if(BuildConfig.DEBUG)
//			PtpLog.debug("sendpacket: "+p);
		endpoints.setTimeOut(timeout);
		byte[] buffer = p.getBuffer();
		if(buffer.length<=maxout)
			endpoints.writeDataOut(buffer, buffer.length);
		else
		{
			int offset = 0;
			while(buffer.length-offset>maxout)
			{
				System.arraycopy(buffer, offset, writebuffer, 0, maxout);
				endpoints.writeDataOut(writebuffer, maxout);
				offset+=maxout;
			}
			System.arraycopy(buffer, offset, writebuffer, 0, buffer.length-offset);
			endpoints.writeDataOut(writebuffer, buffer.length-offset);
		}		
	}
	
	public Packet receivePacket(boolean withheader, int timeout) throws ReceiveDataException
	{
		endpoints.setTimeOut(timeout);
		int received = 0;
		int len;
		len=endpoints.readDataIn(readbuffer);		
		received+=len;

//		if(BuildConfig.DEBUG)
//			PtpLog.debug(Packet.getHexString(readbuffer));
		
		
		int size = (0x000000ff & readbuffer[3])<<24 | 
				   (0x000000ff & readbuffer[2])<<16 | 
				   (0x000000ff & readbuffer[1])<<8  | 
				   (0x000000ff & readbuffer[0]);

		if(size<=0)
			return new Packet(new byte[0]);
		byte[] packet = null;
		try
		{
			packet = new byte[size-(withheader?0:12)];
		}
		catch(OutOfMemoryError e)
		{
			throw new ReceiveDataException(e.toString());
		}
		int offset = 0;
		if(!withheader)
		{
			byte[] header = new byte[12];
			System.arraycopy(readbuffer, 0, header, 0, 12);
			
//			PtpLog.debug("receivepacket header typ is: "+PtpConnection_Usb.getPacketType(new Packet(header, false)));
//            PtpLog.debug(new Packet(header, false));
		}
		
		System.arraycopy(readbuffer, (withheader?0:12), packet, offset, len-(withheader?0:12));
		offset+=len-(withheader?0:12);

		while(received<size)
		{
			len=endpoints.readDataIn(readbuffer);
			received+=len;
			
			System.arraycopy(readbuffer, 0, packet, offset, len);
			offset+=len;			
		}
		
		return new Packet(packet);
	}
	
	public void receiveData(DataCallback cb, int timeout) throws ReceiveDataException
	{
		if(cb==null)
			return;
		endpoints.setTimeOut(timeout);
		
		long received = 0;
		int len;
		len=endpoints.readDataIn(readbuffer);
		received+=len-12;
		
		long totalsize = (0x000000ff & readbuffer[3])<<24 |
						(0x000000ff & readbuffer[2])<<16 | 
						(0x000000ff & readbuffer[1])<<8  | 
						(0x000000ff & readbuffer[0]);
		totalsize-=12;
		

		try
		{
			cb.receivedDataPacket(0, totalsize, received, readbuffer, 12, len-12);
		}
		catch(OutOfMemoryError e)
		{
			throw e; //rethrow to be handled outside
		}
		catch(Exception e)
		{
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);	
			throw new ReceiveDataException("Exception in Callback:"+sw.toString());
		}
		
		
		while(received<totalsize)
		{
			len=endpoints.readDataIn(readbuffer);
			received+=len;
			

			try
			{
				cb.receivedDataPacket(0, totalsize, received, readbuffer, 0, len);
			}
			catch(OutOfMemoryError e)
			{
				throw e; //rethrow to be handled outside
			}
			catch(Exception e)
			{
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				e.printStackTrace(pw);
				throw new ReceiveDataException("Exception in Callback:"+sw.toString());
			}

		}	
		//PtpLog.debug("received "+received+ " total "+totalsize);

	}
	

	public void receiveEvent()
	{		
		endpoints.readEvent(eventbuffer, true);
		//PtpLog.debug("read event: "+len);
	}
	
//	public void cancelRequest(int transactionid)
//	{
//		int len = endpoints.controlTransfer(0x21, 0x64, 0, 0,
//				new CancelPacket(transactionid).getBuffer());
//		PtpLog.debug("cancelrequest: "+len);
//	}
	
	public short getDeviceStatus()
	{
		byte[] buffer = new byte[32];
		int len = endpoints.controlTransfer(0xa1, 0x67, 0, 0, buffer);
		
		PtpLog.debug("getDeviceStatus: "+len);
		Packet p = new Packet(buffer);
		
		short status = p.getShortL(2);
		PtpLog.debug("status is "+Packet.getHexString(status));
				
		
		return status;	
	}
	
	public void resetDevice()
	{
		int len = endpoints.controlTransfer(0x21, 0x66, 0, 0, null);
		PtpLog.debug("resetDevice: "+len);
	}
	
	public void getExtendedEventData()
	{
		byte[] buffer = new byte[30];
		int len = endpoints.controlTransfer(0xa1, 0x65, 0, 0, buffer);
		PtpLog.debug("getExtendedEventData: "+len);	
		if(len>0)
		{
			Packet p = new Packet(buffer);
			short eventcode = p.getShortL(0);
			PtpLog.debug("eventcode is "+Packet.getHexString(eventcode));
			PtpLog.debug("transid is "+p.getIntL(2));					
		}				
	}
	
	public void initializeInterface() throws InitFailException
	{
		if(!initialized)
		{
			endpoints.initalize();
			maxin = endpoints.getMaxPacketSizeIn();
			maxout = endpoints.getMaxPacketSizeOut();
			readbuffer = new byte[maxin];
			if(maxin==maxout)
				writebuffer = readbuffer;
			else
				writebuffer = new byte[maxout];
			initialized = true;
			eventbuffer = new byte[endpoints.getMaxPacketSizeInterrupt()];
			
			eventThread = new Thread
			(	()->
				{
					while(!Thread.interrupted())
					{
						endpoints.readEvent(eventbuffer, false);
						try
                        {
	                        Thread.sleep(5000);
                        }
                        catch (InterruptedException e)
                        {
                        }
					}					
				}
			);
//			eventThread.start();

		}
	}
	
	public void releaseInterface()
	{
		if(initialized)
		{
			eventThread.interrupt();			
			endpoints.release();
			initialized = false;
			readbuffer = null;
			writebuffer = null;
			eventbuffer = null;
		}
	}	


//	class CancelPacket extends Packet
//	{
//		CancelPacket(int transactionid)
//		{
//			super(6);
//			setShortL(0, PtpEC.CancelTransaction);
//			setIntL(2, transactionid);
//		}
//
//	}
	
}
