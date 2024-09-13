package pdfvalidation;


import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.PageFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBuffer;

import commonutilities.utilities.Util;
import reportutilities.common.ReportCommon;
import reportutilities.extentmodel.ExtentUtilities;
import reportutilities.model.TestCaseParam;
import uitests.testng.common.ExcelUtility;

public class ReadPFDBox {
	private static final Logger logger =LoggerFactory.getLogger(ReadPFDBox.class.getName());
	private static String impagePath1 = "";
	private static String impagePath2 = "";
	ExtentUtilities extentUtilities = new ExtentUtilities();
	protected WebDriver driver ;
	ReportCommon exceptionDetails = new ReportCommon();
	ReportCommon testStepDetails = new ReportCommon();
	private static String statusPass="PASS";
	private static String statusFail="FAIL";
	
	Util util = new Util();
	
	ExcelUtility excelutil=new ExcelUtility();
	HashMap<String, ArrayList<String>> testcaseDataDBQScreen = new HashMap<>();
	public ReadPFDBox(){ }
	public ReadPFDBox(WebDriver wDriver) 
	{ 
		initializePage(wDriver);
	}
	
	public void initializePage(WebDriver wDriver) 
	{
		driver = wDriver;
		PageFactory.initElements(driver, this);
	
	}


	public static class PDFComparisonException extends Exception implements Serializable {
	    private static final long serialVersionUID = 1L; // You can use any unique long value

	    public PDFComparisonException(String message) {
	        super(message);
	    }

	    public PDFComparisonException(String message, Throwable cause) {
	        super(message, cause);
	    }
	}

	public void validatepdfvspdfCompareLine(TestCaseParam testCaseParam, String pdfSourceFile, String pdfTargetFile, String resultFile, String replacedTextFile, String targetTextFile, int count1) throws PDFComparisonException, InterruptedException {
	    try {

	        File file1 = new File(pdfSourceFile);
	        PDDocument document = PDDocument.load(file1);
	        File file2 = new File(pdfTargetFile);
	        PDDocument document2 = PDDocument.load(file2);
	        int totalpageofDocument1 = getPageCount(document);
	        int totalpageofDocument2 = getPageCount(document2);
	        if (totalpageofDocument1 == totalpageofDocument2) {
	            for (int i = 1; i < (totalpageofDocument1 - count1) + 1; i++) {
	                extracted(testCaseParam, resultFile, replacedTextFile, targetTextFile,  document2,
	                        totalpageofDocument2, i);
	            }
	        } else {
	            throw new PDFComparisonException("Page count for Source and Target PDF mismatched");
	        }
	    } catch (IOException e) {
	        throw new PDFComparisonException("Error loading PDF documents", e);
	    }
	}

	
	private void extracted(TestCaseParam testCaseParam, String resultFile, String replacedTextFile,
			String targetTextFile, PDDocument document2, int totalpageofDocument2, int i)
			throws IOException, InterruptedException {
		String action;
		String actionDescription;
		File directory;
		action = "Verify PDF Page"+i;
		actionDescription = "Verify PDF Page"+i;
		String action1 = "Verified Page"+i+":: Pass";
		String actionDescription1 = "Verified Page"+i+":: Pass";
		String testcontant2 = readPdfContent(document2,i,totalpageofDocument2-(totalpageofDocument2-i));
		
		directory = new File(targetTextFile);
		if (! directory.exists()){
		    directory.mkdirs();
		}
				
		Path path = Paths.get(targetTextFile).normalize();
        if (!Files.isWritable(path.getParent())) {
            throw new SecurityException("Invalid file path or insufficient permissions");
        }
		
		
		try(BufferedWriter out2 = new BufferedWriter(new FileWriter(path.toFile()+"\\TargetTextFile"+i+".txt",StandardCharsets.UTF_8)))
		{
			out2.write("");
			out2.write(testcontant2);
		
		}
		
		StringBuilder stringBuilder = new StringBuilder();

		
		try(BufferedReader br=new BufferedReader(new FileReader(replacedTextFile+"\\"+"ReplacedTextFile"+i+".txt",StandardCharsets.UTF_8)))
		{
			String replacedtxtlines=br.readLine();
			while(replacedtxtlines!=null) 
			{
				stringBuilder.append(replacedtxtlines);
				replacedtxtlines=br.readLine();
				
			}
		}
		
		
		
		Thread.sleep(6000);
		String testcontant1 = stringBuilder.toString().trim().replace("\\s", "").replace("\\n", "").replace("\\t", "");

		
		testcontant2=testcontant2.trim().replaceAll("\\s", "").replace("\\n", "").replace("\\t", "");
		if(!testcontant1.equalsIgnoreCase(testcontant2)){

			String targetTxtFile=targetTextFile+"\\TargetTextFile"+i+".txt";
	
			String replacedtxtFile=replacedTextFile+"\\ReplacedTextFile"+i+".txt";
			List<String> linenumber = textdiffutil(replacedtxtFile,targetTxtFile);
			directory = new File(resultFile);
		    if (! directory.exists())
		    {
		        directory.mkdirs();
		    }
		    try(BufferedWriter out = new BufferedWriter(new FileWriter(resultFile+"\\ResultFile"+i+".txt",StandardCharsets.UTF_8)))
		    {
		    	out.write("");
				out.write("Difference in lines of SourcefilePage"+i+"::\r\n"+linenumber);
				testStepDetails.logPDFDetails(testCaseParam, action+"\r\n"+linenumber, actionDescription+"\r\n"+linenumber, LocalDateTime.now(), statusFail,"Difference in lines of SourcefilePage"+i+"::\r\n"+linenumber);
				
		    }
			
		    logger.info("Source and Target Page :: Didn't match {}", i );
		}else {
			logger.info("Page are equal {} ", i);
			testStepDetails.logPDFDetailsPass(testCaseParam, action1, actionDescription1, LocalDateTime.now(), statusPass);
		}
	}
	
