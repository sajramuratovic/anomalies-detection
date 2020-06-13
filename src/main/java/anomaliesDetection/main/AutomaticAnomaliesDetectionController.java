package anomaliesDetection.main;

import anomaliesDetection.layout.LayoutFactory;
import anomaliesDetection.utils.AlertHelper;
import anomaliesDetection.utils.Utils;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.stage.Window;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import xPert.DomNode;
import xPert.JsonDomParser;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AutomaticAnomaliesDetectionController {

    //Web Page url
    public String url;
    private String sampleTechnique = "uniformBP";
    static String scriptToExtract;
    //the browser to use for the test run
    private String browser = "chrome";

    // start and end width for sampling
    private int startWidth = 320;
    private int finalWidth = 1400;
    // step size for sampling
    private int stepSize = 60;
    static int sleep = 50;
    static HashMap<Integer, DomNode> oracleDoms;

    //Use binary search or not
    private boolean binarySearch = true;

    //Whether to run the baseline approaches
    private boolean baselines;
    private static int browserHeight = 600;

    static String anomalies = "/Users/sajram/Desktop/MagistarskiImplementacija/anomalies-detection/";

    public AutomaticAnomaliesDetectionController() {
        java.awt.Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        double width = screenSize.getWidth();
        double height = screenSize.getHeight();
        if (finalWidth > width) {
            finalWidth = (int) width;
        }
    }


    @FXML
    private javafx.scene.control.TextField webPageURL;

    @FXML
    private javafx.scene.control.Button btnClick;

    @FXML
    private Button closeApplicationButton;

    @FXML
    void report(ActionEvent event) throws IOException {

        Window owner = btnClick.getScene().getWindow();
        if (webPageURL.getText().isEmpty()) {
            AlertHelper.showAlert(Alert.AlertType.ERROR, owner, "Error!", "Please enter Web Page URL!");
            return;
        }
        if (!verifyUrl(webPageURL.getText())) {
            AlertHelper.showAlert(Alert.AlertType.ERROR, owner, "Error!", "Please enter a valid Web Page URL!");
            return;
        }

        try {
            setUp(webPageURL.getText());
        } catch (IOException e) {
            e.printStackTrace();
            AlertHelper.showAlert(Alert.AlertType.ERROR, owner, "Error!", "Some error occurred, please try again!");
        }

        AlertHelper.showAlert(Alert.AlertType.INFORMATION, owner, "Anomalies Detection Report",
                "Report and a series of highlighted screenshots are successfully saved in the reports directory of your AnomaliesDetection installation!");

    }

    @FXML
    void closeApplication(ActionEvent event) {
        Platform.exit();
    }

    public void setUp(String webPageUrl) throws IOException {

        String current = new java.io.File(".").getCanonicalPath();
        System.setProperty("webdriver.chrome.driver", current + "/resources/chromedriver.exe");
        url = webPageUrl;
        scriptToExtract = Utils.readFile(current + "/resources/webdiff2.js");

        try {
            Date date = new Date();
            Format formatter = new SimpleDateFormat("YYYY-MM-dd_hh-mm-ss");
            String timeStamp = formatter.format(date);
            RLGExtractor extractor = new RLGExtractor(current, url, oracleDoms, browser, sampleTechnique,
                    binarySearch, startWidth, finalWidth, stepSize, null, sleep, timeStamp, baselines);
            extractor.extract();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * This method samples the DOM of a webpage at a set of viewports, and saves the DOMs into a HashMap
     *
     * @param url        The url of the webpage
     * @param widths     The viewport widths to sample
     * @param domStrings
     */
    public static void capturePageModel(String url, int[] widths, boolean saveDom, WebDriver webDriver, HashMap<Integer,
            LayoutFactory> layoutFactories, HashMap<Integer, String> domStrings) {
        // Create a parser for the DOM strings
        JsonDomParser parser = new JsonDomParser();
        File domFile = null;
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
                webDriver.manage().window().setSize(new org.openqa.selenium.Dimension(w, browserHeight));
                String previous = "";


                while (!consecutiveMatches) {
                    // Extract the DOM and save it to the HashMap.
                    String extractedDom = extractDOM(webDriver, scriptToExtract);
                    if (previous.equals(extractedDom)) {

                        layoutFactories.put(w, new LayoutFactory(extractedDom));
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

    public static String extractDOM(WebDriver webDriver, String script) throws IOException {
        return (String) ((JavascriptExecutor) webDriver).executeScript(script);
    }

    private boolean verifyUrl(String url) {
        String urlRegex = "^(http|https)://[-a-zA-Z0-9+&@#/%?=~_|,!:.;]*[-a-zA-Z0-9+@#/%=&_|]";
        Pattern pattern = Pattern.compile(urlRegex);
        Matcher m = pattern.matcher(url);
        if (m.matches()) {
            return true;
        } else {
            return false;
        }
    }
}
