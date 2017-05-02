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

## Usage
```java
import com.blueconic.browscap.UserAgentParser;
import com.blueconic.browscap.UserAgentService;
import com.blueconic.browscap.domain.Capabilities;

// ... class definition

final UserAgentService userAgentService = new UserAgentService();
final UserAgentParser parser = userAgentService.loadParser();

// parser can be re-used
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