package trycb;

import com.couchbase.client.java.bucket.BucketType;
import com.couchbase.client.java.cluster.DefaultBucketSettings;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.testcontainers.containers.BrowserWebDriverContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.HttpWaitStrategy;
import trycb.testcontainers.CouchbaseContainer;
import trycb.testcontainers.CouchbaseWaitStrategy;
import trycb.testcontainers.LinkedContainer;

import java.io.File;

/**
 * Created by ldoguin on 12/26/16.
 */
public class TryCBJavaIT {

    public static final String clusterUser = "Administrator";
    public static final String clusterPassword = "password";

    public static CouchbaseContainer couchbaseContainer = new CouchbaseContainer()
            .withFTS(true)
            .withIndex(true)
            .withQuery(true)
            .withTravelSample(true)
            .withClusterUsername(clusterUser)
            .withClusterPassword(clusterPassword)
            .withNewBucket(DefaultBucketSettings.builder().enableFlush(true).name("default").quota(100).replicas(0).type(BucketType.COUCHBASE).build());

    static {
        couchbaseContainer.start();
        couchbaseContainer.getCouchbaseCluster().disconnect();
    }

    public static GenericContainer trycbBack = new LinkedContainer("trycb/java:latest")
            .withLinkToContainer(couchbaseContainer, "couchbase")
            .withExposedPorts(8080)
            .withCommand("-Dspring.couchbase.bootstrap-hosts="+couchbaseContainer.getContainerIpAddress())
            .waitingFor(new HttpWaitStrategy().forPath("/wut").forStatusCode(404));

    static {
        trycbBack.start();
    }

    public static GenericContainer trycbFront = new LinkedContainer("trycb/front:latest").withLinkToContainer(trycbBack, "trycbBack").withExposedPorts(80);

    static {
        trycbFront.start();
    }

    @ClassRule
    public static BrowserWebDriverContainer chrome = new BrowserWebDriverContainer()
            .withLinkToContainer(trycbFront, "trycbfront")
            .withLinkToContainer(trycbBack, "trycbback")
            .withDesiredCapabilities(DesiredCapabilities.chrome())
            .withRecordingMode(BrowserWebDriverContainer.VncRecordingMode.RECORD_ALL, new File("target"));

    @Test
    public void testTab() throws InterruptedException {
        RemoteWebDriver driver = chrome.getWebDriver();
        driver.get("http://trycbfront");

        WebElement usernameField = (new WebDriverWait(driver, 10))
                .until(ExpectedConditions.presenceOfElementLocated(By.name("username")));

        // SIGNUP
        usernameField.sendKeys("ld@cb.com");
        driver.findElementByName("password").sendKeys("password");
        driver.findElementByTagName("button").click();

        // Verify SIGNUP
        String textElement = (new WebDriverWait(driver, 10))
                .until(ExpectedConditions.presenceOfElementLocated(By.xpath(("/html/body/app-root/div/div[2]/div[1]/div/div[2]/app-home/div[1]/div/div[1]/div[1]/div/div/div[2]/div/small/strong")))).getText();
        Assert.assertEquals("Find a Flight", textElement);

        navigateToCart(driver);

        textElement = (new WebDriverWait(driver, 10))
                .until(ExpectedConditions.presenceOfElementLocated(By.xpath(("/html/body/app-root/div/div[2]/div[1]/div/div[2]/app-cart/div[1]/button")))).getText();
        Assert.assertEquals("Clear Cart", textElement);
    }

    public void navigateToFlights(RemoteWebDriver driver) {
        driver.findElementByXPath("/html/body/app-root/div/div[2]/div[1]/div/div[1]/div/app-navbar/div/div[3]/div/a[1]").click();
    }

    public void navigateToCart(RemoteWebDriver driver) {
        driver.findElementByXPath("/html/body/app-root/div/div[2]/div[1]/div/div[1]/div/app-navbar/div/div[3]/div/a[2]").click();
    }

    public void navigateToUser(RemoteWebDriver driver) {
        driver.findElementByXPath("/html/body/app-root/div/div[2]/div[1]/div/div[1]/div/app-navbar/div/div[3]/div/a[3]").click();
    }

    public void navigateToHotels(RemoteWebDriver driver) {
        driver.findElementByXPath("/html/body/app-root/div/div[2]/div[1]/div/div[1]/div/app-navbar/div/div[3]/div/a[4]").click();
    }
}

