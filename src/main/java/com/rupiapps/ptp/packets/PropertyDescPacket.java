package com.rupiapps.ptp.packets;

import com.rupiapps.ptp.PtpLog;
import com.rupiapps.ptp.packets.Packet;

/**
 * Created by Rupert on 04.02.2017.
 */

public class PropertyDescPacket extends Packet
{
    public static byte FORM_NONE = 0;
    public static byte FORM_RANGE = 1;
    public static byte FORM_ENUMERATION = 2;

    private int tsize;
    private boolean signed;
    private boolean isString;

    public PropertyDescPacket(byte[] bytes)
    {
        super(bytes);
        if(isValid())
        {
            tsize = getTypeSize();
            signed = getType() % 2 == 1;
            isString = getType() == (short) 0xffff;
        }
    }

    public PropertyDescPacket(Packet p)
    {
        super(p.getBuffer());
        if(isValid())
        {
            tsize = getTypeSize();
            signed = getType() % 2 == 1;
            isString = getType() == (short) 0xffff;
        }
    }

    public boolean isValid()
    {
        return this.getBuffer().length>5;
    }

    public boolean isStringType()
    {
        return isString;
    }

    public short getPropertyCode()
    {
        return getShortL(0);
    }

    public short getType()
    {
        return getShortL(2);
    }

    public boolean canSet()
    {
        return getByte(4)!=0;
    }

    public int getTypeSize()
    {
        short t = getType();

        if(t==1 || t==2)
            return 1;
        if(t==3 || t==4)
            return 2;
        if(t==5 || t==6)
            return 4;
        if(t==7 || t==8)
            return 8;
        if(t==9 || t==10)
            return 16;
        return -1;
    }

    public int getDefaultValueOffset()
    {
        return 5;
    }

    private int readValueAt(int pos)
    {
        if(pos+tsize>getBufferSize())
            return -1;
        if(tsize==1 && signed)
            return getByte(pos);
        if(tsize==2 && signed)
            return getShortL(pos);

        return getIntL(pos, tsize);
    }

    public int getDefaultValueInt()
    {
        return readValueAt(getDefaultValueOffset());
    }

    public String getDefaultValueString()
    {
        return getString(getDefaultValueOffset()+1);
    }

    public int getCurrentValueOffset()
    {
        if(isString)
        {
            int len = getByte(getDefaultValueOffset())*2+1;
            return 5+len;
        }

        return 5+tsize;
    }

    public int getCurrentValueInt()
    {
        return readValueAt(getCurrentValueOffset());
    }

    public String getCurrentValueString()
    {
        return getString(getCurrentValueOffset()+1);
    }

    public byte getForm()
    {
        return getByte(firstValueOffset()-3);
    }

    public int numberOfValues()
    {
        return getShortL(firstValueOffset()-2);
    }

    public int firstValueOffset()
    {
        if(isString)
        {
            int prevoffset = getCurrentValueOffset();
            int len = getByte(prevoffset)*2+1;
            return prevoffset+len+3;
        }

        return 8+tsize+tsize;
    }

    public int getValueInt(int idx)
    {
        int pos = firstValueOffset()+idx*tsize;
        return readValueAt(pos);
    }

    public String getValueString(int idx)
    {
        int pos = getValueOffset(idx);
        return getString(pos+1);
    }

    private int getValueOffset(int idx)
    {
        if(idx<0)
            return 0;
        if(idx==0)
            return firstValueOffset();

        int prevOffset = getValueOffset(idx-1);
        int len = getByte(prevOffset)*2+1;
        return prevOffset+len;
    }

    public int getRangeMinValue()
    {
        int pos = 6+tsize+tsize;
        return readValueAt(pos);
    }

    public int getRangeMaxValue()
    {
        int pos = 6+tsize+tsize+tsize;
        return readValueAt(pos);
    }

    public int getRangeStepSize()
    {
        int pos = 6+tsize+tsize+tsize+tsize;
        return readValueAt(pos);
    }

}
