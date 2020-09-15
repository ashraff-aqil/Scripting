import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

import com.ephesoft.dcma.script.IJDomScript;
import com.ephesoft.dcma.util.logger.EphesoftLogger;
import com.ephesoft.dcma.util.logger.ScriptLoggerFactory;
import com.ephesoft.dcma.core.component.ICommonConstants;
import com.ephesoft.dcma.util.ApplicationConfigProperties;


/**
 * The <code>ScriptAutomaticValidation</code> class represents the script execute structure. Writer of scripts plug-in should implement
 * this IScript interface to execute it from the scripting plug-in. Via implementing this interface writer can change its java file at
 * run time. Before the actual call of the java Scripting plug-in will compile the java and run the new class file.
 * 
 * @author Ephesoft
 * @version 1.0
 */
public class ScriptAutomaticValidation implements IJDomScript {

	private static EphesoftLogger LOGGER = ScriptLoggerFactory.getLogger(ScriptAutomaticValidation.class);
	private static String DOCUMENT = "Document";
	private static String DOCUMENTS = "Documents";
	private static String DOCUMENT_LEVEL_FIELDS = "DocumentLevelFields";
	private static String TRUE = "true";
	private static String FALSE = "false";
	private static String TYPE = "Type";
	private static String VALUE = "Value";
	private static String BATCH_LOCAL_PATH = "BatchLocalPath";
	private static String BATCH_INSTANCE_ID = "BatchInstanceIdentifier";
	private static String EXT_BATCH_XML_FILE = "_batch.xml";
	private static String VALID = "Valid";
	private static String PATTERN = "dd/MM/yyyy";
	private static String DATE = "DATE";
	private static String LONG = "LONG";
	private static String DOUBLE = "DOUBLE";
	private static String STRING = "STRING";
	private static String ZIP_FILE_EXT = ".zip";

	public static void main(String args[]) {

		// Define a path to the Batch XML.
		String filePath = "C:\\Users\\Lenovo\\Documents\\Hume\\Cement\\Customer Onboarding\\Scripting\\Test XML\\F49_HiTech.xml";
		try {
			SAXBuilder sb = new SAXBuilder();
			Document doc = sb.build(filePath);
			// Note that the name of the script is being used for the execute
			ScriptAutomaticValidation se = new ScriptAutomaticValidation();
			se.execute(doc, null, null);
		} catch (Exception x) {
			LOGGER.error(x.getMessage());
		}
	}

	/**
	 * The <code>execute</code> method will execute the script written by the writer at run time with new compilation of java file. It
	 * will execute the java file dynamically after new compilation.
	 * 
	 * @param document {@link Document}
	 */
	public Object execute(Document document, String methodName, String documentIdentifier) {
		Exception exception = null;
		try {
			LOGGER.info("*************  Inside ScriptAutomaticValidation scripts.");

			LOGGER.info("*************  Start execution of ScriptAutomaticValidation scripts.");

			if (null == document) {
				LOGGER.error("Input document is null.");
			}
			boolean isWrite = true;
			Element validNode = null;
			String valueText = null;
			String typeText = null;
			Element documents = document.getRootElement().getChild(DOCUMENTS);
			List<?> documentList = documents.getChildren(DOCUMENT);
			if (null != documentList) {
				for (int index = 0; index < documentList.size(); index++) {
					Element documentNode = (Element) documentList.get(index);
					if (null == documentNode) {
						continue;
					}
					List<?> childNodeList = documentNode.getChildren();
					if (null == childNodeList) {
						continue;
					}
					validNode = null;
					outerloop: for (int y = 0; y < childNodeList.size(); y++) {
						Element childDoc = (Element) childNodeList.get(y);
						if (null == childDoc) {
							continue;
						}
						String nodeName = childDoc.getName();
						if (null == nodeName) {
							continue;
						}
						if (nodeName.equals(VALID)) {
							validNode = childDoc;
						} else {
							if (nodeName.equals(DOCUMENT_LEVEL_FIELDS)) {
								List<?> dlfNodeList = childDoc.getChildren();
								if (null == dlfNodeList) {
									continue;
								}
								for (int dlf = 0; dlf < dlfNodeList.size(); dlf++) {
									Element dlfDoc = (Element) dlfNodeList.get(dlf);
									if (null == dlfDoc) {
										continue;
									}
									List<?> dlfValueNodeList = dlfDoc.getChildren();
									if (null == dlfValueNodeList) {
										continue;
									}
									valueText = null;
									typeText = null;
									for (int x = 0; x < dlfValueNodeList.size(); x++) {
										Element dlfValueDoc = (Element) dlfValueNodeList.get(x);
										if (null == dlfValueDoc) {
											continue;
										}
										String nName = dlfValueDoc.getName();
										if (nName.equals(VALUE)) {
											valueText = dlfValueDoc.getText();
										} else {
											if (nName.equals(TYPE)) {
												typeText = dlfValueDoc.getText();
											}
										}
										if (null != typeText) {
											boolean isValid = checkValueText(valueText, typeText);
											if (isValid) {
												// validNode.setText(TRUE);
												break;
											} else {
												if (null != validNode) {
													validNode.setText(FALSE);
												}
												break outerloop;
											}
										}
									}
								}
							}
						}

					}
				}
				swapValue(document);
				checkPageExtracted(document);
				handleDate(document);
				handleDateF49(document);
				getTableRuleName(document);
				takeAmountOnly(document);
				// Write the document object to the xml file. Currently following IF block is commented for performance improvement.
				/*if (isWrite) {					
					writeToXML(document);
					LOGGER.info("*************  Successfully write the xml file for the ScriptAutomaticValidation scripts.");
				}*/
			}
			LOGGER.info("*************  End execution of the ScriptAutomaticValidation scripts.");
		} catch (Exception e) {
			LOGGER.error("*************  Error occurred in scripts." + e.getMessage());
			e.printStackTrace();
			exception = e;
		}
		return exception;
	}

