package anomaliesDetection.anomaliesReporting;

import anomaliesDetection.responsiveLayoutGraph.Node;
import org.openqa.selenium.WebDriver;

import java.util.HashSet;

public abstract class ResponsiveLayoutAnomaly {

    public abstract void captureScreenshotExample(int errorID, String url, WebDriver webDriver,  String timeStamp);

    public abstract HashSet<Node> getNodes();

    public abstract int[] getBounds();
}
