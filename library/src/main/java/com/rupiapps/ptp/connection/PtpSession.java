package com.rupiapps.ptp.connection;

import com.rupiapps.ptp.PtpLog;
import com.rupiapps.ptp.constants.PtpOc;
import com.rupiapps.ptp.datacallbacks.CreatePacketDataCallback;
import com.rupiapps.ptp.datacallbacks.DataCallback;
import com.rupiapps.ptp.exceptions.ReceiveDataException;
import com.rupiapps.ptp.exceptions.ResponseNotOkException;
import com.rupiapps.ptp.exceptions.SendDataException;
import com.rupiapps.ptp.packets.Packet;
import com.rupiapps.ptp.constants.PtpRc;

import java.util.ArrayList;
import java.util.List;

public class PtpSession
{
    int USE_DEF_TIMEOUT = -1;

    private static int sessionid=1;
    private PtpConnection ptp;
    private boolean hasOpenSession;
    private int transactionid;

    public PtpSession(PtpConnection ptp)
    {
        this.ptp = ptp;
        transactionid = 0;
        hasOpenSession = false;
    }

    private int nextTransactionID()
    {
        return transactionid++;
    }

    public void openSession() throws ResponseNotOkException, ReceiveDataException, SendDataException
    {
        if(!ptp.isConnected())
            return;
        transactionid = 0;
        ptp.sendRequest(PtpOc.OpenSession, nextTransactionID(), new int[]{sessionid}, USE_DEF_TIMEOUT);

        PtpLog.debug("session is open");
        hasOpenSession = true;
    }

    public void closeAndDisconnect()
    {
        if(!ptp.isConnected())
            return;
        PtpLog.debug("close and disconnect");
        try
        {
            if(hasOpenSession)
            {
                try
                {
                    ptp.sendRequest(PtpOc.CloseSession, nextTransactionID(), null, 1500);
                }
                catch(ResponseNotOkException rne)
                {
                    PtpLog.error("close session not ok: " + rne.getResponseCode());
                }
                hasOpenSession=false;
            }
        }
        catch(Exception ex)
        {
            PtpLog.error(ex);
        }
        finally
        {
            ptp.disconnect();
        }
    }

    public Packet getDeviceInfo(boolean openSessionBefore) throws ResponseNotOkException, ReceiveDataException, SendDataException
    {
        if(!ptp.isConnected())
            return null;
        if(!hasOpenSession && openSessionBefore)
            openSession();
        return requestPacket(PtpOc.GetDeviceInfo, nextTransactionID(), null, USE_DEF_TIMEOUT);
    }


    public Packet getThumb(int handle, int timeout) throws ReceiveDataException, ResponseNotOkException, SendDataException
    {
        if(!ptp.isConnected())
            return null;
        if(!hasOpenSession)
            openSession();
        return requestPacket(PtpOc.GetThumb, nextTransactionID(), new int[]{handle}, timeout);
    }

    public Packet getDevicePropDesc(short dpcode) throws ResponseNotOkException, ReceiveDataException, SendDataException
    {
        if(!ptp.isConnected())
            return null;
        if(!hasOpenSession)
            openSession();
        return requestPacket(PtpOc.GetDevicePropDesc, nextTransactionID(), new int[]{0x0000ffff& (int)dpcode}, 500);
    }

    public void canon_getDevicePropValue(short dpcode) throws ResponseNotOkException, ReceiveDataException, SendDataException
    {
        if(!ptp.isConnected())
            return;
        if(!hasOpenSession)
            openSession();
        ptp.sendRequest(PtpOc.Canon_GetDevicePropValue, nextTransactionID(), new int[]{0x0000ffff& (int)dpcode}, USE_DEF_TIMEOUT);
    }

    public Packet getDevicePropValue(short dpcode) throws ResponseNotOkException, ReceiveDataException, SendDataException
    {
        if(!ptp.isConnected())
            return null;
        if(!hasOpenSession)
            openSession();
        return requestPacket(PtpOc.GetDevicePropValue, nextTransactionID(), new int[]{0x0000ffff& (int)dpcode}, USE_DEF_TIMEOUT);
    }

    public void setDevicePropValueInt(short dpcode, final int value) throws ResponseNotOkException, ReceiveDataException, SendDataException
    {
        if(!ptp.isConnected())
            return;
        if(!hasOpenSession)
            openSession();

        Packet data = new Packet(4)
        {
            @Override
            protected void init()
            {
                setIntL(0, value);
            }
        };
        ptp.sendPacket(data, PtpOc.SetDevicePropValue, nextTransactionID(), new int[]{0x0000ffff& (int)dpcode}, 5000);
    }