	private void handleDateF49(Document document){
		Element root = document.getRootElement();
		List<Element> docs = root.getChild("Documents").getChildren("Document");

		for(Element doc:docs){
			if(doc.getChildText("Type").equalsIgnoreCase("Form49")){
				List<Element> dlfs = doc.getChild("DocumentLevelFields").getChildren("DocumentLevelField");
				for(Element dlf : dlfs) {
					String f49date = "";
					//Guarantee Date
					if (dlf.getChildText("Name").equalsIgnoreCase("Date")) {
						try {
							System.out.println("In F49 Date");
							f49date = dlf.getChildText("Value").trim().toLowerCase();
							f49date = returnConvertedDate(f49date, dlf);
							System.out.println("Returned from function " + f49date);
						} catch (Exception e) {
							System.out.println("Error in F49 Date");
						}
					}
				}
			}
		}
	}

	private void takeAmountOnly(Document document){
		Element root = document.getRootElement();
		List<Element> docs = root.getChild("Documents").getChildren("Document");

		for(Element doc: docs){
			List<Element> dlfs = doc.getChild("DocumentLevelFields").getChildren("DocumentLevelField");
			if(doc.getChildText("Type").equalsIgnoreCase("Bank Guarantees")){
				for(Element dlf : dlfs){
					if(dlf.getChildText("Name").equalsIgnoreCase("BGAmount")){
						boolean match = false;
						String finalValue = "";
						Pattern p = Pattern.compile("\\d+\\.\\d{2}");
						try{
							Matcher m = p.matcher(dlf.getChildText("Value").trim());
							while(m.find()){
								System.out.println("Matched: "+m.group());
								finalValue = m.group();
								match = true;
								break;
							}
							if(match == true)
								dlf.getChild("Value").setText(finalValue);
						}
						catch (Exception e){
							System.out.println("Error in BG Amount");
						}
					}
				}
			}
		}
	}

