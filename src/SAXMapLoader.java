import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.io.gml2.GMLConstants;
import com.vividsolutions.jts.io.gml2.GMLHandler;

import dto.CartographicText;

/**
 * http://www.ibm.com/developerworks/xml/library/x-perfap1.html
 * 
 * Table structure of OSMM data
cartographicsymbol	theme
cartographicsymbol	physicalpresence
cartographicsymbol	referencetofeature
cartographicsymbol	physicallevel
cartographicsymbol	orientation
cartographicsymbol	descriptiveterm
cartographicsymbol	descriptivegroup
cartographicsymbol	versiondate
cartographicsymbol	version
cartographicsymbol	featurecode
cartographicsymbol	wkb_geometry
cartographicsymbol	ogc_fid
cartographictext	make
cartographictext	textstring
cartographictext	physicallevel
cartographictext	descriptivegroup
cartographictext	descriptiveterm
cartographictext	version
cartographictext	theme
cartographictext	versiondate
cartographictext	ogc_fid
cartographictext	wkb_geometry
cartographictext	featurecode
cartographictext	anchorposition
cartographictext	font
cartographictext	height
cartographictext	orientation

 * 
 * @author aled
 *
 */
public class SAXMapLoader {
	
	public static final Logger log = Logger.getLogger(SAXMapLoader.class.getName());
	
	private ContentHandler currentHandler;
	private ContentHandler docHandler = new DocHandler();
	private ContentHandler geomHandler = new GeomHandler();
	private ContentHandler cartoTextHandler;
	private Writer cartoTextOutput;
	private String cartoTextOutputFilename = "C:\\temp\\osmmloader\\cartoText.txt";
	private XMLReader reader;
	
	public static final class OSGMLConstants {
		public static final String OS_GEOM = "osgb:geometry";
		public static final String OS_CRS = "osgb:BNG";
		public static final String OS_CARTOGRAPHIC_MEMBER = "osgb:cartographicMember";
	}
	
	public SAXMapLoader() {
		try {
	        cartoTextOutput = new BufferedWriter(new FileWriter("outfilename"));
	    } catch (IOException e) {
	    	log.fatal("unable to open cartocraphic text output " + cartoTextOutputFilename );
	    }
	}
	
	public class GeomHandler extends GMLHandler{
		public GeomHandler() {
			super(new GeometryFactory(new PrecisionModel(),27700),null);
		}
		@Override
		public void endElement(String uri, String localName, String name)
				throws SAXException {
			super.endElement(uri, localName, name);
			if (isGeometryComplete()){
				//TODO: output this somehow
			}
		}
	}
		
	
	public class DocHandler extends DefaultHandler{

		public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
			
	            
	    		
	    }
		
		@Override
		public void endDocument() throws SAXException {
			// TODO: close all the output files
			super.endDocument();
		}
	}
	
		
	
	/**
	 * For the moment this just deals with Cartographic Text
	 * @author a4709393
	 */
	public class CartographicTextHandler extends DefaultHandler{
		private String currElem; 
		private StringBuilder val = new StringBuilder();
		private CartographicText c = new CartographicText();
		
		public static final String WRAPPER_ELEMENT = "osgb:cartographicText";
		
		@Override
		public void startElement(String uri, String localName, String name,
				Attributes attributes) throws SAXException {
			if (c.getAttribs().containsKey(name.toString())){
				currElem = name;
//			} else if (attributes.getIndex('fid')>=0){
//				//this elem has an 
			}
		}
		
		@Override
		public void characters(char[] ch, int start, int length)
				throws SAXException {
			val.append(ch,start,length);
		}
		
		@Override
		public void endElement(String uri, String localName, String name)
				throws SAXException {
			if(name.equals(currElem)){
				c.setAttrib(currElem, val);
				currElem = "";
			}
//			if (name.equals(wrapElem)){
//				
//				reader.setContentHandler(docHandler);
//			}
		}
	}
	
	
	public void parse(String fileName) throws SAXException{
		XMLReader xr = XMLReaderFactory.createXMLReader();
		reader.setContentHandler(docHandler);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		String gmlUri = "file:///C:/Projects/workspace/MapLoader/src/9620-NZ3622-5i376.xml";
		SAXMapLoader ldr = new SAXMapLoader();
		ldr.parse(gmlUri);

	}

}
