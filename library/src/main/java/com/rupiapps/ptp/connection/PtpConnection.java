package com.rupiapps.ptp.connection;

import com.rupiapps.ptp.connection.ptpip.PtpConnection_Ip;
import com.rupiapps.ptp.connection.ptpip.PtpSocket;
import com.rupiapps.ptp.connection.ptpusb.PtpConnection_Usb;
import com.rupiapps.ptp.connection.ptpusb.PtpUsbEndpoints;
import com.rupiapps.ptp.connection.ptpusb.PtpUsbPort;
import com.rupiapps.ptp.datacallbacks.DataCallback;
import com.rupiapps.ptp.exceptions.InitFailException;
import com.rupiapps.ptp.exceptions.ReceiveDataException;
import com.rupiapps.ptp.exceptions.ResponseNotOkException;
import com.rupiapps.ptp.exceptions.SendDataException;
import com.rupiapps.ptp.packets.Packet;

import java.net.InetAddress;

public interface PtpConnection
{
	String getDeviceName();
	void connectAndInit(byte[] guid, String appname, Runnable waitForAck) throws InitFailException;
	boolean isConnected();
	void disconnect();

	void sendRequest(short reqcode, int tid, int[] param, int timeout) throws ResponseNotOkException, ReceiveDataException, SendDataException;
//	Packet requestPacket(short reqcode, int tid, int[] param, int timeout) throws ReceiveDataException, ResponseNotOkException, SendDataException;

	void sendPacket(Packet data, short reqcode, int tid, int[] param, int timeout) throws ResponseNotOkException, ReceiveDataException, SendDataException;
	void requestData(DataCallback cb, short reqcode, int tid, int[] param, int timeout) throws ReceiveDataException, ResponseNotOkException, SendDataException;

	public static PtpConnection create(InetAddress host)
	{
		return new PtpConnection_Ip(new PtpSocket(host));
	}

	public static PtpConnection create(PtpUsbEndpoints endpoints)
	{
		return new PtpConnection_Usb(new PtpUsbPort(endpoints));
	}
}
