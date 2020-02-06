package com.rupiapps.ptp.datacallbacks;

import java.io.IOException;
import java.io.OutputStream;




public class WriteToStreamDataCallback implements DataCallback
{			
	private OutputStream out;
	
	
	public WriteToStreamDataCallback(OutputStream out)
	{
		this.out = out;		
		
	}
		

	@Override
	public void receivedDataPacket(int transactionid, long totaldatasize, long cumulateddatasize, byte[] data, int offset, int length)
	{	
		try
        {
            out.write(data, offset, length);
        }
        catch (IOException e)
        {	                   
            e.printStackTrace();
        }
	}

}
