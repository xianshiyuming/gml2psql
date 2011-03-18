import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import org.apache.commons.lang.time.DurationFormatUtils;
import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.io.gml2.GMLHandler;


public class SAXMapLoader2 {
	public static final Logger log = Logger.getLogger(SAXMapLoader2.class
			.getName());
	
	private XMLReader reader; 
	private MasterMapHandler docHandler = new SAXMapLoader2.MasterMapHandler();
	private String delimiter = "|";
	private String lineReturn = "\n";
	private int featureCount;
	private long startTime;
	
	/**
	 * Root ContentHandler for OSMasterMap GML documents. Creates an OSFeatureTypeHandler for each featuretype to handle.
	 * @author a4709393
	 *
	 */
	public class MasterMapHandler extends DefaultHandler{
		private Map<String, OSFeatureTypeHandler> featureTypes = new HashMap<String, OSFeatureTypeHandler>();
		private String database;
		private String databaseUser;
		private String databasePassword;
				
		/**
		 * Add a feature type to capture. All featuretypes output geom as WKT, TOID and most recent versioninfo in CSV format.
		 * @param wrapperElement 		QName of the element defining each featuretype 
		 * @param featureAttributes 	A map of attributes to output, indexed by QName (eg. osgb:featureCode)
		 * @param output 				A writer to receive CSV output
		 * @deprecated
		 */
		public void addFeatureType(String wrapperElement, Set<String> featureAttributes, PrintStream output){
			featureTypes.put(wrapperElement, new OSFeatureTypeHandler(wrapperElement, featureAttributes,output));
			log.debug("Adding featuretype for " + wrapperElement);
		}
		
		public void addFeatureType(String wrapperElement, Map<String,String> featureAttributeMapping, String tableName){
			try {
				featureTypes.put(wrapperElement, new OSFeatureTypeHandler(wrapperElement, featureAttributeMapping, tableName));
			} catch (IOException e) {
				log.error("Database error: " + e.getMessage(), e);
			}
		}
		
		public Map<String,OSFeatureTypeHandler> getFeatureTypes(){
			return this.featureTypes;
		}
		
		@Override
		public void startElement(String uri, String localName, String name,
				Attributes attributes) throws SAXException {
			//if the element matches one of the wrapper elements, pass control to the appropriate handler
			if (featureTypes.containsKey(name)){
				log.debug("found " + name + ": passing control to handler");
				ContentHandler ch = featureTypes.get(name);
				reader.setContentHandler(ch);
				ch.startElement(uri, localName, name, attributes);
			} else log.debug("Doc Handler ignoring " + name);
		}

		@Override
		public void endDocument() throws SAXException {
			//close all the open output files
			for (Iterator<OSFeatureTypeHandler> it = featureTypes.values().iterator(); it.hasNext();) {
				log.debug("finished document, closing streams");
				OSFeatureTypeHandler fth = (OSFeatureTypeHandler) it.next();
				try {
					fth.dbOut.close();
				} catch (IOException e) {
					log.error("Error when closing DB connection for " + fth.wrapperElement, e);
				}
			}
		}
	}
	
	/**
	 * ContentHandler for a MasterMap Feature Type. One of these is created per featureType and hence one per table.
	 * @author a4709393
	 * 
	 */
	/**
	 * @author algr2
	 *
	 */
	public class OSFeatureTypeHandler extends DefaultHandler{
		
		@Deprecated
		private PrintStream out;
		private String wrapperElement;
		protected Map<String,String> featureAttributes = new LinkedHashMap<String, String>();
		private LinkedHashMap<String,String> featureAttributeDBMapping;
		private String currentElement;
		private StringBuilder elementContent;
		private boolean captureContent;
		private GMLHandler geomHandler;
		private String tableName;
		private Writer dbOut;
		
		/**
		 * @deprecated
		 */
		public OSFeatureTypeHandler(String wrapperElement, Set<String> featureAttributes2Handle, PrintStream output) {
			//create a Map which will iterate in the same order as given in fA2H containing empty Strings
			for (String featureAttribute : featureAttributes2Handle) {
				featureAttributes.put(featureAttribute, null);
			}
			this.wrapperElement = wrapperElement;
			this.out = output;
		}
		
