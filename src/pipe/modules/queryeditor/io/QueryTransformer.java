/**
 * QueryTransformer
 * 
 * - deals with getting information from the XML file, then passes this 
 *   back to QueryData constructor for construction
 *   
 * @author Tamas Suto
 * @date 09/05/07
 */

package pipe.modules.queryeditor.io;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;


public class QueryTransformer {

	
	public QueryTransformer() {
		
	}

	
	/** Transform a PTML file into a Document which is returned and used to
	 * construct the QueryData
	 * 
	 * @returns - Document from which QueryData can be built
	 * @param filename - URI location of PTML
	 * @throws IOException
	 * @throws SAXException
	 * @throws TransformerException
	 * @throws ParserConfigurationException
	 */
	public Document transformPTML(String filename) {
		File outputObjectArrayList = null;
		Document document = null;

		try {
			// Create Transformer with XSL source file
			StreamSource xsltSource = new StreamSource(Thread.currentThread().getContextClassLoader().getResourceAsStream(
					"pipe"+System.getProperty("file.separator")+
					"modules"+System.getProperty("file.separator")+
					"queryeditor"+System.getProperty("file.separator")+
					"io"+System.getProperty("file.separator")+"LoadQueryXML.xsl"));		
			
			if (xsltSource == null)
				System.out.println("xsltSource is null");
			
			Transformer transformer = TransformerFactory.newInstance().newTransformer(xsltSource);	
			outputObjectArrayList = new File(System.getProperty("java.io.tmpdir") + System.getProperty("file.separator") + "QueryDocument.xml"); 			
			outputObjectArrayList.deleteOnExit();
			StreamSource source = new StreamSource(filename);
			StreamResult result = new StreamResult(outputObjectArrayList);
			transformer.transform(source, result);
			document = getDOM(outputObjectArrayList);    
		} catch (IOException e) {
			System.out.println("IOException thrown in transformPTML(String filename) : modules.queryeditor.io.QueryTransformer");
			e.printStackTrace(System.err);
		} catch (SAXException e) {
			System.out.println("SAXException thrown in transformPTML(String filename) : modules.queryeditor.io.QueryTransformer");
			e.printStackTrace(System.err);
		} catch (TransformerException e) {
			System.out.println("TransformerException thrown in transformPTML(String filename) : modules.queryeditor.io.QueryTransformer");
			e.printStackTrace(System.err);
		} catch (ParserConfigurationException e) {
			System.out.println("ParserConfigurationException thrown in transformPTML(String filename) : modules.queryeditor.io.QueryTransformer");
			e.printStackTrace(System.err);
		}

		if(outputObjectArrayList != null) 
			// Delete transformed file
			outputObjectArrayList.delete();

		return document;   
	}

	/**
	 * Return a DOM for the PTML file at URI ptmlFileName
	 *
	 * @param ptmlFileName - URI of PTML file
	 * @return A DOM for the PTML file ptmlFileName
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws SAXException
	 */
	public Document getDOM(String ptmlFileName) throws ParserConfigurationException, IOException, SAXException {
		Document document = null;

		try {
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			documentBuilderFactory.setIgnoringElementContentWhitespace(true);
			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
			document = documentBuilder.parse(ptmlFileName);
		} catch (ParserConfigurationException e) {
			System.err.println("javax.xml.parsers.ParserConfigurationException thrown in getDom(String ptmlFileName) : modules.queryeditor.io.QueryTransformer");
		} catch (IOException e) {
			System.err.println("ERROR: File may not be present or have the correct attributes");
			System.err.println("java.io.IOException thrown in getDom(String ptmlFileName) : modules.queryeditor.io.QueryTransformer");
		} catch (SAXException e) {
			System.err.println("org.xml.sax.SAXException thrown in getDom(String ptmlFileName) : modules.queryeditor.io.QueryTransformer");
		}

		return document;
	}

	/**
	 * Return a DOM for the PTML File ptmlFile
	 *
	 * @param ptmlFile - File Object for PTML 
	 * @return A DOM for the File Object for PTML 
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws SAXException
	 */
	public Document getDOM(File ptmlFile) throws ParserConfigurationException, IOException, SAXException {
		Document document = null;

		try {
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			documentBuilderFactory.setIgnoringElementContentWhitespace(true);
			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
			document = documentBuilder.parse(ptmlFile);
		} catch (ParserConfigurationException e) {
			System.err.println("javax.xml.parsers.ParserConfigurationException thrown in getDom(String ptmlFileName) : modules.queryeditor.io.QueryTransformer");
		} catch (IOException e) {
			System.err.println("ERROR: File may not be present or have the correct attributes");
			System.err.println("java.io.IOException thrown in getDom(String ptmlFileName) : modules.queryeditor.io.QueryTransformer" + e);
		} catch (SAXException e) {
			System.err.println("org.xml.sax.SAXException thrown in getDom(String ptmlFileName) : modules.queryeditor.io.QueryTransformer" + e);
			System.err.println("Workaround: delete the xmlns attribute from the PNML root node.  Probably not ideal, to be fixed when time allows.");
		}

		return document;
	}

}