	private void handleDate(Document document){
		Element root = document.getRootElement();
		List<Element> docs = root.getChild("Documents").getChildren("Document");

		for(Element doc:docs){
			String gDateRec = "", eDateRec = "", lcDateRec = "", numDays = "", toBeAdd = "";
			boolean validDate = false, lcDateAvail = false, numDaysAvail = false;
			StringBuilder sb = new StringBuilder();


			List<Element> dlfs = doc.getChild("DocumentLevelFields").getChildren("DocumentLevelField");
			for(Element dlf : dlfs){
				String convertedDate = "";
				//Guarantee Date
				if(dlf.getChildText("Name").equalsIgnoreCase("BGGuaranteeDate")){
					try{
						System.out.println("In Guarantee Date");
						gDateRec = dlf.getChildText("Value").trim().toLowerCase();
						gDateRec = returnConvertedDate(gDateRec,dlf);
						System.out.println("Returned from function "+gDateRec);
					}
					catch (Exception e){
						System.out.println("Error in BG Guarantee Date");
					}
				}

				//Change date for expiry date
				if(dlf.getChildText("Name").equalsIgnoreCase("BGExpiryDate")){
					try{
						System.out.println("In Expiry Date");
						eDateRec = dlf.getChildText("Value").trim().toLowerCase();//.replace("st","").replace("th","").replace("nd","").replace("rd","");
						eDateRec = returnConvertedDate(eDateRec,dlf);
						System.out.println("Returned from function "+eDateRec);
					}
					catch (Exception e){
						System.out.println("Error in BG Expiry Date");
					}
				}
				//Bank Last Claim Date
				if(dlf.getChildText("Name").equalsIgnoreCase("BGLastClaimDate")){
					System.out.println("In BG Last Claim Date");
					try{
						lcDateRec = dlf.getChildText("Value").trim().toLowerCase();
						if(lcDateRec.isEmpty()) {
							System.out.println("Empty");
							lcDateAvail = false;
						}
						else
							lcDateAvail = true;

						if(lcDateAvail == true){
							try{
								lcDateRec = returnConvertedDate(lcDateRec,dlf);
								System.out.println("Returned from function "+lcDateRec);
							}
							catch(Exception e){
								System.out.println("Date received wrongly");
							}
						}
					}
					catch (Exception e){
						System.out.println("Last Claim Date not available");
					}

				}

				if(dlf.getChildText("Name").equalsIgnoreCase("ValidityPeriod")){
					try{
						numDays = dlf.getChildText("Value").trim();
						if(numDays.isEmpty()){
							numDaysAvail = false;
						}
						else{
							numDaysAvail = true;
							System.out.println("Extracted number of days: "+numDays);
						}
					}
					catch (Exception e){
						System.out.println("");
					}
				}
			}

			if (numDaysAvail == true && lcDateAvail == false){
				for(Element dlf : dlfs){
					if(dlf.getChildText("Name").equalsIgnoreCase("BGLastClaimDate")){
						try{
							Date toChangeDate = new SimpleDateFormat("dd/MM/yyyy").parse(eDateRec);
							SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
							String convertedDate = sdf.format(toChangeDate);
							System.out.println("Converted Date: "+convertedDate);

							Calendar c = Calendar.getInstance();
							try{
								c.setTime(sdf.parse(convertedDate));
								c.add(Calendar.DAY_OF_MONTH,Integer.parseInt(numDays));
								sdf = new SimpleDateFormat("dd/MM/yyyy");
								String newDate = sdf.format(c.getTime());
								System.out.println("Added date: "+newDate);
								dlf.getChild("Value").setText(newDate);
							}
							catch (Exception e){
								e.printStackTrace();
							}
						}
						catch (Exception e){
							System.out.println("Unable to convert");
						}
					}
				}
			}
		}
	}

	private void getTableRuleName(Document document){
		System.out.println();
		System.out.println("In getting table name");
		Element root = document.getRootElement();
		List<Element> docs = root.getChild("Documents").getChildren("Document");
		for (Element doc : docs) {
			if(doc.getChildText("Type").equalsIgnoreCase("Form24")){
				String ruleName = "";
				try {
					List<Element> dts = doc.getChild("DataTables").getChildren("DataTable");

					for (Element dt : dts) {
						ruleName = dt.getChildText("RuleName");
						System.out.println("Rule Name: "+ruleName);
					}
				}
				catch(Exception e) {
					System.out.println("No datatables found");
				}

				try{
					List<Element> dlfs = doc.getChild("DocumentLevelFields").getChildren("DocumentLevelField");

					for (Element dlf : dlfs) {
						System.out.println("Test");
						try{
							System.out.println("In try");
							if(dlf.getChildText("Name").equalsIgnoreCase("RuleName")){
								System.out.println("In RuleName");
								dlf.getChild("Value").setText(ruleName);
								System.out.println("Assigned!");
								break;
							}
						}
						catch (Exception e){
							System.out.println("No RuleName");
						}
					}
				}
				catch (Exception ea){
					System.out.println("Error");
				}
			}
		}
	}

