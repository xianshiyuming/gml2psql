import org.geotools.gml2.GMLConfiguration;
import org.geotools.xs.XSConfiguration;
import org.geotools.xml.Configuration;


public class ApplicationSchemaConfiguration2 extends
		Configuration {

	    public ApplicationSchemaConfiguration2(String namespace, String schemaLocation) {
	    	super(new ApplicationSchemaXSD2(namespace, schemaLocation));
	        addDependency(new XSConfiguration());
	        addDependency(new GMLConfiguration());
	    }

//	    protected void registerBindings(MutablePicoContainer container) {
//	        //no bindings
//	    }

}
