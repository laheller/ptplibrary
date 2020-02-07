# ptplibrary
use ptp/ip and ptp/usb to connect to cameras like Canon and Nikon

## Setup
### Add dependency 
```Gradle
//in build.gradle of module
dependencies {
    implementation 'com.github.rupiapps:ptplibrary:1.0'
}
//in build.gradle of project
allprojects {
    repositories {
        maven {
            url "https://jitpack.io"
        }
    }
}
```
### Add permissions to manifest file
```Xml
<uses-permission android:name="android.permission.INTERNET"/>
```

## Example usage
Do not run this on mainthread otherwise you will get NetworkOnMainthreadException
```Java
            //this is only a test code
            //do not make try-catch-block around all your code
            try
            {
                System.out.println("start connect");

                //1. create a connection object by either using ptp/ip or ptp/usb
                InetAddress host = InetAddress.getByName("192.168.1.1");
                PtpConnection connection = PtpConnection.create(host);

                //2. connect to the host with your guid and name
                //   provide a callback to give feedback to the user
                //   if connection needs to be acknowledged
                connection.connectAndInit(
                        //16 byte guid
                        new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                   0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00},
                        "appname",
                        () -> System.out.println("waiting for acknowledge")
                );

                //3. use connection object to create a session
                PtpSession session = new PtpSession(connection);

                //4. get deviceinfo - some cameras need to have open session to get this
                Packet data = session.getDeviceInfo(true);
                DeviceInfoPacket deviceInfo = new DeviceInfoPacket(data);

                //check if valid
                if(deviceInfo.getBufferSize()>10)
                {
                    System.out.println(deviceInfo.getModel());
                    System.out.println(deviceInfo.getManufacturer());
                }

                //5. close the session after use
                session.closeAndDisconnect();
            }
            catch(InitFailException ife)
            {
                System.out.println("init failed: " + ife.getReason());
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
```
Output: 
```
I/System.out: start connect
I/System.out: waiting for acknowledge
I/System.out: D5300
I/System.out: Nikon Corporation
```
## Using ptp/usb
For usb connections you will have to implement PtpUsbEndpoints on your operating system. 
An implementation for Android is not part of this project to keep the project independent from android.

Here is how it could look like:
```Java
public class AndroidUsbEndpoints implements PtpUsbEndpoints
{
	private static int DEFAULT_TIMEOUT = 10000;
	private UsbDeviceConnection usbconnection;
	private UsbInterface usbinterface;
	private UsbEndpoint data_out, data_in, interrupt;
	private int timeout;
	
	
	public AndroidUsbEndpoints(UsbDeviceConnection usbconnection, UsbInterface usbinterface)
	{
		this.usbconnection = usbconnection;
		this.usbinterface = usbinterface;
		timeout = DEFAULT_TIMEOUT;
	}
	
	public void initalize() throws InitFailException
	{
		if(!usbconnection.claimInterface(usbinterface, true))
			throw new InitFailException(InitFailReason.ClaimFailed);
		
		if(usbinterface.getEndpointCount()<3)
			throw new InitFailException(InitFailReason.NoEndpoints);
		
		for(int i=0; i<usbinterface.getEndpointCount(); i++)
		{
			UsbEndpoint ep = usbinterface.getEndpoint(i);
			
			boolean isout = ep.getDirection()==UsbConstants.USB_DIR_OUT;
			boolean isbulk = ep.getType()==UsbConstants.USB_ENDPOINT_XFER_BULK;
			
			if(isout && isbulk)
				data_out = ep;
			if(!isout && isbulk)
				data_in = ep;
			if(!isbulk)
				interrupt = ep;			
		}
		
		if(data_out==null || data_in==null || interrupt==null)
			throw new InitFailException(InitFailReason.NoEndpoints);
	}
	
	public void release()
	{
		usbconnection.releaseInterface(usbinterface);
	}
	
	public int controlTransfer(int requestType, int request, int value, int index, byte[] buffer)
	{
		return usbconnection.controlTransfer(requestType, request, value, index, buffer, buffer!=null?buffer.length:0, 1500);
	}

	public void setTimeOut(int to)
	{
		timeout = to>0?to:DEFAULT_TIMEOUT;
	}
	
	public int writeDataOut(byte[] buffer, int length) throws SendDataException
	{
		int len = usbconnection.bulkTransfer(data_out, buffer, length, timeout);
		if(len<0)
			throw new SendDataException("senderror: len is "+len);
		return len;
	}
	
	public int readDataIn(byte[] buffer) throws ReceiveDataException
	{
		int len=0; 
		while(len==0)		
		  len = usbconnection.bulkTransfer(data_in, buffer, buffer!=null?buffer.length:0, timeout);
		if(len<0)
			throw new ReceiveDataException("receiveerror: len is "+len);
		return len;
	}
	
	public void readEvent(byte[] buffer, boolean bulk)
	{		
		if(bulk)
			usbconnection.bulkTransfer(interrupt, buffer, buffer.length, 500);
		else
		{
			UsbRequest req = new UsbRequest();
			req.initialize(usbconnection, interrupt);		
			ByteBuffer bbuffer = ByteBuffer.wrap(buffer);
			req.queue(bbuffer, buffer.length);							
			req = usbconnection.requestWait();
			PtpLog.debug("received event");
			if(req!=null)
			{
				PtpLog.debug("req != null");
				PtpLog.debug(new Packet(bbuffer.array()).toString());
			}	
			else
				PtpLog.debug("event is null");
		}
		
	}
	
	public int getMaxPacketSizeOut()
	{
		return data_out.getMaxPacketSize();
	}
	
	public int getMaxPacketSizeIn()
	{
		return data_in.getMaxPacketSize();
	}
	
	public int getMaxPacketSizeInterrupt()
	{
		return interrupt.getMaxPacketSize();
	}	
}
```