	private void swapValue(Document document){
		Element root = document.getRootElement();
		List<Element> docs = root.getChild("Documents").getChildren("Document");

		for(Element doc : docs){
			List<Element> dlfs = doc.getChild("DocumentLevelFields").getChildren("DocumentLevelField");
			for(Element dlf : dlfs){
				int x0main=0,x1main=0,y0main=0,y1main=0;
				int x0alt=0,x1alt=0,y0alt=0,y1alt=0;
				String alternateValues = "";
				try {
					if (dlf.getChildText("Name").equalsIgnoreCase("CLTermLoans")) {
						List<Element> cds = dlf.getChild("CoordinatesList").getChildren("Coordinates");
						for (Element cd : cds) {
							x0main = Integer.parseInt(cd.getChildText("x0"));
							x1main = Integer.parseInt(cd.getChildText("x1"));
							y0main = Integer.parseInt(cd.getChildText("y0"));
							y1main = Integer.parseInt(cd.getChildText("y1"));
						}
						try {
							List<Element> alts = dlf.getChild("AlternateValues").getChildren("AlternateValue");
							for (Element alt : alts) {
								List<Element> cdsAlt = alt.getChild("CoordinatesList").getChildren("Coordinates");
								for (Element cdAlt : cdsAlt) {
									alternateValues = cdAlt.getChildText("Value");
									x0alt = Integer.parseInt(cdAlt.getChildText("x0"));
									x1alt = Integer.parseInt(cdAlt.getChildText("x1"));
									y0alt = Integer.parseInt(cdAlt.getChildText("y0"));
									y1alt = Integer.parseInt(cdAlt.getChildText("y1"));
								}
							}
						} catch (Exception e) {
							System.out.print("No alternate values");
						}

						alternateValues = alternateValues.trim().replaceAll(",", "");

						if (y1alt > y1main && y0alt > y0main && !alternateValues.contains(" ")) {
							dlf.getChild("Value").setText(alternateValues);
							for (Element cd : cds) {
								cd.getChild("x0").setText(Integer.toString(x0alt));
								cd.getChild("x1").setText(Integer.toString(x1alt));
								cd.getChild("y0").setText(Integer.toString(y0alt));
								cd.getChild("y1").setText(Integer.toString(y1alt));
							}
						}
					}
				}
				catch(Exception e){
					LOGGER.error("Nothing to be extracted");
				}

				try {
					if (dlf.getChildText("Name").equalsIgnoreCase("CLHirePurchasePayable")) {
						List<Element> cds = dlf.getChild("CoordinatesList").getChildren("Coordinates");
						for (Element cd : cds) {
							x0main = Integer.parseInt(cd.getChildText("x0"));
							x1main = Integer.parseInt(cd.getChildText("x1"));
							y0main = Integer.parseInt(cd.getChildText("y0"));
							y1main = Integer.parseInt(cd.getChildText("y1"));
						}
						try {
							List<Element> alts = dlf.getChild("AlternateValues").getChildren("AlternateValue");
							for (Element alt : alts) {
								List<Element> cdsAlt = alt.getChild("CoordinatesList").getChildren("Coordinates");
								for (Element cdAlt : cdsAlt) {
									alternateValues = cdAlt.getChildText("Value");
									x0alt = Integer.parseInt(cdAlt.getChildText("x0"));
									x1alt = Integer.parseInt(cdAlt.getChildText("x1"));
									y0alt = Integer.parseInt(cdAlt.getChildText("y0"));
									y1alt = Integer.parseInt(cdAlt.getChildText("y1"));
								}
							}
						} catch (Exception e) {
							System.out.print("No alternate values");
						}

						alternateValues = alternateValues.trim().replaceAll(",", "");

						if (y1alt > y1main && y0alt > y0main && !alternateValues.contains(" ")) {
							dlf.getChild("Value").setText(alternateValues);
							for (Element cd : cds) {
								cd.getChild("x0").setText(Integer.toString(x0alt));
								cd.getChild("x1").setText(Integer.toString(x1alt));
								cd.getChild("y0").setText(Integer.toString(y0alt));
								cd.getChild("y1").setText(Integer.toString(y1alt));
							}
						}
					}
				}
				catch (Exception e){
					LOGGER.error("No value to be extracted");
				}

				try {
					if (dlf.getChildText("Name").equalsIgnoreCase("CLTermLoans")) {
						List<Element> cds = dlf.getChild("CoordinatesList").getChildren("Coordinates");
						for (Element cd : cds) {
							x0main = Integer.parseInt(cd.getChildText("x0"));
							x1main = Integer.parseInt(cd.getChildText("x1"));
							y0main = Integer.parseInt(cd.getChildText("y0"));
							y1main = Integer.parseInt(cd.getChildText("y1"));
						}
						try {
							List<Element> alts = dlf.getChild("AlternateValues").getChildren("AlternateValue");
							for (Element alt : alts) {
								List<Element> cdsAlt = alt.getChild("CoordinatesList").getChildren("Coordinates");
								for (Element cdAlt : cdsAlt) {
									alternateValues = cdAlt.getChildText("Value");
									x0alt = Integer.parseInt(cdAlt.getChildText("x0"));
									x1alt = Integer.parseInt(cdAlt.getChildText("x1"));
									y0alt = Integer.parseInt(cdAlt.getChildText("y0"));
									y1alt = Integer.parseInt(cdAlt.getChildText("y1"));
								}
							}
						} catch (Exception e) {
							System.out.print("No alternate values");
						}

						alternateValues = alternateValues.trim().replaceAll(",", "");

						if (y1alt > y1main && y0alt > y0main && !alternateValues.contains(" ")) {
							dlf.getChild("Value").setText(alternateValues);
							for (Element cd : cds) {
								cd.getChild("x0").setText(Integer.toString(x0alt));
								cd.getChild("x1").setText(Integer.toString(x1alt));
								cd.getChild("y0").setText(Integer.toString(y0alt));
								cd.getChild("y1").setText(Integer.toString(y1alt));
							}
						}
					}
				}
				catch (Exception e){
					LOGGER.error("No value to be extracted");
				}

				try {
					if (dlf.getChildText("Name").equalsIgnoreCase("CLBorrowings")) {
						List<Element> cds = dlf.getChild("CoordinatesList").getChildren("Coordinates");
						for (Element cd : cds) {
							x0main = Integer.parseInt(cd.getChildText("x0"));
							x1main = Integer.parseInt(cd.getChildText("x1"));
							y0main = Integer.parseInt(cd.getChildText("y0"));
							y1main = Integer.parseInt(cd.getChildText("y1"));
						}
						try {
							List<Element> alts = dlf.getChild("AlternateValues").getChildren("AlternateValue");
							for (Element alt : alts) {
								List<Element> cdsAlt = alt.getChild("CoordinatesList").getChildren("Coordinates");
								for (Element cdAlt : cdsAlt) {
									alternateValues = cdAlt.getChildText("Value");
									x0alt = Integer.parseInt(cdAlt.getChildText("x0"));
									x1alt = Integer.parseInt(cdAlt.getChildText("x1"));
									y0alt = Integer.parseInt(cdAlt.getChildText("y0"));
									y1alt = Integer.parseInt(cdAlt.getChildText("y1"));
								}
							}
						} catch (Exception e) {
							System.out.print("No alternate values");
						}

						alternateValues = alternateValues.trim().replaceAll(",", "");

						if (y1alt > y1main && y0alt > y0main && !alternateValues.contains(" ")) {
							dlf.getChild("Value").setText(alternateValues);
							for (Element cd : cds) {
								cd.getChild("x0").setText(Integer.toString(x0alt));
								cd.getChild("x1").setText(Integer.toString(x1alt));
								cd.getChild("y0").setText(Integer.toString(y0alt));
								cd.getChild("y1").setText(Integer.toString(y1alt));
							}
						}
					}
				}
				catch (Exception e){
					LOGGER.error("No value to be extracted");
				}
			}
		}
	}

