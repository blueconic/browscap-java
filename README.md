# browscap-java
A blazingly fast and memory efficient Java client on top of the BrowsCap CSV source files.
The BrowsCap version currently shipped is: 6022.

## Notes
* Although this library is very fast, implementing a cache is advisable. Since cache strategies differ per usecase, this library doesn't ship with one out of the box.
* The followings BrowsCap fields are available:
  * browser (e.g. Chrome)
  * browserType (e.g. Browser or Application)
  * browserMajorVersion (e.g. 55 in case of Chrome)
  * deviceType (e.g. Mobile Phone, Desktop, Tablet, Console, TV Device)
  * platform (e.g. Android, iOS, Win7, Win10)
  * platformVersion (e.g. 4.2, 10 depending on what the platform is)
* The fields are not configurable.
* The CSV file is read in a streaming way, so it's processed line by line. This makes it more memory efficient than loading the whole into memory first.
* 1000+ user agents are tested in the unit tests.

## Future
Possible new features we're thinking of (and are not yet present):
* Make the fields configurable and let Capabilities return a Map containing the given fields
* Auto-update the BrowsCap CSV or use an InputStream to use an alternative CSV file.

## Maven
Add this to the dependencies in your pom.xml.

```xml
<dependency>
  <groupId>com.blueconic</groupId>
  <artifactId>browscap-java</artifactId>
  <version>1.0.0</version>
</dependency>
```

## Usage
```java
import com.blueconic.browscap.Capabilities;
import com.blueconic.browscap.ParseException;
import com.blueconic.browscap.UserAgentParser;
import com.blueconic.browscap.UserAgentService;

// ... class definition

final UserAgentParser parser = new UserAgentService().loadParser(); // handle IOException and ParseException

// parser can be re-used for multiple lookup calls
final String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.81 Safari/537.36";
final Capabilities capabilities = parser.parse(userAgent);

final String browser = capabilities.getBrowser();
final String browserType = capabilities.getBrowserType();
final String browserMajorVersion = capabilities.getBrowserMajorVersion();
final String deviceType = capabilities.getDeviceType();
final String platform = capabilities.getPlatform();
final String platformVersion = capabilities.getPlatformVersion();

// do something with the values

```
