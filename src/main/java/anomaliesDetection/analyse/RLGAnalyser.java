package anomaliesDetection.analyse;

import anomaliesDetection.anomaliesReporting.ResponsiveLayoutFailure;
import anomaliesDetection.layout.LayoutFactory;
import anomaliesDetection.responsiveLayoutGraph.AlignmentConstraint;
import anomaliesDetection.responsiveLayoutGraph.Node;
import anomaliesDetection.responsiveLayoutGraph.ResponsiveLayoutGraph;
import com.google.common.collect.HashBasedTable;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

public class RLGAnalyser {

    ResponsiveLayoutGraph responsiveLayoutGraph;
    ArrayList<ResponsiveLayoutFailure> errors;
    WebDriver driver;
    String url;
    ArrayList<Integer> bpoints;
    ArrayList<Node> onePixelOverflows;
    HashMap<Integer, LayoutFactory> layouts;
    int vmin, vmax;

    public RLGAnalyser(ResponsiveLayoutGraph r, WebDriver webDriver, String fullUrl, ArrayList<Integer> breakpoints, HashMap<Integer, LayoutFactory> lFactories, int vmin, int vmax) {
        responsiveLayoutGraph = r;
        driver = webDriver;
        url = fullUrl;
        bpoints = breakpoints;
        onePixelOverflows = new ArrayList<>();
        layouts = lFactories;
        this.vmin = vmin;
        this.vmax = vmax;
        errors = new ArrayList<>();
    }

    public ArrayList<ResponsiveLayoutFailure> analyse() {

        checkForViewportOverflows(responsiveLayoutGraph.getNodes());
        detectOverflowOrOverlap(responsiveLayoutGraph.getAlignmentConstraints());
        checkForSmallRanges(responsiveLayoutGraph.getAlignmentConstraints());
        checkForWrappingElements();

        return errors;
    }

    public void checkForViewportOverflows(HashMap<String, Node> nodes) {
        //TODO
    }

    public void detectOverflowOrOverlap(HashBasedTable<String, int[], AlignmentConstraint> alignmentConstraints) {
        //TODO
    }

    public void checkForSmallRanges(HashBasedTable<String, int[], AlignmentConstraint> alignmentConstraints) {
        //TODO
    }

    private void checkForWrappingElements() {
        //TODO
    }

    public void writeReport(String url, ArrayList<ResponsiveLayoutFailure> errors, String ts) {
        PrintWriter output = null;
        PrintWriter output2 = null;
        PrintWriter output3 = null;
        try {
            File outputFile = null;
            if (!url.contains("www.") && (!url.contains("http://"))) {
                String[] splits = url.split("/");
                String webpage = splits[0];
                String mutant = "index-" + ts;
                //                    splits[1];
                try {
                    outputFile = new File(new File(".").getCanonicalPath() + "/../reports/" + webpage + "/" + mutant + "/");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (url.contains("http://")) {
                String[] splits = url.split("http://");
                String webpage = splits[1];
                String mutant = ts;
                try {
                    outputFile = new File(new java.io.File(".").getCanonicalPath() + "/../reports/" + webpage + "/" + mutant + "/");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else {
                String[] splits = url.split("www.");
                String webpage = splits[1];
                String mutant = ts;
                try {
                    outputFile = new File(new File(".").getCanonicalPath() + "/../reports/" + webpage + "/" + mutant + "/");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            FileUtils.forceMkdir(outputFile);
            File dir = new File(outputFile+"/fault-report.txt");
//            File countDir = new File(outputFile + "/error-count.txt");
//            File typeFile = new File(outputFile + "/error-types.txt");
            File classification = new File(outputFile + "/classification.txt");
            File actualFaultsFile = new File(outputFile + "/../actual-fault-count.txt");
//            classification.createNewFile();
//            actualFaultsFile.createNewFile();
            output = new PrintWriter(dir);
//            output2 = new PrintWriter(countDir);
//            output3 = new PrintWriter(typeFile);
            if (errors.size() > 0) {
//                output2.append(Integer.toString(errors.size()));
                for (ResponsiveLayoutFailure rle : errors) {
                    output.append(rle.toString() + "\n\n");
//                    output3.append(errorToKey(rle) + "\n");
                }
            } else {
                output.append("NO FAULTS DETECTED.");
//                output2.append("0");
            }

            output.close();
//            output2.close();
//            output3.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