		/**
		 * Create a new featuretype. Takes a mapping between tag and coumns for the specified featuretype.
		 * @param wrapperElement the 
		 * @param featureAttributeDBMapping a Map of tag name (qName) and column name
		 * @param tableName
		 * @throws IOException
		 */
		public OSFeatureTypeHandler(String wrapperElement, Map<String,String> featureAttributeDBMapping, String tableName) throws IOException {
			this.wrapperElement = wrapperElement;
			this.tableName = tableName;
			//ensure this will iterate in a predictable order
			this.featureAttributeDBMapping = new LinkedHashMap<String, String>(featureAttributeDBMapping);
			//create a Map which will iterate in the same order as DB mapping containing empty strings
			for (String featureAttribute : featureAttributeDBMapping.keySet()) {
				featureAttributes.put(featureAttribute, "");
			}
			Set<String> cols = new LinkedHashSet<String>(this.featureAttributeDBMapping.values());
			this.dbOut = new PostgresJDBCCopy(docHandler.database, docHandler.databaseUser, docHandler.databasePassword,
													this.tableName, cols, false);
			/*this.dbOut = new PostgresCopyWriter(docHandler.database, docHandler.databaseUser, docHandler.databasePassword,
													this.tableName, cols, false);*/
		}
		
		@Override
		public void startElement(String uri, String localName, String name,
				Attributes attributes) throws SAXException {
			//TODO: handle special case of version history
			log.debug(wrapperElement + " handler found " + name);
			if (wrapperElement.equals(name) && attributes.getIndex("fid")>=0){
				//this is the TOID
				this.featureAttributes.put("fid", attributes.getValue("fid"));
			} else if (name.startsWith("gml:")){
				//this is a geometry
				log.debug("found geom");
				geomHandler = new OSMMGeomHandler(this, name);
				reader.setContentHandler(geomHandler);
				geomHandler.startElement(uri, localName, name, attributes);
			} else if (featureAttributes.containsKey(name)){
				//this is an element we want to capture
				elementContent = new StringBuilder();
				currentElement = name;
				captureContent = true;
			}
		}
		
		
		/**
		 * Feature complete, write the feature to the output. 
		 * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
		 */
		@Override
		public void endElement(String uri, String localName, String name)
				throws SAXException {
			if (name.equals(currentElement)){
				//finished capturing element
				featureAttributes.put(name, elementContent.toString().trim());
				captureContent=false;
				currentElement="";
				log.debug(name + "=" + featureAttributes.get(name).toString());
			}
			else if (wrapperElement.equals(name)){
				log.debug("finished" + name);
				//finished this Feature so output all attributes
				/* nulls are OK
				if (featureAttributes.containsValue(null)) throw new SAXException("encountered a null value: " + featureAttributes.toString());
				 */
				StringBuilder sb = new StringBuilder();
				for (Iterator<String> it = featureAttributes.values().iterator(); it.hasNext();) {
					sb.append(it.next().toString());
					sb.append(it.hasNext() ? delimiter : lineReturn);
				}
				try {
					log.debug("writing: " + sb.toString().trim());
					if (featureCount++ % 100 == 99) log.info(featureCount + " features written");
					this.dbOut.write(sb.toString());
				} catch (IOException e) {
					log.error("Unable to write to database", e);
				} finally {
					reader.setContentHandler(docHandler);
				}
			}
		}
		
		/**
		 * Adds the characters passed, ignoring whitespace. 
		 * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
		 */
		@Override
		public void characters(char[] ch, int start, int length)
				throws SAXException {
			// also check ch aren't just whitespace
			if(captureContent && !new String(ch, start, length).trim().equals("")){
				//we want to capture these characters
				elementContent.append(ch,start,length);
			}
		}
	}
	
	/**
	 * A GMLHandler which creates JTS {@link Geometry} in British National Grid (EPSG:27700), and returns control to a parent when 
	 * geometry element ends.
	 * @author a4709393
	 */
	private class OSMMGeomHandler extends GMLHandler{
		private OSFeatureTypeHandler parentHandler;
		private String geomToHandle;
		private static final int BNG_SRID = 27700; 
		
		public OSMMGeomHandler(OSFeatureTypeHandler parentHandler, String geomToHandle) {
			super(new GeometryFactory(new PrecisionModel(),27700),null);
			this.parentHandler = parentHandler;
			this.geomToHandle = geomToHandle;
		}
		
		@Override
		public void endElement(String uri, String localName, String name)
				throws SAXException {
			super.endElement(uri, localName, name);
			if (geomToHandle.equals(name)){
				log.debug("GeomReader finished reading geom");
				//End of geometry elements, so geomHandler should have a complete JTS Geometry
				if (!this.isGeometryComplete()){
					//isGeometryComplete is true when parser has finished parsing geom, so must be true at this point 
					throw new SAXException("Invalid geometry");
				} else {
					String WKT  = this.getGeometry().toText();
					//create a PostGIS geometry
					//Have to use EWKT as PostGIS wants an SRID and COPY doesn't like CreateGeometryFromText()
					String pgGeom = "SRID=" + BNG_SRID + ";" + WKT;
					parentHandler.featureAttributes.put("geom", pgGeom);
				}
				reader.setContentHandler(parentHandler);
			}
		}
	}

