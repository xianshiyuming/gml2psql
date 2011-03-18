import org.geotools.gml3.ApplicationSchemaXSD;
import org.eclipse.xsd.XSDSchema;
import java.io.File;
import org.geotools.xml.SchemaLocationResolver;

public class ApplicationSchemaXSD2 extends ApplicationSchemaXSD {

    public ApplicationSchemaXSD2(String namespaceURI, String schemaLocation) {
        super(namespaceURI,schemaLocation);
    }


    public SchemaLocationResolver createSchemaLocationResolver() {
        return new SchemaLocationResolver(this) {
                public String resolveSchemaLocation(XSDSchema schema, String uri, String location) {
                    String schemaLocation;

                    if (schema == null) {
                        schemaLocation = getSchemaLocation();
                    } else {
                        schemaLocation = schema.getSchemaLocation();
                    }

                    String locationUri = null;

                    if ((null != schemaLocation) && !("".equals(schemaLocation))) {
                        String schemaLocationFolder = schemaLocation;
                        int lastSlash = schemaLocation.lastIndexOf('/');

                        if (lastSlash > 0) {
                            schemaLocationFolder = schemaLocation.substring(0, lastSlash);
                        }

                        if (schemaLocationFolder.startsWith("file:")) {
                        	//TODO: I've set to 6 for Windows
                            schemaLocationFolder = schemaLocationFolder.substring(5);
                        }
                        //here's the error
                        File locationFile = new File(schemaLocationFolder, location);

                        if (locationFile.exists()) {
                            locationUri = locationFile.toURI().toString();
                        }
                    }

                    if ((locationUri == null) && (location != null) && location.startsWith("http:")) {
                        locationUri = location;
                    }

                    return locationUri;
                }
            };
    }

}
