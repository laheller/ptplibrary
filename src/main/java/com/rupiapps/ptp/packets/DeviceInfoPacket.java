package com.rupiapps.ptp.packets;


import com.rupiapps.ptp.packets.Packet;

public class DeviceInfoPacket extends Packet
{		
	public DeviceInfoPacket(Packet p)
    {
		super(p.getBuffer());
    }
	
	public short getStandardVersion()
	{
		return getShortL(0);
	}
	
	public int getVendorExtensionId()
	{
		return getIntL(2);
	}
	
	public short getVendorExtensionVersion()
	{
		return getShortL(6);
	}
	
	public String getVendorExtensionDesc()
	{
		int offset = 8;
		int len = getByte(offset);
		if(len==0)
			return "";
		return getString(offset+1);
	}
	
	private int getFMOffset()
	{
		int offset = 8;
		int len = getByte(offset);
		return offset+len*2+1;
	}
	
	public short getFunctionalMode()
	{
		return getShortL(getFMOffset());
	}
	
	private int getOpOffset()
	{
		return getFMOffset()+2;		
	}
	
	public short[] getOperationsSupported()
	{
		if(getBufferSize()<20)
			return new short[0];
		
		int offset = getOpOffset();
		int len = getIntL(offset);
		offset += 4;
		short[] ops = new short[len];
		for(int i=0; i<len; i++)
		{
			ops[i] = getShortL(offset);
			offset+=2;
		}
		return ops;
	}
	
	private int getEvOffset()
	{
		int opoffset = getOpOffset();
		int len =  getIntL(opoffset);
		return opoffset +4 +len*2;
	}
	
	public short[] getEventsSupported()
	{
		int offset = getEvOffset();
		int len = getIntL(offset);
		offset += 4;
		short[] ops = new short[len];
		for(int i=0; i<len; i++)
		{
			ops[i] = getShortL(offset);
			offset+=2;
		}
		return ops;
	}
	
	private int getDevPropOffset()
	{
		int offset = getEvOffset();
		int len =  getIntL(offset);
		return offset +4 +len*2;
	}

	public short[] getDevicePropertiesSupported()
	{
		if(getBufferSize()<14)
			return new short[0];
		int offset = getDevPropOffset();
		int len = getIntL(offset);
		offset += 4;
		short[] ops = new short[len];
		for(int i=0; i<len; i++)
		{
			ops[i] = getShortL(offset);
			offset+=2;
		}
		return ops;
	}
	
	private int getCFOffset()
	{
		int offset = getDevPropOffset();
		int len =  getIntL(offset);
		return offset +4 +len*2;
	}
	
	public short[] getCaptureFormats()
	{
		int offset = getCFOffset();
		int len = getIntL(offset);
		offset += 4;
		short[] ops = new short[len];
		for(int i=0; i<len; i++)
		{
			ops[i] = getShortL(offset);
			offset+=2;
		}
		return ops;
	}
	
	private int getIFOffset()
	{
		int offset = getCFOffset();
		int len =  getIntL(offset);
		return offset +4 +len*2;
	}
	
	public short[] getImageFormats()
	{
		int offset = getIFOffset();
		int len = getIntL(offset);
		offset += 4;
		short[] ops = new short[len];
		for(int i=0; i<len; i++)
		{
			ops[i] = getShortL(offset);
			offset+=2;
		}
		return ops;
	}
	
	private int getManuOffset()
	{
		int offset = getIFOffset();
		int len =  getIntL(offset);
		return offset +4 +len*2;
	}
	
	public String getManufacturer()
	{
		int offset = getManuOffset();
		int len = getByte(offset);
		if(len==0)
			return "";
		return getString(offset+1);
	}
	
	private int getModelOffset()
	{
		int offset = getManuOffset();
		int len = getByte(offset);
		return offset+len*2+1;
	}
	
	public String getModel()
	{
		int offset = getModelOffset();
		int len = getByte(offset);
		if(len==0)
			return "";
		return getString(offset+1);
	}
	
	private int getDVOffset()
	{
		int offset = getModelOffset();
		int len = getByte(offset);
		return offset+len*2+1;
	}
	
	public String getDeviceVersion()
	{
		int offset = getDVOffset();
		int len = getByte(offset);
		if(len==0)
			return "";
		return getString(offset+1);
	}
	
	private int getSNOffset()
	{
		int offset = getDVOffset();
		int len = getByte(offset);
		return offset+len*2+1;
	}
	
	public String getSerialNumber()
	{
		int offset = getSNOffset();
		int len = getByte(offset);
		if(len==0)
			return "";
		return getString(offset+1);
	}
	
}
