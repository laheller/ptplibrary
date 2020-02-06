package com.rupiapps.ptp.connection.ptpip;

import com.rupiapps.ptp.PtpLog;
import com.rupiapps.ptp.exceptions.InitFailException;
import com.rupiapps.ptp.exceptions.InitFailException.InitFailReason;
import com.rupiapps.ptp.exceptions.ReceiveDataException;
import com.rupiapps.ptp.exceptions.SendDataException;
import com.rupiapps.ptp.packets.Packet;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;


public class PtpSocket
{
	private InetAddress host;
	private int port;
		
	private Socket dataSocket, eventSocket;
	private OutputStream dataOut, eventOut;
	private DataInputStream dataIn;
	private InputStream eventIn;
	private boolean isDataConnected, isEventConnected;
	private byte[] receiveBuffer, lenBuffer, eventBuffer, streamBuffer;
	private static int DEFAULT_TIMEOUT = 20000;
	
	public PtpSocket(InetAddress host, int port)
	{		
		this.host = host;
		this.port = port;
		isDataConnected = false;
		isEventConnected = false;
//		receiveBuffer = new byte[512];
		lenBuffer = new byte[4];
		eventBuffer = new byte[14];
		streamBuffer = new byte[1024*16];
	}
	
	public PtpSocket(InetAddress host)
	{
		this(host, 15740);		
	}
	
	public InetAddress getHost()
	{
		return host;
	}
	
	public int getPort()
	{
		return port;
	}
	
	public boolean isDataConnected()
	{
		return isDataConnected;
	}
	
	public boolean isEventConnected()
	{
		return isEventConnected;
	}	
	
	public void connectDataLine() throws InitFailException 
	{
		if(isDataConnected)
			return;		
				
		try
        {
			dataSocket = new Socket();
			dataSocket.setSoTimeout(DEFAULT_TIMEOUT);
			dataSocket.setTcpNoDelay(true);
			dataSocket.setKeepAlive(true);
			dataSocket.setReuseAddress(true);
			dataSocket.connect(new InetSocketAddress(host, port), 2500);
			dataIn = new DataInputStream(dataSocket.getInputStream());
	        dataOut = dataSocket.getOutputStream();
        }
        catch (IOException e)
        {
        	if(e instanceof UnknownHostException)
        		throw new InitFailException(InitFailReason.HostNotFound);        	    	        	
	        
	        throw new InitFailException(InitFailReason.NoConnection);
        }
		
		PtpLog.debug("connected to "+host+":"+port);
		isDataConnected = true;
	}
	
	public void connectEventLine() throws InitFailException 
	{
		if(isEventConnected)
			return;
		
		try
        {
			eventSocket = new Socket();
			eventSocket.setSoTimeout(120000);
			eventSocket.setTcpNoDelay(true);
			eventSocket.setKeepAlive(true);
			
			eventSocket.connect(new InetSocketAddress(host, port), 5000);		
			eventIn = (eventSocket.getInputStream());
			eventOut = eventSocket.getOutputStream();
        }
		catch (IOException e)
        {
        	if(e instanceof UnknownHostException)
        		throw new InitFailException(InitFailReason.HostNotFound);        	        	        	
	        
	        throw new InitFailException(InitFailReason.NoConnection);
        }
		isEventConnected = true;
	}
	
	public void disconnect() 
	{
		if(!isDataConnected)
			return;
				
		try
		{
			if(dataSocket!=null)
			{
				dataIn.close();
				dataOut.close();
				dataSocket.shutdownInput();
				dataSocket.shutdownOutput();
				dataSocket.close();
			}
			if(eventSocket!=null)
			{
				eventSocket.shutdownInput();
				eventSocket.shutdownOutput();	
				eventSocket.close();
			}
		}
		catch(IOException ex)
		{	
			PtpLog.debug(ex.getMessage());
		}
		
		isDataConnected = false;
		isEventConnected = false;
		dataIn = null;
		dataOut = null;
		eventIn = null;
		eventOut = null;
		
		
	}	
	