	public static List<String> textdiffutil(String path1, String path2) throws IOException {
	   
	    List<String> delta1 = new ArrayList<>();

	    try (FileReader fileReader1 = new FileReader(path1);
	         FileReader fileReader2 = new FileReader(path2)) 
	    {
	    	
	    	return delta1;
	    	
	    }
	}


	public static List<String> readLines(final Reader reader) throws IOException {
		final BufferedReader bufReader = toBufferedReader(reader);
		final List<String> list = new ArrayList<>();
		String line;
		while ((line = bufReader.readLine()) != null) {
			
			list.add(line.trim().replaceAll("\\s", "").replace("\\n", "").replace("\\t", ""));
		}
		return list;
	}
	public static BufferedReader toBufferedReader(final Reader reader) {
		return reader instanceof BufferedReader ? (BufferedReader) reader : new BufferedReader(reader);
	}
	public static String readPdfContent(PDDocument document,int firstpage, int lastpage) throws IOException {
		
		PDFTextStripper pdfStripper = new PDFTextStripper();
		pdfStripper.setStartPage(firstpage);
		pdfStripper.setEndPage(lastpage);
		String text = pdfStripper.getText(document);
		return text.trim().replaceAll("\\s", "").replace("\\n", "").replace("\\t", "");
	}
	public static int getPageCount(PDDocument doc) {
		
		return doc.getNumberOfPages();
	}
	
	public static boolean compareImage() { 
		try {
			File fileA = new File(impagePath1);
			File fileB = new File(impagePath2);
			
			BufferedImage biA = ImageIO.read(fileA);
			DataBuffer dbA = (DataBuffer) biA.getData().getDataBuffer();
			int sizeA = ((java.awt.image.DataBuffer) dbA).getSize(); 
			BufferedImage biB = ImageIO.read(fileB);
			DataBuffer dbB = (DataBuffer) biB.getData().getDataBuffer();
			int sizeB = ((java.awt.image.DataBuffer) dbB).getSize();
			
			if(sizeA == sizeB) {
				for(int i=0; i<sizeA; i++) { 
					if(((java.awt.image.DataBuffer) dbA).getElem(i) != ((java.awt.image.DataBuffer) dbB).getElem(i)) {
						return false;
					}
				}
				return true;
			}
			else {
				return false;
			}
		} 
		catch (Exception e) { 
			logger.info("Failed to compare image files ...");
			return false;
		}
	}
	public static float compareImagePer() {
		File fileA = new File(impagePath1);
		File fileB = new File(impagePath2);
		float percentage = 0;
		try {
			
			BufferedImage biA = ImageIO.read(fileA);
			DataBuffer dbA = (DataBuffer) biA.getData().getDataBuffer();
			int sizeA = ((java.awt.image.DataBuffer) dbA).getSize();
			BufferedImage biB = ImageIO.read(fileB);
			DataBuffer dbB = (DataBuffer) biB.getData().getDataBuffer();
			int sizeB = ((java.awt.image.DataBuffer) dbB).getSize();
			int count = 0;
			
			if (sizeA == sizeB) {
				for (int i = 0; i < sizeA; i++) {
					if (((java.awt.image.DataBuffer) dbA).getElem(i) == ((java.awt.image.DataBuffer) dbB).getElem(i)) {
						count = count + 1;
					}
				}
				percentage = (count * 100) / (float)sizeA;
			} else {
				logger.info("Both the images are not of same size");
			}
		} catch (Exception e) {
			logger.info("Failed to compare image files ...");
		}
		return percentage;
	}

	public static void pdfToHTML(String sourcepdf, String outputfile) throws IOException {
	    PDDocument document = PDDocument.load(new File(sourcepdf));
	    Writer output = new PrintWriter(outputfile, "utf-8");
	    output.close();
	    document.close();
	}
}