package anomaliesDetection.anomaliesReporting;

import anomaliesDetection.layout.Element;
import anomaliesDetection.layout.LayoutFactory;
import anomaliesDetection.main.RLGExtractor;
import anomaliesDetection.main.Utils;
import anomaliesDetection.responsiveLayoutGraph.AlignmentConstraint;
import anomaliesDetection.responsiveLayoutGraph.Node;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.WebDriver;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

public class CollisionFailure extends ResponsiveLayoutFailure {

    // Instance variable representing the colliding constraint
    private AlignmentConstraint constraint;

    public CollisionFailure(AlignmentConstraint con) {
        constraint = con;
    }

    /**
     * @return String describing the failure for either console or text file printing
     */
    public String toString() {
        return "ELEMENTS " + constraint.getNode1().getxPath() + " AND " + constraint.getNode2().getxPath() + " ARE OVERLAPPING BETWEEN " + constraint.getMin() + " AND " + constraint.getMax();
    }

    /**
     * Captures a screenshot of the failure, highlights the colliding elements and then saves it to disk
     *
     * @param errorID   The error ID of the failure to uniquely identify it
     * @param url       The URL of the webpage under test
     * @param webDriver The WebDriver object currently rendering the page
     * @param fullUrl   The full file path used to save the image in the correct place
     * @param timeStamp The time stamp of the tool execution to uniquely identify different full test reports
     */
    @Override
    public void captureScreenshotExample(int errorID, String url, WebDriver webDriver, String fullUrl, String timeStamp) {
        try {
            int captureWidth = (constraint.getMin() + constraint.getMax()) / 2;

            // Layout factory to store the DOM
            HashMap<Integer, LayoutFactory> lfs = new HashMap<>();

            // Capture the image and the DOM
            BufferedImage img = RLGExtractor.getScreenshot(captureWidth, errorID, lfs, webDriver, fullUrl);

            // Get the coordinates of the two colliding elements
            LayoutFactory lf = lfs.get(captureWidth);
            Element e1 = lf.getElementMap().get(constraint.getNode1().getxPath());
            int[] coords1 = e1.getBoundingCoordinates();
            Element e2 = lf.getElementMap().get(constraint.getNode2().getxPath());
            int[] coords2 = e2.getBoundingCoordinates();

            // Set up Graphics@d object so the elements can be highlighted
            Graphics2D g2d = img.createGraphics();

            // Highlight the two elements in different colours
            g2d.setColor(Color.RED);
            g2d.setStroke(new BasicStroke(3));
            g2d.drawRect(coords1[0], coords1[1], coords1[2] - coords1[0], coords1[3] - coords1[1]);
            g2d.setColor(Color.CYAN);
            g2d.drawRect(coords2[0], coords2[1], coords2[2] - coords2[0], coords2[3] - coords2[1]);
            g2d.dispose();

            // Set up the output file
            File output = Utils.getOutputFilePath(url, timeStamp, errorID);

            // Make sure the output directory exists
            FileUtils.forceMkdir(output);

            // Write the highlighted screenshot to file
            ImageIO.write(img, "png", new File(output + "/overlapWidth" + captureWidth + ".png"));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException npe) {
            System.out.println("Could not find one of the offending elements in screenshot.");
        }
    }

    /**
     * This method returns the two colliding elements as a HashMap
     *
     * @return HashMap containing the two elements
     */
    @Override
    public HashSet<Node> getNodes() {
        HashSet<Node> nodes = new HashSet<>();
        nodes.add(constraint.getNode1());
        nodes.add(constraint.getNode2());
        return nodes;
    }

    /**
     * Returns the lower and upper bounds of the constraint
     *
     * @return Array containing the bounds
     */
    @Override
    public int[] getBounds() {
        return new int[]{constraint.getMin(), constraint.getMax()};
    }

    /**
     * Accessor for the constraint
     *
     * @return the colliding constraint
     */
    public AlignmentConstraint getConstraint() {
        return constraint;
    }
}
