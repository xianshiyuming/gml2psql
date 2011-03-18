package dto;

import java.util.HashMap;
import java.util.Map;

/**
 * A flattened Cartographic Text object ready for writing to textfile
 * 
 * @author a4709393
 * 
 */
public class CartographicText {
	private String make = "";
	private String textstring = "";
	private String physicallevel = "";
	private String descriptivegroup = "";
	private String descriptiveterm = "";
	private String version = "";
	private String theme = "";
	private String versiondate = "";
	private String ogc_fid = "";
	private String wkb_geometry = "";
	private String featurecode = "";
	private String anchorposition = "";
	private String font = "";
	private String height = "";
	private String orientation = "";
	private String delimiter = ",";
	private String lineReturn = "\n";
	
	private Map attribs = new HashMap();
	
	public CartographicText() {
		attribs.put("anchorPosition", null);
	}
	
	public Map getAttribs(){
		return this.attribs;
	}

	public void setAttrib(Object key,Object val){
		this.attribs.put(key, val);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(make); sb.append(delimiter);
		sb.append(textstring); sb.append(delimiter);
		sb.append(physicallevel); sb.append(delimiter);
		sb.append(descriptivegroup); sb.append(delimiter);
		sb.append(descriptiveterm); sb.append(delimiter);
		sb.append(version); sb.append(delimiter);
		sb.append(theme); sb.append(delimiter);
		sb.append(versiondate); sb.append(delimiter);
		sb.append(ogc_fid); sb.append(delimiter);
		sb.append(wkb_geometry); sb.append(delimiter);
		sb.append(featurecode); sb.append(delimiter);
		sb.append(anchorposition); sb.append(delimiter);
		sb.append(font); sb.append(delimiter);
		sb.append(height); sb.append(delimiter);
		sb.append(orientation); sb.append(lineReturn);
		return sb.toString();
	}

	public String getMake() {
		return make;
	}

	public void setMake(String make) {
		this.make = make;
	}

	public String getTextstring() {
		return textstring;
	}

	public void setTextstring(String textstring) {
		this.textstring = textstring;
	}

	public String getPhysicallevel() {
		return physicallevel;
	}

	public void setPhysicallevel(String physicallevel) {
		this.physicallevel = physicallevel;
	}

	public String getDescriptivegroup() {
		return descriptivegroup;
	}

	public void setDescriptivegroup(String descriptivegroup) {
		this.descriptivegroup = descriptivegroup;
	}

	public String getDescriptiveterm() {
		return descriptiveterm;
	}

	public void setDescriptiveterm(String descriptiveterm) {
		this.descriptiveterm = descriptiveterm;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getTheme() {
		return theme;
	}

	public void setTheme(String theme) {
		this.theme = theme;
	}

	public String getVersiondate() {
		return versiondate;
	}

	public void setVersiondate(String versiondate) {
		this.versiondate = versiondate;
	}

	public String getOgc_fid() {
		return ogc_fid;
	}

	public void setOgc_fid(String ogc_fid) {
		this.ogc_fid = ogc_fid;
	}

	public String getWkb_geometry() {
		return wkb_geometry;
	}

	public void setWkb_geometry(String wkb_geometry) {
		this.wkb_geometry = wkb_geometry;
	}

	public String getFeaturecode() {
		return featurecode;
	}

	public void setFeaturecode(String featurecode) {
		this.featurecode = featurecode;
	}

	public String getAnchorposition() {
		return anchorposition;
	}

	public void setAnchorposition(String anchorposition) {
		this.anchorposition = anchorposition;
	}

	public String getFont() {
		return font;
	}

	public void setFont(String font) {
		this.font = font;
	}

	public String getHeight() {
		return height;
	}

	public void setHeight(String height) {
		this.height = height;
	}

	public String getOrientation() {
		return orientation;
	}

	public void setOrientation(String orientation) {
		this.orientation = orientation;
	}

	public String getDelimiter() {
		return delimiter;
	}

	public void setDelimiter(String delimiter) {
		this.delimiter = delimiter;
	}

	public String getLineReturn() {
		return lineReturn;
	}

	public void setLineReturn(String lineReturn) {
		this.lineReturn = lineReturn;
	}

}
