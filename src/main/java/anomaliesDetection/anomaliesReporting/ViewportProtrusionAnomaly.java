package anomaliesDetection.anomaliesReporting;

import anomaliesDetection.layout.Element;
import anomaliesDetection.layout.LayoutFactory;
import anomaliesDetection.main.RLGExtractor;
import anomaliesDetection.responsiveLayoutGraph.Node;
import anomaliesDetection.utils.Utils;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.WebDriver;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

public class ViewportProtrusionAnomaly extends ResponsiveLayoutAnomaly {

    Node node;
    int min, max;

    public ViewportProtrusionAnomaly(Node n, int i, int key) {
        node = n;
        min = i;
        max = key;
    }

    @Override
    public void captureScreenshotExample(int errorID, String url, WebDriver webDriver, String timeStamp) {
        try {
            int captureWidth = (min + max) / 2;
            HashMap<Integer, LayoutFactory> lfs = new HashMap<>();

            BufferedImage img;
            img = RLGExtractor.getScreenshot(captureWidth, errorID, lfs, webDriver, url);
            LayoutFactory lf = lfs.get(captureWidth);
            Element e1 = lf.getElementMap().get(node.getxPath());
            Element body = lf.getElementMap().get("/HTML/BODY");

            Graphics2D g2d = img.createGraphics();
            g2d.setColor(Color.RED);
            g2d.setStroke(new BasicStroke(3));

            int[] coords = e1.getBoundingCoordinates();

            g2d.drawRect(coords[0], coords[1], coords[2] - coords[0], coords[3] - coords[1]);

            g2d.setColor(Color.GREEN);

            int[] coords2 = body.getBoundingCoordinates();

            g2d.drawRect(coords2[0], coords2[1], coords2[2] - coords2[0], coords2[3] - coords2[1]);

            g2d.dispose();
            File output = Utils.getOutputFilePath(url, timeStamp, errorID);
            FileUtils.forceMkdir(output);
            //ImageIO.write(img, "png", new File(output + "/viewportOverflowWidth" + captureWidth + ".png"));
            ImageIO.write(img, "png", new File(output + "/anomaly-" + errorID+"viewportOverflowWidth" + captureWidth + ".png"));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException npe) {
            System.out.println("Could not find one of the offending elements in screenshot.");
        }
    }

    @Override
    public HashSet<Node> getNodes() {
        HashSet<Node> nodes = new HashSet<>();
        nodes.add(node);
        return nodes;
    }

    @Override
    public int[] getBounds() {
        return new int[]{min, max};
    }

    public Node getNode() {
        return node;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    public String toString() {
        return node.getxPath() + " overflowed the viewport window between " + min + " and " + max;
    }

}