	public void sendDataPacket(Packet packet) throws SendDataException
	{
		if(!isDataConnected)
			return;
				
		byte[] buffer = packet.getBuffer();
				
		try
        {
	        dataOut.write(buffer);
			dataOut.flush();
        }
        catch (IOException e)
        {
	        throw new SendDataException(e.getMessage());
        }
	}

	
	private void setTimeout(int timeout)
	{
		try
		{
			dataSocket.setSoTimeout(timeout);
		}
		catch(SocketException sex)
		{				
		}		
	}
	
	public Packet receivePtpPacket(int timeout) throws ReceiveDataException
	{
		if(!isDataConnected)
			return null;
		
		if(timeout>=0)
		{
			setTimeout(timeout);			
		}
		int len = -1;
		try
		{
			len = dataIn.read(lenBuffer);
			if(len==4)
			{
				int length = (0x000000ff & lenBuffer[0])
						   | (0x000000ff & lenBuffer[1]) << 8
						   | (0x000000ff & lenBuffer[2]) << 16
						   | (0x000000ff & lenBuffer[3]) << 24;
				if(length<0)
					throw new ReceiveDataException("length <0");
					
				receiveBuffer = new byte[length];
				if(length>=4)
				{
					System.arraycopy(lenBuffer, 0, receiveBuffer, 0, 4);
					dataIn.readFully(receiveBuffer, 4, length-4);	
				}
			}
		}
		catch(IOException ex)
		{

			throw new ReceiveDataException("could not read stream");
		}
		catch(OutOfMemoryError e)
		{
			throw new ReceiveDataException(e.toString());			
		}
		finally
		{
			if(timeout>=0)
			{
				setTimeout(DEFAULT_TIMEOUT);
			}
		}
		if(len<0)
		{			
			throw new ReceiveDataException("len <0");
		}		
						
		return new Packet(receiveBuffer);
	}
	
	public Packet receivePtpHeader(boolean readtransactionid) throws ReceiveDataException
	{
		try
		{
			receiveBuffer = new byte[readtransactionid?12:8];
			dataIn.readFully(receiveBuffer);
		}
		catch(IOException ex)
		{			
			throw new ReceiveDataException("could not read header");
		}
		return new Packet(receiveBuffer);
	}
	
	public void receiveDataStream(int bytesToRead, ReadStreamCallback cb) throws ReceiveDataException
	{
				
		while(bytesToRead>streamBuffer.length)
		{
			try
			{
				int len = dataIn.read(streamBuffer);	
				if(len==0)
					continue;
				bytesToRead-=len;
				cb.receivedBytes(streamBuffer, len);
			}
			catch(IOException ex)
			{
				String msg = ex.getMessage();
				throw new ReceiveDataException("could not read data stream1 "+msg);
			}	
		}
		
		if(bytesToRead>0)
		{
			try
			{
				dataIn.readFully(streamBuffer, 0, bytesToRead);
				cb.receivedBytes(streamBuffer, bytesToRead);
			}
			catch(IOException ex)
			{			
				throw new ReceiveDataException("could not read data stream2 "+ex.getMessage());
			}	
		}
	}
	
	
	public void sendEventPacket(Packet packet) throws SendDataException
	{
		if(!isEventConnected)
			return;
				
		byte[] buffer = packet.getBuffer();
				
		try
        {
	        eventOut.write(buffer);
        }
        catch (IOException e)
        {	        
	        throw new SendDataException(e.getMessage());
        }
	}
	
	public boolean isEventAvailable()
	{
		try
        {
	        return eventIn.available()>0;
        }
        catch (IOException e)
        {
	        return false;
        }
	}
	
	public Packet receiveEventPacket() throws ReceiveDataException
	{
		if(!isEventConnected)
			return null;
				
		int len = -1;
		try
		{			
			len = eventIn.read(eventBuffer);			
			
		}
		catch(IOException ex)
		{			
			throw new ReceiveDataException("could not read stream");			
		}
		if(len<0)
		{			
			throw new ReceiveDataException("packet has length <0");
		}		
						
		return new Packet(Arrays.copyOf(eventBuffer, len));
	}
	
	public interface ReadStreamCallback
	{
		void receivedBytes(byte[] buffer, int len) throws ReceiveDataException;
	}
	
}
