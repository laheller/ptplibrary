package com.rupiapps.ptp.packets;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class ObjectInfoPacket extends Packet
{
	private static SimpleDateFormat dt = new SimpleDateFormat("yyyyMMdd'T'HHmmss.S", Locale.getDefault());
	private Date captureDate, modDate;
	private String filename, captureDateString;

	public ObjectInfoPacket(Packet p)
    {
		super(p.getBuffer());
    }

    public ObjectInfoPacket(byte[] buffer)
	{
		super(buffer);
	}
	
	public int getStorageId()
	{
		return getIntL(0);		
	}
	
	public short getFormatCode()
	{
		return getShortL(4);
	}
	
	public short getProtectionStatus()
	{
		return getShortL(6);
	}
	
	public long getCompressedSize()
	{
		return getLongL(8, 4);
	}
	
	public short getThumbFormatCode()
	{
		return getShortL(12);
	}
	
	public int getThumbCompressedSize()
	{
		return getIntL(14);
	}
	
	public int getThumbWidth()
	{
		return getIntL(18);
	}
	
	public int getThumbHeight()
	{
		return getIntL(22);
	}
	
	public int getWidth()
	{
		return getIntL(26);
	}
	
	public int getHeight()
	{
		return getIntL(30);
	}
	
	public int getBitDepth()
	{
		return getIntL(34);
	}
	
	public int getParentHandle()
	{
		return getIntL(38);
	}
	
	public short getAssociationType()
	{
		return getShortL(42);		
	}
	
	public int getAssociationDescription()
	{
		return getIntL(44);
	}
	
	public int getSequenceNumber()
	{
		return getIntL(48);
	}
	
	public String getFileName()
	{
		if(filename!=null)
			return filename;

		filename = getString(53);
		return filename;
	}
	
	public Date getCaptureDate()
	{
		if(captureDate!=null)
			return captureDate;

		if(captureDateString==null)
		{
			captureDateString = getString(54 + getByte(52) * 2);
			if(captureDateString.indexOf('.') < 0)
				captureDateString = captureDateString + ".0";
		}
		try
        {
        	captureDate = dt.parse(captureDateString);
	        return captureDate;
        }
        catch (ParseException e)
        {
	        return new Date(0);	        
        }
	}

	public String getCaptureDateString()
	{
		if(captureDateString!=null)
			return captureDateString;

		captureDateString = getString(54 + getByte(52) * 2);
		if(captureDateString.indexOf('.') < 0)
			captureDateString = captureDateString + ".0";
		return captureDateString;
	}
	
	public Date getModificationDate()
	{
		if(modDate!=null)
			return modDate;

		String date_s = getString(55+getByte(52)*2+getByte(53+getByte(52)*2)*2);
		if(date_s.indexOf('.')<0)
			date_s = date_s+".0";
		try
        {
        	modDate = dt.parse(date_s);
	        return modDate;
        }
        catch (ParseException e)
        {
	        return new Date(0);	        
        }
	}

}
