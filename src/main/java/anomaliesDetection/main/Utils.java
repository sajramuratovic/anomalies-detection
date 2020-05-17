package anomaliesDetection.main;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.Screenshot;
import ru.yandex.qatools.ashot.screentaker.ViewportPastingStrategy;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Utils {

    /**
     * Utility function which reads the contents of a file into a String variable
     *
     * @param fileName the file name to read
     * @return a string containing the files contents.
     * @throws IOException
     */
    public static String readFile(String fileName) throws IOException {
        try {
            BufferedReader br = new BufferedReader(new FileReader(fileName));
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append("\n");
                line = br.readLine();
            }
            br.close();
            return sb.toString();
        } catch (IOException e) {
            return "";
        } finally {

        }
    }

    @SuppressWarnings("")
    public static BufferedImage getScreenshot(String url, int w, int sleep, WebDriver d, int errorID) {
        d.manage().window().setSize(new Dimension(w, 600));
        Screenshot screenshot = null;
        screenshot = new AShot().shootingStrategy(new ViewportPastingStrategy(500)).takeScreenshot(d);
        BufferedImage image;
        image = screenshot.getImage();

        return image;
    }

    public static File getOutputFilePath(String url, String timeStamp, int errorID) {

        File output = null;
        if (!url.contains("www.") && (!url.contains("http://"))) {
            String[] splits = url.split("/");
            String webpage = splits[0];
            String mutant = "index-" + timeStamp;
            try {
                output = new File(new File(".").getCanonicalPath() + "/../reports/" + webpage + "/" + mutant + "/fault" + errorID + "/");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (url.contains("http://")) {
            String[] splits = url.split("http://");
            String webpage = splits[1];
            String mutant = timeStamp;
            try {
                output = new File(new java.io.File(".").getCanonicalPath() + "/../reports/" + webpage + "/" + mutant + "/fault" + errorID + "/");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            String[] splits = url.split("www.");
            String webpage = splits[1];
            String mutant = timeStamp;
            try {
                output = new File(new File(".").getCanonicalPath() + "/../reports/" + webpage + "/" + mutant + "/fault" + errorID + "/");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return output;
    }

}
