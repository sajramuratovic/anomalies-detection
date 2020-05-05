package anomaliesDetection;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

public class AutomaticAnomaliesDetection {

    private WebDriver webDriver;

    public static void main(String args[])
    {
//        anomaliesDetection.AutomaticAnomaliesDetection automaticAnomaliesDetection = new anomaliesDetection.AutomaticAnomaliesDetection();
//        automaticAnomaliesDetection.setUp();
        System.out.println("Hello World!");
    }

    public void setUp(){
        System.setProperty("webdriver.chrome.driver", "resources/chromedriver.exe");
        webDriver = new ChromeDriver();
        //launch the browser
        webDriver.get("https://www.google.com/");
        //webDriver.manage().window().maximize();
        //webDriver.manage().window().fullscreen();
        webDriver.manage().window().setSize(new Dimension(375, 812));
        System.out.println(webDriver.getTitle());
    }

}
