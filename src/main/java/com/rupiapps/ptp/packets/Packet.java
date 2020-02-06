package com.rupiapps.ptp.packets;

import java.util.Arrays;

public class Packet 
{
	private byte[] packet;
	private char[] charbuffer;

	public Packet(byte[] packet)//, boolean cloneit)
	{
//		if(cloneit)
//			this.packet = packet.clone();
//		else
			this.packet = packet;
	}
	
//	public Packet(byte[] packet, int len)
//	{
//		this.packet = Arrays.copyOf(packet, len);
//	}
//
	public Packet(int len)
	{
		if(len>0)
			this.packet = new byte[len];
		init();
	}

	protected void init()
	{
	}

	public byte[] getBuffer()
	{
		return packet;
	}	
	
	protected void setBuffer(byte[] buf)
	{
		packet = buf;		
	}
	
	public int getBufferSize()
	{
		if(packet==null)
			return 0;
		return packet.length;
	}

	public byte getByte(int offset) 
	{
		return packet[offset];
	}
	
	public short getShortL(int offset)
	{
		return (short) ((0x000000ff & packet[offset])
				| (0x000000ff & packet[offset + 1]) << 8);
	}
	
	public short getShortB(int offset)
	{
		return (short) ((0x000000ff & packet[offset + 1]) | (0x000000ff & packet[offset]) << 8);	
	}

	public int getIntL(int offset)
	{
		int value = 0;

		for(int i=0, shift=0; i<4 && offset+i<packet.length; i++, shift+=8)
		{
			value = value | (0x000000ff & packet[offset + i]) << shift;
		}
		return value;
	}

	public int getIntL(int offset, int size)
	{
		int value = 0;

		for(int i=0, shift=0; i<size && offset+i<packet.length; i++, shift+=8)
		{
			value = value | (0x000000ff & packet[offset + i]) << shift;
		}
		return value;
	}
	
	public int getIntB(int offset)
	{
		return    (0x000000ff & packet[offset + 3])
				| (0x000000ff & packet[offset + 2]) << 8
				| (0x000000ff & packet[offset + 1]) << 16
				| (0x000000ff & packet[offset]) << 24;		
	}

	public int getIntB(int offset, int size)
	{
		int value = 0;

		for(int i=0; i<size && offset+i<packet.length; i++)
		{
			value = value << 8;
			value = value | (0x000000ff & packet[offset + i]);
		}
		return value;
	}
	
	public long getLongL(int offset)
	{
		return    (0x000000ffL & packet[offset + 0])
				| (0x000000ffL & packet[offset + 1]) << 8
				| (0x000000ffL & packet[offset + 2]) << 16
				| (0x000000ffL & packet[offset + 3]) << 24
				| (0x000000ffL & packet[offset + 4]) << 32
				| (0x000000ffL & packet[offset + 5]) << 40
				| (0x000000ffL & packet[offset + 6]) << 48
				| (0x000000ffL & packet[offset + 7]) << 56;
	}

	public long getLongL(int offset, int size)
	{
		long value = 0;

		for(int i=0, shift=0; i<size && offset+i<packet.length; i++, shift+=8)
		{
			value = value | ((long)(0x000000ffL & packet[offset + i]))<< shift;
		}
		return value;
	}
	
	public long getLongB(int offset)
	{
		return    (0x000000ff & packet[offset + 7])
				| (0x000000ff & packet[offset + 6]) << 8
				| (0x000000ff & packet[offset + 5]) << 16
				| (0x000000ff & packet[offset + 4]) << 24
				| (0x000000ff & packet[offset + 3]) << 32
				| (0x000000ff & packet[offset + 2]) << 40
				| (0x000000ff & packet[offset + 1]) << 48
				| (0x000000ff & packet[offset + 0]) << 56;
	}
	
	protected void setByte(int offset, byte b) 
	{
		packet[offset] = b;
	}
	
	protected void setShortL(int offset, short s)
	{
		packet[offset] = (byte) (s);
		packet[offset + 1] = (byte) (s >> 8);		
	}
	
	protected void setShortB(int offset, short s)
	{
		packet[offset + 1] = (byte) (s);
		packet[offset] = (byte) (s >> 8);		
	}

	protected void setIntL(int offset, int s)
	{
		packet[offset] = (byte) (s);
		packet[offset + 1] = (byte) (s >> 8);
		packet[offset + 2] = (byte) (s >> 16);
		packet[offset + 3] = (byte) (s >> 24);		
	}
	
	protected void setIntB(int offset, int s)
	{
		packet[offset + 3] = (byte) (s);
		packet[offset + 2] = (byte) (s >> 8);
		packet[offset + 1] = (byte) (s >> 16);
		packet[offset] = (byte) (s >> 24);
	}

