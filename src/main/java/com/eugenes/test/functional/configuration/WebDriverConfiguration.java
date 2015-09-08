package com.eugenes.test.functional.configuration;

import static com.gargoylesoftware.htmlunit.BrowserVersion.INTERNET_EXPLORER_9;
import static java.lang.String.format;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.Platform;
import org.openqa.selenium.Point;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriver.Window;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.Augmenter;
import org.openqa.selenium.remote.BrowserType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.FileDetector;
import org.openqa.selenium.remote.LocalFileDetector;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.safari.SafariOptions;
import org.openqa.selenium.support.events.EventFiringWebDriver;
import org.openqa.selenium.support.ui.FluentWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import au.com.permeance.test.functional.selenium.BrowserSupport;
import au.com.permeance.test.functional.selenium.RemoteFluentWait;
import au.com.permeance.test.functional.selenium.firefox.FirefoxProfileConfigurer;
import au.com.permeance.test.functional.selenium.ie.ScrollBeforeClickEventListener;
import au.com.permeance.test.functional.selenium.safari.SafariBrowserSupport;
import au.com.permeance.test.functional.spring.PageObjectBeanPostProcessor;

@ComponentScan({"au.com.permeance.test.functional"})
@Configuration
@PropertySource({"classpath:feature.properties", "classpath:feature-ext.properties"})
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class WebDriverConfiguration {

    public static final String SAFARI_WITH_ROBOT = "robotSafari";

    @Value("${driver.implicitlyWait.time:500}")
    private long implicitWaitTime;

    @Value("${driver.implicitlyWait.unit:MILLISECONDS}")
    private TimeUnit implicitWaitUnit;

    @Value("${driver.pageLoadTimeout.time:30}")
    private long pageLoadTimeoutTime;

    @Value("${driver.pageLoadTimeout.unit:SECONDS}")
    private TimeUnit pageLoadTimeoutUnit;

    @Value("${wait.pollingEveryTime.time:500}")
    private int pollingEveryTime;

    @Value("${wait.pollingEveryTime.unit:MILLISECONDS}")
    private TimeUnit pollingEveryUnit;

    @Value("${driver.scriptTimeout.time:5}")
    private long scriptTimeoutTime;

    @Value("${driver.scriptTimeout.unit:SECONDS}")
    private TimeUnit scriptTimeoutUnit;

    @Autowired
    private WebDriver webDriver;

    @Value("${browser.window.height:0}")
    private int windowHeight;

    @Value("${browser.window.width:0}")
    private int windowWidth;

    @Value("${wait.withTimeout.time}")
    private int withTimeoutTime;

    @Value("${wait.withTimeout.unit}")
    private TimeUnit withTimeoutUnit;

    @Value("${wait.short.withTimeout.time:5}")
    private int shortTimeoutTime;

    @Value("${wait.short.withTimeout.unit:SECONDS}")
    private TimeUnit shortTimeoutTimeUnit;

    @Value("${test.server.protocol:http}")
    private String serverProtocol;

    @Value("${test.server.host:localhost}")
    private String serverHost;

    @Value("${test.server.port:80}")
    private String serverPort;

    @Value("${driver.remote.capability.browser:firefox}")
    private String browser;

    @Bean
    public WaitConfiguration waitConfig() {
        return WaitConfiguration.builder().timeoutTime(withTimeoutTime).timeoutUnit(withTimeoutUnit).build();
    }

    /**
     * Define the PageObjectPostProcessor explicitly, rather than using @Component, to enable
     * overriding of the bean definition in sub-modules (e.g. relative-finder-module).
     *
     * @return
     */
    @Bean(name = "pageObjectPostProcessor")
    public BeanPostProcessor pageObjectPostProcessor() {
        return new PageObjectBeanPostProcessor();
    }

    @Bean
    public BrowserSupport browserSupport() {
        if (BrowserType.SAFARI.equals(browser) || SAFARI_WITH_ROBOT.equals(browser)) {
            return new SafariBrowserSupport();
        } else {
            return new BrowserSupport();
        }
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {

        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    @Primary
    public FluentWait<WebDriver> fluentWait() {

        return new RemoteFluentWait<WebDriver>(webDriver).pollingEvery(pollingEveryTime, pollingEveryUnit)
                .withTimeout(withTimeoutTime, withTimeoutUnit).ignoring(NoSuchElementException.class, StaleElementReferenceException.class);
    }

    @Bean(name = Beans.SHORT_DURATION)
    public FluentWait<WebDriver> shortDurationWait() {

        return new FluentWait<WebDriver>(webDriver).pollingEvery(pollingEveryTime, pollingEveryUnit)
                .withTimeout(shortTimeoutTime, shortTimeoutTimeUnit)
                .ignoring(NoSuchElementException.class, StaleElementReferenceException.class);
    }

    @PostConstruct
    public void setWebDriverTimeouts() {

        webDriver.manage().timeouts().implicitlyWait(implicitWaitTime, implicitWaitUnit)
                .setScriptTimeout(scriptTimeoutTime, scriptTimeoutUnit);
        try {
            webDriver.manage().timeouts().pageLoadTimeout(pageLoadTimeoutTime, pageLoadTimeoutUnit);
        } catch (WebDriverException e) {
            // probably running Safari, currently doesn't support page timeouts.
            // https://code.google.com/p/selenium/issues/detail?id=6015
        }

    }

    @PostConstruct
    public void setWebDriverWindowSize() {

        Window window = webDriver.manage().window();
        window.setPosition(new Point(0, 0));
        window.maximize();

        // if (windowHeight <= 0 || windowWidth <= 0) {
        // webDriver.manage().window().maximize();
        // } else {
        // webDriver.manage().window().setSize(new Dimension(windowWidth, windowHeight));
        // }

    }

    @Bean
    public SeleniumConfig environment() {
        return new SeleniumConfig() {

            @Override
            public String baseurl() {
                // Address of the machine running Liferay
                return format("%s://%s:%s", serverProtocol, serverHost, serverPort);
            }
        };
    }

    @Configuration
    @Profile({"local"})
    public static class LocalFirefoxDriverConfiguration {

        @Value("${http.proxy.host:none}")
        private String proxyHost;

        @Value("${http.proxy.port:8080}")
        private int proxyPort;

        @Bean(destroyMethod = "quit")
        public FirefoxDriver firefoxDriver() {
            FirefoxProfile profile = new FirefoxProfile();

            // Force the use of native events to prevent focus issues
            profile.setEnableNativeEvents(true);

            boolean useProxy = !"none".equals(proxyHost);

            // Configure proxy if required
            if (useProxy) {

                profile.setPreference("network.proxy.type", FirefoxPreferences.MANUAL_PROXY_CONFIG);
                profile.setPreference("network.proxy.http", proxyHost);
                profile.setPreference("network.proxy.http_port", proxyPort);
                profile.setPreference("network.proxy.ssl", proxyHost);
                profile.setPreference("network.proxy.ssl_port", proxyPort);

            }

            // profile.setEnableNativeEvents(false);
            return new FirefoxDriver(profile);
        }

        @Bean
        public WebDriverEnvironment localEnvironment() {
            return new LocalWebDriverEnvironment();
        }

    }

    @Configuration
    @Profile({"htmlunit"})
    public static class HtmlUnitConfiguration {

        @Bean(destroyMethod = "quit")
        public HtmlUnitDriver htmlUnitDriver() {

            return new HtmlUnitDriver(INTERNET_EXPLORER_9);
        }

        @Bean
        public WebDriverEnvironment localEnvironment() {
            return new LocalWebDriverEnvironment();
        }

    }

    @PostConstruct
    public void handleFlakyIeDriver() {

        if (browser.equals(BrowserType.IE)) {
            // IE has some "random" intermittent exceptions which can be thrown when the
            // page is loading.
            // However, they are thrown as WebDriverException, which is a shame as it is the
            // parent of all exceptions!
            // This will mean it may take longer to fail for true errors, which shall be
            // reported as a wait timeout rather than being propogated sooner.
            fluentWait().ignoring(WebDriverException.class);
        }
    }

    @Configuration
    @Profile({"remote"})
    public static class RemoteDriverPostConfiguration {
        @Inject
        private RemoteFluentWait<WebDriver> wait;

        @PostConstruct
        public void referenceGridNodeOnTimeout() {
            wait.withEnvironment(remoteWebDriverEnvironment());
        }

        @Bean
        public WebDriverEnvironment remoteWebDriverEnvironment() {
            return new RemoteWebDriverEnvironment();
        }

    }

    /**
     * @author tim.myerscough
     *
     */
    @Configuration
    @Profile({"remote"})
    public static class RemoteDriverConfiguration {

        @Value("${driver.remote.hub.host:localhost}")
        private String seleniumGridHubHost;

        @Value("${driver.remote.capability.version:}")
        private String browserVersion;

        @Value("${driver.remote.capability.platform:ANY}")
        private Platform platform;

        @Value("${driver.remote.capability.browser:firefox}")
        private String browser;

        @Inject
        private List<FirefoxProfileConfigurer> ffProfileConfigs;

        @Bean
        public FileDetector fileDetector() {
            LocalFileDetector detector = new LocalFileDetector();
            return detector;
        }

        @Bean
        public ScrollBeforeClickEventListener scrollBeforeClickListener() {
            return new ScrollBeforeClickEventListener();
        }

        @Bean(destroyMethod = "quit")
        public WebDriver remoteDriver() {

            DesiredCapabilities capabilities;
            switch (browser) {
                case BrowserType.IE:
                    capabilities = DesiredCapabilities.internetExplorer();
                    capabilities.setCapability(InternetExplorerDriver.IE_ENSURE_CLEAN_SESSION, true);
                    capabilities.setCapability(InternetExplorerDriver.NATIVE_EVENTS, true);

                    // Uncomment for debug logging
                    // capabilities.setCapability(InternetExplorerDriver.LOG_LEVEL, "TRACE");
                    // capabilities.setCapability(InternetExplorerDriver.LOG_FILE,
                    // "C:\\selenium\\log\\log.out");

                    break;
                case BrowserType.CHROME:
                    capabilities = DesiredCapabilities.chrome();
                    break;
                case BrowserType.SAFARI:
                    capabilities = DesiredCapabilities.safari();
                    Map<String, Object> options = new HashMap<String, Object>();
                    options.put("cleanSession", Boolean.TRUE);
                    capabilities.setCapability(SafariOptions.CAPABILITY, options);
                    break;
                case SAFARI_WITH_ROBOT:
                    capabilities = new DesiredCapabilities("robotSafari", "", Platform.MAC);
                    break;

                case BrowserType.FIREFOX:
                default:
                    capabilities = DesiredCapabilities.firefox();
                    FirefoxProfile profile = new FirefoxProfile();

                    for (FirefoxProfileConfigurer configurer : ffProfileConfigs) {
                        configurer.configure(profile);
                    }

                    // Force the use of native events to prevent focus issues
                    profile.setEnableNativeEvents(true);

                    capabilities.setCapability(FirefoxDriver.PROFILE, profile);

            }

            capabilities.setPlatform(platform);
            capabilities.setVersion(browserVersion);

            try {
                // The remote server needs to be running Selenium standalone (or Grid)
                //
                // http://docs.seleniumhq.org/docs/03_webdriver.jsp#running-standalone-selenium-server-for-use-with-remotedrivers
                URL remoteAddress = new URL(format("http://%s:4444/wd/hub/", seleniumGridHubHost));
                // Remote Driver, e.g. on a VM
                RemoteWebDriver driver = new RemoteWebDriver(remoteAddress, capabilities);
                driver.setFileDetector(fileDetector());

                // Use the scroll listener to workaround Selenium bugs
                EventFiringWebDriver eventFiringDriver = new EventFiringWebDriver(driver);
                eventFiringDriver.register(scrollBeforeClickListener());
                return new Augmenter().augment(eventFiringDriver);

            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }

    }

}
