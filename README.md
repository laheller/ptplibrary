# ptplibrary
use ptp/ip and ptp/usb to connect to cameras like Canon and Nikon

##Setup
###Add dependency 
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
###Add permissions to manifest file
```Xml
<uses-permission android:name="android.permission.INTERNET"/>
```

##Example usage