    public void setDevicePropValueShort(short dpcode, final short value) throws ResponseNotOkException, ReceiveDataException, SendDataException
    {
        if(!ptp.isConnected())
            return;
        if(!hasOpenSession)
            openSession();

        Packet data = new Packet(2)
        {
            @Override
            protected void init()
            {
                setShortL(0, value);
            }
        };
        ptp.sendPacket(data, PtpOc.SetDevicePropValue, nextTransactionID(), new int[]{0x0000ffff& (int)dpcode}, 5000);
    }

    public void setDevicePropValueByte(short dpcode, final byte value) throws ResponseNotOkException, ReceiveDataException, SendDataException
    {
        if(!ptp.isConnected())
            return;
        if(!hasOpenSession)
            openSession();

        Packet data = new Packet(1)
        {
            @Override
            protected void init()
            {
                setByte(0, value);
            }
        };
        ptp.sendPacket(data, PtpOc.SetDevicePropValue, nextTransactionID(), new int[]{0x0000ffff& (int)dpcode}, 5000);
    }



    public void setDevicePropValueString(short dpcode, final String value) throws ResponseNotOkException, ReceiveDataException, SendDataException
    {
        if(!ptp.isConnected())
            return;

        if(!hasOpenSession)
            openSession();

        Packet data = new Packet(Packet.strLen(value)+1)
        {
            @Override
            protected void init()
            {
                setByte(0, (byte)Packet.strLen(value));
                setString(1, value);
            }
        };
        ptp.sendPacket(data, PtpOc.SetDevicePropValue, nextTransactionID(), new int[]{0x0000ffff& (int)dpcode}, 5000);
    }

    public void canon_getThumbEx(int handle, DataCallback cb) throws ReceiveDataException, ResponseNotOkException, SendDataException
    {
        if(!ptp.isConnected())
            return;
        if(!hasOpenSession)
            openSession();

        ptp.requestData(cb, PtpOc.Canon_GetThumbEx, nextTransactionID(), new int[]{handle, 0x32000}, USE_DEF_TIMEOUT);
    }

    public void nikon_getLargeThumb(int handle, DataCallback cb) throws ReceiveDataException, ResponseNotOkException, SendDataException
    {
        if(!ptp.isConnected())
            return;
        if(!hasOpenSession)
            openSession();

        ptp.requestData(cb, PtpOc.Nikon_GetLargeThumb, nextTransactionID(), new int[]{handle}, USE_DEF_TIMEOUT);
    }


    public void getObject(int handle, DataCallback cb) throws ReceiveDataException, ResponseNotOkException, SendDataException
    {
        if(!ptp.isConnected())
            return;
        if(!hasOpenSession)
            openSession();

        ptp.requestData(cb, PtpOc.GetObject, nextTransactionID(), new int[]{handle}, USE_DEF_TIMEOUT);
    }

    public void getPartialObject(int handle, int offset, int size, DataCallback cb) throws ReceiveDataException, ResponseNotOkException, SendDataException
    {
        if(!ptp.isConnected())
            return;
        if(!hasOpenSession)
            openSession();

        PtpLog.debug("getPartialObject "+offset+" "+size);
        ptp.requestData(cb, PtpOc.GetPartialObject, nextTransactionID(), new int[]{handle, offset, size}, USE_DEF_TIMEOUT);
    }


    public Packet getObjectInfo(int handle) throws ReceiveDataException, ResponseNotOkException, SendDataException
    {
        if(!ptp.isConnected())
            return null;
        if(!hasOpenSession)
            openSession();

        return requestPacket(PtpOc.GetObjectInfo, nextTransactionID(), new int[]{handle}, USE_DEF_TIMEOUT);
    }

    public Packet canon_getObjectInfo(int handle) throws ReceiveDataException, ResponseNotOkException, SendDataException
    {
        if(!ptp.isConnected())
            return null;
        if(!hasOpenSession)
            openSession();

        return requestPacket(PtpOc.Canon_GetObjectInfo, nextTransactionID(), new int[]{handle}, USE_DEF_TIMEOUT);
    }

    public Packet canon_getObjectInfoEx(int handle) throws ReceiveDataException, ResponseNotOkException, SendDataException
    {
        if(!ptp.isConnected())
            return null;
        if(!hasOpenSession)
            openSession();

        return requestPacket(PtpOc.Canon_GetObjectInfoEx, nextTransactionID(), new int[]{0x020001, handle, 0x2000}, USE_DEF_TIMEOUT);
    }

