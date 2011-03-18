

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.log4j.Logger;
import org.geotools.xml.XSISAXHandler;
import org.geotools.xml.gml.GMLComplexTypes;
import org.opengis.feature.type.FeatureType;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;


public class MapLoader2 {
	
	public static final Logger log = Logger.getLogger(MapLoader2.class.getName());
	
	public static void main(String[] args) {
		String schemaString ="file:///C:/Projects/workspace/MapLoader/bin/OSDNFFeatures.xsd";
		try {
			XMLReader reader = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
			
			
			URI schemaLoc = new java.net.URI(schemaString);

			XSISAXHandler schemaHandler = new XSISAXHandler(schemaLoc);

			reader.setContentHandler(schemaHandler);
			reader.parse(new InputSource(new URL(schemaLoc.toString()).openConnection().getInputStream()));

			FeatureType ft = GMLComplexTypes.createFeatureType(schemaHandler.getSchema().getElements()[0]);
			
			
			log.debug("CRS: " + ft.getCoordinateReferenceSystem());
			log.debug(ft.toString());
		} catch (MalformedURLException e) {
			log.fatal("unable to access schema " + schemaString);
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			log.fatal("Badly formed schema URI" + schemaString);
			e.printStackTrace();
		} catch (IOException e) {
			log.fatal("Unable to access " + schemaString);
			e.printStackTrace();
		}
	}
	
	
	
}