	public void load(InputSource in, String database, String databaseUser, String databasePassword) throws SAXException{
		//TODO: handle appends 
		//InputSource in;
		
		this.startTime = System.currentTimeMillis();
		
		/*try {
			InputStream stream = new FileInputStream(new File(fileName));
			if (fileName.endsWith(".gz")) {
				stream = new GZIPInputStream(stream);
			}
			in = new InputSource(stream);
		} catch (FileNotFoundException e1) {
			throw new SAXException(fileName + " could not be found");
		} catch (IOException e1) {
			throw new SAXException(e1);
		}*/
		
		docHandler.database = database;
		docHandler.databaseUser = databaseUser;
		docHandler.databasePassword = databasePassword;
		
		reader = XMLReaderFactory.createXMLReader();
		reader.setContentHandler(docHandler);
		reader.setProperty("http://apache.org/xml/properties/input-buffer-size", 8);
		
/*		Set<String> cartoTxtAttribs = new HashSet<String>();
		cartoTxtAttribs.add("fid");
		cartoTxtAttribs.add("osgb:make");
		cartoTxtAttribs.add("osgb:textString");
		cartoTxtAttribs.add("osgb:physicalLevel");
		cartoTxtAttribs.add("osgb:descriptiveGroup");
		cartoTxtAttribs.add("osgb:version");
		cartoTxtAttribs.add("osgb:theme");
		cartoTxtAttribs.add("osgb:versionDate");
		cartoTxtAttribs.add("osgb:featureCode");
		cartoTxtAttribs.add("osgb:anchorPosition");
		cartoTxtAttribs.add("osgb:font");
		cartoTxtAttribs.add("osgb:height");
		cartoTxtAttribs.add("osgb:orientation");*/
		
		Map<String,String> cartoTxtDBMapping = new LinkedHashMap<String,String>();
		cartoTxtDBMapping.put("geom", "geom");
		cartoTxtDBMapping.put("fid", "fid");
		cartoTxtDBMapping.put("osgb:make", "make");
		cartoTxtDBMapping.put("osgb:textString", "text_string");
		cartoTxtDBMapping.put("osgb:physicalLevel", "physical_level");
		cartoTxtDBMapping.put("osgb:descriptiveGroup", "descriptive_group");
		cartoTxtDBMapping.put("osgb:version", "version");
		cartoTxtDBMapping.put("osgb:theme", "theme");
		cartoTxtDBMapping.put("osgb:versionDate", "version_date");
		cartoTxtDBMapping.put("osgb:featureCode", "feature_code");
		cartoTxtDBMapping.put("osgb:anchorPosition", "anchor_position");
		cartoTxtDBMapping.put("osgb:font", "font");
		cartoTxtDBMapping.put("osgb:height", "height");
		cartoTxtDBMapping.put("osgb:orientation", "orientation");
		String tableName = "cartographic_text";
		docHandler.addFeatureType("osgb:CartographicText", cartoTxtDBMapping, tableName);
	
		Map<String,String> topoAreaDBMapping = new HashMap<String, String>();	
		topoAreaDBMapping.put("geom", "geom");
		topoAreaDBMapping.put("fid", "fid");
		topoAreaDBMapping.put("osgb:make", "make");
		topoAreaDBMapping.put("osgb:descriptiveGroup", "descriptive_group");
		topoAreaDBMapping.put("osgb:descriptiveTerm", "descriptive_term");
		topoAreaDBMapping.put("osgb:version", "version");
		topoAreaDBMapping.put("osgb:theme", "theme");
		topoAreaDBMapping.put("osgb:versionDate", "version_date");
		topoAreaDBMapping.put("osgb:featureCode", "feature_code");
		topoAreaDBMapping.put("osgb:physicalLevel", "physical_level");
		topoAreaDBMapping.put("osgb:calculatedAreaValue", "calculated_area");
		docHandler.addFeatureType("osgb:TopographicArea", topoAreaDBMapping, "topographic_area");
		try {
			reader.parse(in);
		} catch (IOException e) {
			throw new SAXException("Unable to read file " + in.toString());
		} 
		log.info(String.format("loading %d features took %s", featureCount, 
				DurationFormatUtils.formatDurationHMS(System.currentTimeMillis()-startTime) 
				));
	}
	
	/**
	 * Loads OS Mastermap GML into postGIS.
	 * @param args
	 * @author a4709393
	 *//*
	public static void main(String[] args) {
		//TODO: decide about IF/usage
		String gmlUri = "C:\\Projects\\OSMM\\data\\nz\\9620-NZ1121-5c208.gz";
		SAXMapLoader2 ldr = new SAXMapLoader2();
		try {
			ldr.load(gmlUri, "mapload", "postgres", "Secre7");
		} catch (SAXException e) {
			log.fatal("Unable to load" + gmlUri, e);
		}
	}*/

}
