package anomaliesDetection.utils;

import anomaliesDetection.anomaliesReporting.ResponsiveLayoutAnomaly;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;

public class PDFUtils {

    private static Font smallBold = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.BOLD);
    private static Font normalFont = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.NORMAL);
    private static Font subFont = new Font(Font.FontFamily.TIMES_ROMAN, 16, Font.BOLD);
    private static Font catFont = new Font(Font.FontFamily.TIMES_ROMAN, 18, Font.BOLD);


    public static void generatePDFReport(File outputFile, ArrayList<ResponsiveLayoutAnomaly> errors){
        try {
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(outputFile.toString() + "/AnomaliesReport.pdf"));
            document.open();
            PDFUtils.addMetaData(document);
            PDFUtils.addTitlePage(document);
            PDFUtils.addContent(document, errors);
            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void addMetaData(Document document) {
        document.addTitle("Anomalies Detection PDF");
        document.addAuthor("Sajra Muratovic");
        document.addCreator("Sajra Muratovic");
    }

    public static void addTitlePage(Document document) throws DocumentException {
        Paragraph preface = new Paragraph();
        // We add one empty line
        addEmptyLine(preface, 1);
        // Lets write a big header
        preface.add(new Paragraph("Automatic Anomalies Detection Report", catFont));

        addEmptyLine(preface, 1);
        // Will create: Report generated by: _name, _date
        preface.add(new Paragraph("Report generated by: " + System.getProperty("user.name") + ", " + new Date(), smallBold));
        addEmptyLine(preface, 3);
        preface.add(new Paragraph("This document describes anomalies that are detected on specific web page.", smallBold));
        document.add(preface);
        // Start a new page
        document.newPage();
    }

    public static void addContent(Document document, ArrayList<ResponsiveLayoutAnomaly> responsiveLayoutAnomalies) throws DocumentException {
        Paragraph anomaliesList = new Paragraph();
        addEmptyLine(anomaliesList, 1);
        anomaliesList.add(new Paragraph("Responsive Layout Anomalies List", subFont));
        addEmptyLine(anomaliesList, 3);

        if (responsiveLayoutAnomalies.size() > 0) {
            for (ResponsiveLayoutAnomaly responsiveLayoutAnomaly : responsiveLayoutAnomalies) {
                anomaliesList.add(new Paragraph("Anomaly: ", smallBold));
                addEmptyLine(anomaliesList, 1);
                anomaliesList.add(new Paragraph(responsiveLayoutAnomaly.toString(), normalFont));
                addEmptyLine(anomaliesList, 2);
            }
        } else {
            anomaliesList.add(new Paragraph("NO FAULTS DETECTED.", smallBold));
        }
        document.add(anomaliesList);
    }

    private static void addEmptyLine(Paragraph paragraph, int number) {
        for (int i = 0; i < number; i++) {
            paragraph.add(new Paragraph(" "));
        }
    }
}