	private void checkPageExtracted(Document document){
		Element root = document.getRootElement();
		List<Element> docs = root.getChild("Documents").getChildren("Document");

		for(Element doc: docs){
			String currentPageNo = "",BSPageNo = "";
			List<Element> dlfs = doc.getChild("DocumentLevelFields").getChildren("DocumentLevelField");
			for(Element dlf : dlfs){
				if(dlf.getChildText("Name").equalsIgnoreCase("TotalCurrentAssets")){
					try{
						BSPageNo = dlf.getChildText("Page");
						System.out.println("BSPageNo: "+BSPageNo.substring(2));
					}
					catch (Exception e){
						LOGGER.error("No Total Current Assets has been extracted");
					}

				}
				if(dlf.getChildText("Name").equalsIgnoreCase("DLHirePurchasePayable")){
					try{
						currentPageNo = dlf.getChildText("Page");
						System.out.println("currentPageNo: "+currentPageNo.substring(2));
						if(!((Integer.parseInt(currentPageNo.substring(2))==Integer.parseInt(BSPageNo.substring(2))) || (Integer.parseInt(currentPageNo.substring(2))==Integer.parseInt(BSPageNo.substring(2))+1))){
							dlf.getChild("Value").setText("0");
							dlf.removeChild("CoordinatesList");
						}
					}
					catch (Exception e){
						LOGGER.error("No DLHirePurchasePayable has been extracted");
					}
				}
				if(dlf.getChildText("Name").equalsIgnoreCase("DLBorrowings")){
					try{
						currentPageNo = dlf.getChildText("Page");
						System.out.println("currentPageNo: "+currentPageNo.substring(2));
						if(!((Integer.parseInt(currentPageNo.substring(2))==Integer.parseInt(BSPageNo.substring(2))) || (Integer.parseInt(currentPageNo.substring(2))==Integer.parseInt(BSPageNo.substring(2))+1))){
							dlf.getChild("Value").setText("0");
							dlf.removeChild("CoordinatesList");
						}
					}
					catch (Exception e){
						LOGGER.error("No DLBorrowings has been extracted");
					}
				}
				if(dlf.getChildText("Name").equalsIgnoreCase("DLTermLoans")){
					try{
						currentPageNo = dlf.getChildText("Page");
						System.out.println("currentPageNo: "+currentPageNo.substring(2));
						if(!((Integer.parseInt(currentPageNo.substring(2))==Integer.parseInt(BSPageNo.substring(2))) || (Integer.parseInt(currentPageNo.substring(2))==Integer.parseInt(BSPageNo.substring(2))+1))){
							dlf.getChild("Value").setText("0");
							dlf.removeChild("CoordinatesList");
						}
					}
					catch (Exception e){
						LOGGER.error("No DLTermLoans has been extracted");
					}
				}
				if(dlf.getChildText("Name").equalsIgnoreCase("TradeCreditors")){
					try{
						currentPageNo = dlf.getChildText("Page");
						System.out.println("currentPageNo: "+currentPageNo.substring(2));
						if(!((Integer.parseInt(currentPageNo.substring(2))==Integer.parseInt(BSPageNo.substring(2))) || (Integer.parseInt(currentPageNo.substring(2))==Integer.parseInt(BSPageNo.substring(2))+1))){
							dlf.getChild("Value").setText("0");
							dlf.removeChild("CoordinatesList");
						}
					}
					catch (Exception e){
						LOGGER.error("No TradeCreditors has been extracted");
					}
				}
				if(dlf.getChildText("Name").equalsIgnoreCase("TradeCreditors")){
					try{
						currentPageNo = dlf.getChildText("Page");
						System.out.println("currentPageNo: "+currentPageNo.substring(2));
						if(!((Integer.parseInt(currentPageNo.substring(2))==Integer.parseInt(BSPageNo.substring(2))) || (Integer.parseInt(currentPageNo.substring(2))==Integer.parseInt(BSPageNo.substring(2))+1))){
							dlf.getChild("Value").setText("0");
							dlf.removeChild("CoordinatesList");
						}
					}
					catch (Exception e){
						LOGGER.error("No TradeCreditors has been extracted");
					}
				}
				if(dlf.getChildText("Name").equalsIgnoreCase("CLHirePurchasePayable")){
					try{
						currentPageNo = dlf.getChildText("Page");
						System.out.println("currentPageNo: "+currentPageNo.substring(2));
						if(!((Integer.parseInt(currentPageNo.substring(2))==Integer.parseInt(BSPageNo.substring(2))) || (Integer.parseInt(currentPageNo.substring(2))==Integer.parseInt(BSPageNo.substring(2))+1))){
							dlf.getChild("Value").setText("0");
							dlf.removeChild("CoordinatesList");
						}
					}
					catch (Exception e){
						LOGGER.error("No CLHirePurchasePayable has been extracted");
					}
				}
				if(dlf.getChildText("Name").equalsIgnoreCase("CLTermLoans")){
					try{
						currentPageNo = dlf.getChildText("Page");
						System.out.println("currentPageNo: "+currentPageNo.substring(2));
						if(!((Integer.parseInt(currentPageNo.substring(2))==Integer.parseInt(BSPageNo.substring(2))) || (Integer.parseInt(currentPageNo.substring(2))==Integer.parseInt(BSPageNo.substring(2))+1))){
							dlf.getChild("Value").setText("0");
							dlf.removeChild("CoordinatesList");
						}
					}
					catch (Exception e){
						LOGGER.error("No CLTermLoans has been extracted");
					}
				}
				if(dlf.getChildText("Name").equalsIgnoreCase("CLBorrowings")){
					try{
						currentPageNo = dlf.getChildText("Page");
						System.out.println("currentPageNo: "+currentPageNo.substring(2));
						if(!((Integer.parseInt(currentPageNo.substring(2))==Integer.parseInt(BSPageNo.substring(2))) || (Integer.parseInt(currentPageNo.substring(2))==Integer.parseInt(BSPageNo.substring(2))+1))){
							dlf.getChild("Value").setText("0");
							dlf.removeChild("CoordinatesList");
						}
					}
					catch (Exception e){
						LOGGER.error("No CLBorrowings has been extracted");
					}
				}
				if(dlf.getChildText("Name").equalsIgnoreCase("CLBankOverdraft")){
					try{
						currentPageNo = dlf.getChildText("Page");
						System.out.println("currentPageNo: "+currentPageNo.substring(2));
						if(!((Integer.parseInt(currentPageNo.substring(2))==Integer.parseInt(BSPageNo.substring(2))) || (Integer.parseInt(currentPageNo.substring(2))==Integer.parseInt(BSPageNo.substring(2))+1))){
							dlf.getChild("Value").setText("0");
							dlf.removeChild("CoordinatesList");
						}
					}
					catch (Exception e){
						LOGGER.error("No CLBankOverdraft has been extracted");
					}
				}
				if(dlf.getChildText("Name").equalsIgnoreCase("CLBillsPayable")){
					try{
						currentPageNo = dlf.getChildText("Page");
						System.out.println("currentPageNo: "+currentPageNo.substring(2));
						if(!((Integer.parseInt(currentPageNo.substring(2))==Integer.parseInt(BSPageNo.substring(2))) || (Integer.parseInt(currentPageNo.substring(2))==Integer.parseInt(BSPageNo.substring(2))+1))){
							dlf.getChild("Value").setText("0");
							dlf.removeChild("CoordinatesList");
						}
					}
					catch (Exception e){
						LOGGER.error("No CLBillsPayable has been extracted");
					}
				}
				if(dlf.getChildText("Name").equalsIgnoreCase("CLBankerAcceptance")){
					try{
						currentPageNo = dlf.getChildText("Page");
						System.out.println("currentPageNo: "+currentPageNo.substring(2));
						if(!((Integer.parseInt(currentPageNo.substring(2))==Integer.parseInt(BSPageNo.substring(2))) || (Integer.parseInt(currentPageNo.substring(2))==Integer.parseInt(BSPageNo.substring(2))+1))){
							dlf.getChild("Value").setText("0");
							dlf.removeChild("CoordinatesList");
						}
					}
					catch (Exception e){
						LOGGER.error("No Bank CLBankerAcceptance has been extracted");
					}
				}
			}
		}
	}

