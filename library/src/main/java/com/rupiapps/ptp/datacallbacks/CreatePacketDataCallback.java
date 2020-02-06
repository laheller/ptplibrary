package com.rupiapps.ptp.datacallbacks;

import com.rupiapps.ptp.packets.Packet;

public class CreatePacketDataCallback implements DataCallback
{
	private byte[] buffer=null;
	private int offset;
	private Packet p;

	@Override
	public void receivedDataPacket(int transactionid, long totaldatasize,
	        long cumulateddatasize, byte[] data, int dataoffset, int len)
	{
		if(buffer==null)
		{
			buffer = new byte[(int)totaldatasize];
			offset = 0;
		}

		System.arraycopy(data, dataoffset, buffer, offset, len);
//		for(int i=0; i<len; i++)
//		{
//			buffer[offset+i] = data[dataoffset+i];
//		}

		offset+=len;
	}
	
	public Packet getPacket()
	{
		if(buffer!=null)
		{
			p = new Packet(buffer);
			buffer = null;
		}		
		return p;
	}
	

}
