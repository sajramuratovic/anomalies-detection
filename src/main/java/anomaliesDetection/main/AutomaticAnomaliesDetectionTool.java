package anomaliesDetection.main;

import anomaliesDetection.layout.LayoutFactory;
import anomaliesDetection.utils.StopwatchFactory;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import java.util.HashMap;

public class AutomaticAnomaliesDetectionTool {

    private WebDriver webDriver;
    static int sleep = 50;

    public static void main(String args[])
    {
//        anomaliesDetection.main.AutomaticAnomaliesDetection automaticAnomaliesDetection = new anomaliesDetection.main.AutomaticAnomaliesDetection();
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

    public static void capturePageModel(String url, int[] widths, int sleep, boolean takeScreenshot, boolean saveDom, WebDriver wdriver, StopwatchFactory swf, HashMap<Integer, LayoutFactory> lFactories, HashMap<Integer, String> domStrings) {
        // Create a parser for the DOM strings
    }

    }