    public List<Integer> getObjectHandles(int storageid, int formatcode) throws ReceiveDataException, ResponseNotOkException, SendDataException
    {
        if(!ptp.isConnected())
            return new ArrayList<>();
        if(!hasOpenSession)
            openSession();

        Packet p = null;
        try
        {
            p = requestPacket(PtpOc.GetObjectHandles, nextTransactionID(), new int[]{storageid, formatcode, 0}, USE_DEF_TIMEOUT);
        }
        catch(ResponseNotOkException e)
        {
            if(e.getResponseCode()== PtpRc.StoreNotAvailable)
            {
                try {Thread.sleep(750);}catch (InterruptedException ie){}

                p = requestPacket(PtpOc.GetObjectHandles, nextTransactionID(), new int[]{storageid, formatcode, 0}, USE_DEF_TIMEOUT);
            }
            else
                throw e;
        }
        List<Integer> handles = new ArrayList<Integer>();
        if(p!=null && p.getBufferSize()>=4)
        {
            int len = p.getIntL(0);
            int offset=4;
            for(int i=0; i<len && offset+3<p.getBufferSize(); i++, offset+=4)
                handles.add(p.getIntL(offset));
        }

        return handles;
    }

    public List<Packet> canon_checkEvents() throws ResponseNotOkException, ReceiveDataException, SendDataException
    {
        if(!ptp.isConnected())
            return new ArrayList<Packet>();
        if(!hasOpenSession)
            openSession();

        //		PtpLog.debug("canon_getEvent()");

        Packet data = requestPacket(PtpOc.Canon_GetEvent, nextTransactionID(), new int[]{0,0,0}, USE_DEF_TIMEOUT);
        List<Packet> events = new ArrayList<Packet>();
        if(data!=null)
        {
            int idx = 0;
            while(idx + 3 < data.getBufferSize())
            {
                int len = data.getIntL(idx);
                if(len < 0)
                    break;

                Packet p = new Packet(data.getBytes(idx, len));
                events.add(p);

                idx += len;
            }
        }
        return events;
    }

    public List<Packet> nikon_checkEvents() throws ResponseNotOkException, ReceiveDataException, SendDataException
    {
        if(!ptp.isConnected())
            return new ArrayList<>();

        if(!hasOpenSession)
            openSession();

        //		PtpLog.debug("nikon_getEvent()");


        Packet data = requestPacket(PtpOc.Nikon_CheckEvent, nextTransactionID(), null, USE_DEF_TIMEOUT);
        List<Packet> events = new ArrayList<Packet>();
        if(data!=null && data.getBufferSize()>=2)
        {
            int numevents = data.getShortL(0);
            int idx = 2;
            while(idx+5< data.getBufferSize() && numevents > 0)
            {
                int len = 6;

                Packet p = new Packet(data.getBytes(idx, len));
                events.add(p);
                numevents--;

                idx += len;
            }
        }
        return events;
    }

    public void canon_setDevicePropValue(final short code, final int value) throws ReceiveDataException, ResponseNotOkException, SendDataException
    {
        if(!ptp.isConnected())
            return;
        if(!hasOpenSession)
            openSession();

        Packet data = new Packet(12)
        {
            @Override
            protected void init()
            {
                setIntL(0, 12);
                setShortL(4, code);
                setShortL(6, (short)0);
                setIntL(8, value);
            }
        };
        ptp.sendPacket(data, PtpOc.Canon_SetDevicePropValue, nextTransactionID(), null, USE_DEF_TIMEOUT);
    }

    public void canon_setDevicePropValue(final short propcode, final int[] values) throws ReceiveDataException, ResponseNotOkException, SendDataException
    {
        if(!ptp.isConnected())
            return;

        if(!hasOpenSession)
            openSession();


        Packet data = new Packet(8+values.length*4)
        {
            @Override
            protected void init()
            {
                setIntL(0, 8+values.length*4);
                setShortL(4, propcode);
                setShortL(6, (short)0);
//                setIntL(8, values.length*4+4);

                int offset = 8;
                for(int i=0; i<values.length; i++)
                {
                    int v = values[i];
                    setIntL(offset, v);
                    offset+=4;
                }
            }
        };
//        PtpLog.debug("send: "+data);
        ptp.sendPacket(data, PtpOc.Canon_SetDevicePropValue, nextTransactionID(), null, USE_DEF_TIMEOUT);
    }