	protected void setLongL(int offset, long s)
	{
		packet[offset + 0] = (byte) (s);
		packet[offset + 1] = (byte) (s >> 8);
		packet[offset + 2] = (byte) (s >> 16);
		packet[offset + 3] = (byte) (s >> 24);
		packet[offset + 4] = (byte) (s >> 32);
		packet[offset + 5] = (byte) (s >> 40);
		packet[offset + 6] = (byte) (s >> 48);
		packet[offset + 7] = (byte) (s >> 56);
	}
	
	protected void setLongB(int offset, long s)
	{
		packet[offset + 7] = (byte) (s);
		packet[offset + 6] = (byte) (s >> 8);
		packet[offset + 5] = (byte) (s >> 16);
		packet[offset + 4] = (byte) (s >> 24);
		packet[offset + 3] = (byte) (s >> 32);
		packet[offset + 2] = (byte) (s >> 40);
		packet[offset + 1] = (byte) (s >> 48);
		packet[offset + 0] = (byte) (s >> 56);
	}
	
	
	protected void setBytes(int offset, byte[] arr) 
	{
		for (int j = 0; j < arr.length; j++) 
		{
			packet[j + offset] = arr[j];
		}
	}

	public byte[] getBytes(int offset, int len) 
	{
		return Arrays.copyOfRange(packet, offset, offset + len);
	}
	
	
	protected void setString(int offset, String s)
	{
		int len = s.length();
				
//		setByte(offset++, (byte)(len+1));				
		for (int i = 0; i < len; i++)
		{
			setShortL(offset, (short) s.charAt (i));
			offset+=2;			
		}
		setShortL(offset, (short)0);				
	}
	
	public static int strLen(String s)
	{
		return (s.length()+1)*2;
	}
	
	public String getString(int offset)
	{
		if(charbuffer==null)
			charbuffer = new char[50];
			
		int len;
		for(len=0; len<charbuffer.length && offset<packet.length; len++, offset+=2)
		{
			charbuffer[len] = (char)getShortL(offset);
			if(charbuffer[len]=='\0')
			{				
				break;				
			}
		}
		return new String(charbuffer, 0, len);		
	}

	public String getCompactString(int offset)
	{
		if(charbuffer==null)
			charbuffer = new char[50];

		int len;
		for(len=0; len<charbuffer.length && offset<packet.length; len++, offset++)
		{
			charbuffer[len] = (char)getByte(offset);
			if(charbuffer[len]=='\0')
			{
				break;
			}
		}
		return new String(charbuffer, 0, len);
	}
	
	@Override
	public String toString()
	{
		return getHexString(packet);
	}
	
	public static String getHexString(byte[] b)
	{
		StringBuffer sb = new StringBuffer();
		sb.append("[");
		for(int i=0; i<b.length; i++)
		{
			if(i!=0)
				sb.append(" ");
			sb.append(hexCode(b[i]));
		}
		sb.append("]");
		return sb.toString();
	}
	
	public static String getHexString(int i)
	{
		String s="";
				
		s+= hexCode( (byte) (i >> 24));	
		s+= hexCode( (byte) (i >> 16));
		s+= hexCode( (byte) (i >> 8));
		s+= hexCode((byte) (i));
		
		return s;
	}
	
	private static String hexCode(byte b)
	{
		String hex = "";
		byte high = (byte) ((b & 0xff) >>> 4);
		byte low = (byte) (b & 0x0F);

		
		switch(high)
		{
		case 0: hex="0"; break;
		case 1: hex="1"; break;
		case 2: hex="2"; break;
		case 3: hex="3"; break;
		case 4: hex="4"; break;
		case 5: hex="5"; break;
		case 6: hex="6"; break;
		case 7: hex="7"; break;
		case 8: hex="8"; break;
		case 9: hex="9"; break;
		case 10: hex="a"; break;
		case 11: hex="b"; break;
		case 12: hex="c"; break;
		case 13: hex="d"; break;
		case 14: hex="e"; break;
		case 15: hex="f"; break;
		}
		
		switch(low)
		{
		case 0: hex+="0"; break;
		case 1: hex+="1"; break;
		case 2: hex+="2"; break;
		case 3: hex+="3"; break;
		case 4: hex+="4"; break;
		case 5: hex+="5"; break;
		case 6: hex+="6"; break;
		case 7: hex+="7"; break;
		case 8: hex+="8"; break;
		case 9: hex+="9"; break;
		case 10: hex+="a"; break;
		case 11: hex+="b"; break;
		case 12: hex+="c"; break;
		case 13: hex+="d"; break;
		case 14: hex+="e"; break;
		case 15: hex+="f"; break;		
		}
		
		return hex;
	}
	
	
	

}