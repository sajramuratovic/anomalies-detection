package anomaliesDetection.anomaliesReporting;

import anomaliesDetection.layout.Element;
import anomaliesDetection.layout.LayoutFactory;
import anomaliesDetection.main.RLGExtractor;
import anomaliesDetection.responsiveLayoutGraph.AlignmentConstraint;
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

public class ElementCollisionAnomaly extends ResponsiveLayoutAnomaly {

    // Instance variable representing the colliding constraint
    private AlignmentConstraint constraint;

    public ElementCollisionAnomaly(AlignmentConstraint alignmentConstraint) {
        constraint = alignmentConstraint;
    }

    @Override
    public void captureScreenshotExample(int errorID, String url, WebDriver webDriver, String timeStamp) {
        try {
            int captureWidth = (constraint.getMin() + constraint.getMax()) / 2;

            HashMap<Integer, LayoutFactory> lfs = new HashMap<>();

            BufferedImage img = RLGExtractor.getScreenshot(captureWidth, errorID, lfs, webDriver, url);

            LayoutFactory lf = lfs.get(captureWidth);
            Element e1 = lf.getElementMap().get(constraint.getNode1().getxPath());
            int[] coords1 = e1.getBoundingCoordinates();
            Element e2 = lf.getElementMap().get(constraint.getNode2().getxPath());
            int[] coords2 = e2.getBoundingCoordinates();

            setUpGraphicsObject(img.createGraphics(), coords1, coords2);

            File output = Utils.getOutputFilePath(url, timeStamp, errorID);

            FileUtils.forceMkdir(output);

            ImageIO.write(img, "png", new File(output + "/overlapWidth" + captureWidth + ".png"));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException npe) {
            System.out.println("Could not find one of the offending elements in screenshot.");
        }
    }

    @Override
    public HashSet<Node> getNodes() {
        HashSet<Node> nodes = new HashSet<>();
        nodes.add(constraint.getNode1());
        nodes.add(constraint.getNode2());
        return nodes;
    }

    @Override
    public int[] getBounds() {
        return new int[]{constraint.getMin(), constraint.getMax()};
    }

    private void setUpGraphicsObject(Graphics2D graphics2D, int[] coords1, int[] coords2) {

        Graphics2D g2d = graphics2D;

        g2d.setColor(Color.RED);
        g2d.setStroke(new BasicStroke(3));
        g2d.drawRect(coords1[0], coords1[1], coords1[2] - coords1[0], coords1[3] - coords1[1]);
        g2d.setColor(Color.CYAN);
        g2d.drawRect(coords2[0], coords2[1], coords2[2] - coords2[0], coords2[3] - coords2[1]);
        g2d.dispose();
    }


    public AlignmentConstraint getConstraint() {
        return constraint;
    }

    public void setConstraint(AlignmentConstraint constraint) {
        this.constraint = constraint;
    }

    public String toString() {
        return "ELEMENTS " + constraint.getNode1().getxPath() + " AND " + constraint.getNode2().getxPath() + " ARE OVERLAPPING BETWEEN " + constraint.getMin() + " AND " + constraint.getMax();
    }
}
