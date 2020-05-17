package anomaliesDetection.main;

import anomaliesDetection.layout.LayoutFactory;
import anomaliesDetection.utils.StopwatchFactory;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import xPert.DomNode;
import xPert.JsonDomParser;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class AutomaticAnomaliesDetectionTool {

    private WebDriver webDriver;
    //public String current;
    public String url;

    static int sleep = 50;
    static HashMap<Integer, DomNode> oracleDoms;
    private String sampleTechnique = "uniformBP";
    private boolean binarySearch = true;
    private int startWidth;
    private int finalWidth;
    private int stepSize;
    private boolean baselines;
    private static int browserHeight;

    static String scriptToExtract;
    private String browser;

    private CommandLineParser commandLineParser = new CommandLineParser();

    static String anomalies = "/Users/sajram/Desktop/MagistarskiImplementacija/anomalies-detection/";

    public AutomaticAnomaliesDetectionTool(){
        startWidth = commandLineParser.startWidth;
        finalWidth = commandLineParser.endWidth;

        java.awt.Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        double width = screenSize.getWidth();
        double height = screenSize.getHeight();
        if (finalWidth > width) {
            finalWidth = (int) width;
        }
        browserHeight = 600;

        if (commandLineParser.ss != -1) {
            stepSize = commandLineParser.ss;
        }

        sampleTechnique = commandLineParser.sampling;
        binarySearch = commandLineParser.binary;
        baselines = commandLineParser.baselines;
        browser = commandLineParser.browser;
        url = commandLineParser.url;
    }

    public static void main(String args[]) throws IOException {
        anomaliesDetection.main.AutomaticAnomaliesDetectionTool automaticAnomaliesDetection = new anomaliesDetection.main.AutomaticAnomaliesDetectionTool();
        automaticAnomaliesDetection.setUp();
    }

    public void setUp() throws IOException {
        String current = new java.io.File( "." ).getCanonicalPath();
        System.setProperty("webdriver.chrome.driver", current + "/resources/chromedriver.exe");

        //url = "https://www.pdfescape.com/windows/";
        System.out.println(current);
        scriptToExtract = Utils.readFile(current +"/resources/webdiff2.js");

        try {
            Date date = new Date();
            Format formatter = new SimpleDateFormat("YYYY-MM-dd_hh-mm-ss");
            String timeStamp = formatter.format(date);
            RLGExtractor extractor = new RLGExtractor(current, url, url, oracleDoms, browser, sampleTechnique, binarySearch, startWidth, finalWidth, stepSize, null, sleep, timeStamp, baselines);

            extractor.extract();
        }catch (IOException e) {
            e.printStackTrace();
        }

    }


/*    public static void detectAnomalies(String current, String url, String browser, String sampleTechnique, boolean binarySearch,
                                       int startWidth, int finalWidth, int stepSize, boolean baselines) {

    }*/

    /**
     * This method samples the DOM of a webpage at a set of viewports, and saves the DOMs into a HashMap
     * @param url        The url of the webpage
     * @param widths    The viewport widths to sample
     * @param domStrings
     */
    public static void capturePageModel(String url, int[] widths, int sleep, boolean takeScreenshot, boolean saveDom, WebDriver wdriver, StopwatchFactory swf, HashMap<Integer, LayoutFactory> lFactories, HashMap<Integer, String> domStrings) {
        // Create a parser for the DOM strings
        JsonDomParser parser = new JsonDomParser();
        File domFile=null;
        try {
            // Set up storage directory
            String outFolder = "";

            if (saveDom) {
                String[] splits = url.split("/");
                outFolder = anomalies + "output/" + splits[7] + "/" + splits[8];
                File dir = new File(outFolder);
                FileUtils.forceMkdir(dir);
            }

            // Iterate through all viewport widths
            for (int w : widths) {
                // Check if DOM already saved for speed
                domFile = new File(outFolder + "/" + w + ".js");
                boolean consecutiveMatches = false;

                // Resize the browser window
                wdriver.manage().window().setSize(new Dimension(w, browserHeight));
                String previous = "";


                while (!consecutiveMatches) {
                    // Extract the DOM and save it to the HashMap.
                    String extractedDom = extractDOM(wdriver, scriptToExtract);
                    if (previous.equals(extractedDom)) {

                        lFactories.put(w, new LayoutFactory(extractedDom));
                        domStrings.put(w, extractedDom);
                        if (saveDom) {
                            FileUtils.writeStringToFile(domFile, extractedDom);
                        }
                        consecutiveMatches = true;
                    } else {
                        previous = extractedDom;
                    }


                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String extractDOM(WebDriver cdriver, String script) throws IOException {
        return (String) ((JavascriptExecutor) cdriver).executeScript(script);
    }
}
