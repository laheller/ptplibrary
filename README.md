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