	/**
	 * The <code>writeToXML</code> method will write the state document to the XML file.
	 * 
	 * @param document {@link Document}.
	 */
	private void writeToXML(Document document) {
		String batchLocalPath = null;
		List<?> batchLocalPathList = document.getRootElement().getChildren(BATCH_LOCAL_PATH);
		if (null != batchLocalPathList) {
			batchLocalPath = ((Element) batchLocalPathList.get(0)).getText();
		}

		if (null == batchLocalPath) {
			LOGGER.error("Unable to find the local folder path in batch xml file.");
			return;
		}

		String batchInstanceID = null;
		List<?> batchInstanceIDList = document.getRootElement().getChildren(BATCH_INSTANCE_ID);
		if (null != batchInstanceIDList) {
			batchInstanceID = ((Element) batchInstanceIDList.get(0)).getText();

		}

		if (null == batchInstanceID) {
			LOGGER.error("Unable to find the batch instance ID in batch xml file.");
			return;
		}

		String batchXMLPath = batchLocalPath.trim() + File.separator + batchInstanceID + File.separator + batchInstanceID
				+ EXT_BATCH_XML_FILE;

		boolean isZipSwitchOn = true;
		try {
			ApplicationConfigProperties prop = ApplicationConfigProperties.getApplicationConfigProperties();
			isZipSwitchOn = Boolean.parseBoolean(prop.getProperty(ICommonConstants.ZIP_SWITCH));
		} catch (IOException ioe) {
			LOGGER.error("Unable to read the zip switch value. Taking default value as true. Exception thrown is:" + ioe.getMessage(),
					ioe);
		}

		LOGGER.info("isZipSwitchOn************" + isZipSwitchOn);
		OutputStream outputStream = null;
		FileWriter writer = null;
		XMLOutputter out = new com.ephesoft.dcma.batch.encryption.util.BatchInstanceXmlOutputter(batchInstanceID);

		try {
			if (isZipSwitchOn) {
				LOGGER.info("Found the batch xml zip file.");
				outputStream = getOutputStreamFromZip(batchXMLPath, batchInstanceID + EXT_BATCH_XML_FILE);
				out.output(document, outputStream);
			} else {
				writer = new java.io.FileWriter(batchXMLPath);
				out.output(document, writer);
				writer.flush();
				writer.close();
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		} finally {
			if (outputStream != null) {
				try {
					outputStream.close();
				} catch (IOException e) {
				}
			}
		}
	}

	/**
	 * The <code>checkValueText</code> method will check the valueText with typeText compatibility.
	 * 
	 * @param valueText {@link String}
	 * @param typeText {@link String}
	 * @return boolean true if pass the test otherwise false.
	 */
	private boolean checkValueText(String valueText, String typeText) {

		boolean isValid = false;
		if (null == valueText || "".equals(valueText)) {
			isValid = false;
		} else {
			if (typeText.equals(DATE)) {
				SimpleDateFormat format = new SimpleDateFormat(PATTERN);
				try {
					format.parse(valueText);
					isValid = true;
				} catch (Exception e) {
					// the value couldn't be parsed by the pattern, return
					// false.
					isValid = false;
				}
			} else {
				if (typeText.equals(LONG)) {
					try {
						Long.parseLong(valueText);
						isValid = true;
					} catch (Exception e) {
						// the value couldn't be parsed by the pattern, return
						// false
						isValid = false;
					}
				} else {
					if (typeText.equals(DOUBLE)) {
						try {
							Float.parseFloat(valueText);
							isValid = true;
						} catch (Exception e) {
							// the value couldn't be parsed by the pattern,
							// return false
							isValid = false;
						}
					} else {
						if (typeText.equals(STRING)) {
							isValid = true;
						} else {
							isValid = false;
						}
					}
				}
			}
		}

		return isValid;
	}

	public String returnConvertedDate(String dateReceived, Element dlf){
		StringBuilder sb = new StringBuilder();
		String convertedDate = "";
		boolean converted = false;
		try{
			dateReceived = dlf.getChildText("Value").trim().toLowerCase();//.replace("st","").replace("th","").replace("nd","").replace("rd","");
			System.out.println("Received: "+dateReceived);
			sb = new StringBuilder(dateReceived);
			if(Pattern.matches("\\d+",dateReceived.substring(0,2)) && Pattern.matches("[a-z]{2}",dateReceived.substring(2,4))){
				dateReceived = sb.deleteCharAt(2).toString();
				dateReceived = sb.deleteCharAt(2).toString();
				//gDateRec = gDateRec.substring(2,4).replace("st","").replace("th","").replace("nd","").replace("rd","");
				//validDate = true;
				System.out.println("Removed st,nd,rd,th: "+dateReceived);
			}
			else if(Pattern.matches("\\d",dateReceived.substring(0,1)) && Pattern.matches("[a-z]{2}",dateReceived.substring(1,3))){
				dateReceived = sb.deleteCharAt(1).toString();
				dateReceived = sb.deleteCharAt(1).toString();
				//gDateRec = gDateRec.substring(1,3).replace("st","").replace("th","").replace("nd","").replace("rd","");
				//validDate = true;
				System.out.println("Removed st,nd,rd,th: "+dateReceived);
			}


			SimpleDateFormat sdf = new SimpleDateFormat();
			//if(validDate == true){
				try{
					Date finalDate = new SimpleDateFormat("dd MMMM yyyy").parse(dateReceived);
					sdf = new SimpleDateFormat("dd/MM/yyyy");
					convertedDate = sdf.format(finalDate);
					System.out.println("Converted Date: "+convertedDate);
					converted = true;
				}
				catch(Exception e){
					System.out.println("Date received was not in a good format");
					converted = false;
				}

				if(converted == false){
					try{
						Date finalDate = new SimpleDateFormat("MMMM dd yyyy").parse(dateReceived);
						sdf = new SimpleDateFormat("dd/MM/yyyy");
						convertedDate = sdf.format(finalDate);
						System.out.println("Converted Date: "+convertedDate);
						converted = true;
					}
					catch (Exception e){
						System.out.println("Date received was not in a good format");
						converted = false;
					}
				}

				if(converted == false){
					try{
						Date finalDate = new SimpleDateFormat("MMMM ddyyyy").parse(dateReceived);
						sdf = new SimpleDateFormat("dd/MM/yyyy");
						convertedDate = sdf.format(finalDate);
						System.out.println("Converted Date: "+convertedDate);
						converted = true;
					}
					catch (Exception e){
						System.out.println("Date received was not in a good format");
						converted = false;
					}
				}

				if(converted == true)
					dlf.getChild("Value").setText(convertedDate);
			//}
		}
		catch(Exception e){
			System.out.println("Date received wrongly");
		}
		return convertedDate;
	}

	public static OutputStream getOutputStreamFromZip(final String zipName, final String fileName) throws FileNotFoundException,
			IOException {
		ZipOutputStream stream = null;
		stream = new ZipOutputStream(new FileOutputStream(new File(zipName + ZIP_FILE_EXT)));
		ZipEntry zipEntry = new ZipEntry(fileName);
		stream.putNextEntry(zipEntry);
		return stream;
	}
}