    public Packet canon_getViewFinderImage(int param2) throws ReceiveDataException, ResponseNotOkException, SendDataException
    {
        if(!ptp.isConnected())
            return null;

//        PtpLog.debug("canon_getViewFinderImage");
        if(!hasOpenSession)
            openSession();

        return requestPacket(PtpOc.Canon_GetViewfinderData, nextTransactionID(), new int[]{0x200000,param2,0}, USE_DEF_TIMEOUT);
    }

    public Packet nikon_getLiveViewImage() throws ReceiveDataException, ResponseNotOkException, SendDataException
    {
        if(!ptp.isConnected())
            return null;

//        PtpLog.debug("nikon_getLiveViewImage");
        if(!hasOpenSession)
            openSession();

        return requestPacket(PtpOc.Nikon_GetLiveViewImage, nextTransactionID(), null, USE_DEF_TIMEOUT);
    }

    public List<Integer> getHandlesFromCtgInfo(int storageid) throws ReceiveDataException, SendDataException
    {
        List<Integer> handles = new ArrayList<Integer>();
        if(!ptp.isConnected())
            return handles;


        int handleoffset = 0x91900000;
        for(int i=0; i<4; i++, handleoffset+=0x40000)
        {

            try
            {
                Packet p = requestPacket(PtpOc.Canon_GetCTGInfo, nextTransactionID(), new int[]{storageid, handleoffset, 3, 0x200000}, USE_DEF_TIMEOUT);
                readHandlesFromCTGPacket(handles, p);
            }
            catch(ResponseNotOkException e)
            {
                break;
            }
        }

        try
        {
            Packet p = requestPacket(PtpOc.Canon_GetCTGInfo, nextTransactionID(), new int[]{storageid, 0x51900000, 3, 0x200000}, USE_DEF_TIMEOUT);
            readHandlesFromCTGPacket(handles, p);
        }
        catch(ResponseNotOkException e)
        {
        }

        return handles;
    }

    private void readHandlesFromCTGPacket(List<Integer> handles, Packet p)
    {
        int offset = 0;
        if(p.getBufferSize()>4)
        {
            int size = p.getIntL(offset);
            offset+=4;
            for(int j=0; j<size; j++)
            {
                if(p.getBufferSize()>offset+4)
                {
                    int elemsize = p.getIntL(offset);
                    if(p.getBufferSize()>offset+elemsize)
                    {
                        int handle = p.getIntL(offset+4);
                        offset+=elemsize;
                        handles.add(handle);
                    }
                }

            }
        }
    }

    public List<Integer> getHandlesFromObjectInfoEx(int storageid) throws ReceiveDataException, SendDataException
    {
        List<Integer> handles = new ArrayList<Integer>();
        if(!ptp.isConnected())
            return handles;

        int handleoffset = 0x91900000;
        for(int i=0; i<4; i++, handleoffset+=0x40000)
        {

            try
            {
                Packet p = requestPacket(PtpOc.Canon_GetObjectInfoEx64, nextTransactionID(), new int[]{storageid, handleoffset, 0x200000}, USE_DEF_TIMEOUT);
                readHandlesFromCTGPacket(handles, p);
            }
            catch(ResponseNotOkException e)
            {
                break;
            }
        }

        try
        {
            Packet p = requestPacket(PtpOc.Canon_GetObjectInfoEx64, nextTransactionID(), new int[]{storageid, 0x51900000, 0x200000}, USE_DEF_TIMEOUT);
            readHandlesFromCTGPacket(handles, p);
        }
        catch(ResponseNotOkException e)
        {
        }

        return handles;
    }

//    private void readHandlesFromObjInfoEx(List<Integer> handles, Packet p)
//    {
//        int offset = 0;
//        if(p.getBufferSize() > 4)
//        {
//            int numObj = p.getIntL(offset);
//            offset += 4;
//            for(int j = 0; j < numObj; j++)
//            {
//                int size = p.getIntL(offset);
//
//
//                offset+=size;
//            }
//        }
//    }


    public void simpleRequest(short reqcode, int[] param) throws ReceiveDataException, SendDataException, ResponseNotOkException
    {
        if(!hasOpenSession)
            openSession();
        ptp.sendRequest(reqcode, nextTransactionID(), param, USE_DEF_TIMEOUT);
    }

    public Packet requestPacket(short reqcode, int tid, int[] param, int timeout) throws ReceiveDataException, ResponseNotOkException, SendDataException
    {
        CreatePacketDataCallback packetCallback = new CreatePacketDataCallback();

        ptp.requestData(packetCallback, reqcode, tid, param, timeout);

        return packetCallback.getPacket();
    }

}
