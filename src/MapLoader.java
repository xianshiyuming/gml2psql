import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.gml3.ApplicationSchemaConfiguration;
import org.opengis.feature.simple.SimpleFeature;
import org.xml.sax.SAXException;

import com.vividsolutions.jts.geom.Geometry;

public class MapLoader {
	private static Log log = LogFactory.getLog(MapLoader.class);
/*
 * <osgb:topographicMember>
		<osgb:TopographicArea fid='1000000072189367'>
			<osgb:featureCode>10056</osgb:featureCode>
			<osgb:version>7</osgb:version>
			<osgb:versionDate>2003-07-24</osgb:versionDate>
			<osgb:theme>Land</osgb:theme>
			<osgb:calculatedAreaValue>
				114447.800368
			</osgb:calculatedAreaValue>
			<osgb:changeHistory>
				<osgb:changeDate>2000-11-24</osgb:changeDate>
				<osgb:reasonForChange>New</osgb:reasonForChange>
			</osgb:changeHistory>
			...
			<osgb:descriptiveGroup>
				General Surface
			</osgb:descriptiveGroup>
			<osgb:make>Natural</osgb:make>
			<osgb:physicalLevel>50</osgb:physicalLevel>
			<osgb:polygon>
				<gml:Polygon srsName='osgb:BNG'>
					<gml:outerBoundaryIs>
						<gml:LinearRing>
							<gml:coordinates>
								435732.800,522979.690
								435725.630,522973.470
								...
								435732.800,522979.690
							</gml:coordinates>
						</gml:LinearRing>
					</gml:outerBoundaryIs>
				</gml:Polygon>
			</osgb:polygon>
		</osgb:TopographicArea>
	</osgb:topographicMember>
 * 
 * -stream by topographicMember
 * -queries will be for a particular feature type (e.g. TopographicArea)
 * -csv file
 * COPY TO @featureTable FROM STDIN
 * fid|featurecode|version|versiondate|theme|calculatedareavalue|changedate|reasonforchage|descriptivegroup|make|physicallevel|geom
 * 
 */
	
	public MapLoader() {
		
		
		try {
			//create the parser with the gml 2.0 configuration
			//org.geotools.xml.Configuration configuration = new org.geotools.gml2.GMLConfiguration();
			
			//create the parser with the filter 1.0 configuration
			String namespace = "http://www.ordnancesurvey.co.uk/xml/namespaces/osgb";
			String schemaLocation =  getClass().getResource("OSDNFFeatures.xsd").toString();
log.debug("schemalocation :" + schemaLocation);
			org.geotools.xml.Configuration configuration = new ApplicationSchemaConfiguration2( namespace, schemaLocation );
//			org.geotools.xml.Parser parser = new org.geotools.xml.Parser( configuration );


			//the xml instance document
			InputStream xml = new FileInputStream("c:\\projects\\workspace\\MapLoader\\src\\9620-NZ3622-5i376.xml");

			//instantiate the streaming parser
			org.geotools.xml.StreamingParser parser = new org.geotools.xml.StreamingParser( configuration, xml, SimpleFeature.class );

			SimpleFeature f = null;
			
			while( ( f=(SimpleFeature) parser.parse() ) != null ) {
				Geometry geom = (Geometry) f.getDefaultGeometry();
				log.debug(geom.toText());
				String fid = (String) f.getAttribute( "fid" );
				log.debug(fid);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		new MapLoader();
	}
}
