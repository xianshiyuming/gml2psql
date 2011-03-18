import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.zip.GZIPInputStream;

import org.apache.log4j.Logger;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * A threaded loader for OSMasterMap GML files. 
 * @author a4709393
 *
 */
public class MasterMapLoader {
	public static final Logger log = Logger.getLogger(MasterMapLoader.class
			.getName());
	
	private BlockingQueue<InputSource> fileQueue;
	private List<InputSource> files; //synchronised list of specific files to be processed
	private static String userName;
	private static String password;
	private static String database;
	
	public class FileLoader implements Runnable{
		private InputSource src;
		public FileLoader(InputSource src){
			this.src = src;
		}
		public void run(){
			try {
				(new SAXMapLoader2()).load(src, MasterMapLoader.database, MasterMapLoader.userName, MasterMapLoader.password);
			} catch (SAXException e) {
				log.error("error loading" + src.toString(), e);
			}
		}
	}
	
	/**
	 * Converts a list of gml files or directories containing gml files into a list of InputSources. Calls itself recursively for each directory.
	 * @param files		a List of gml Files or directories containg gml files. 
	 * @return			a List of input sources wrapping the files
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	static private List<InputSource> ConvertToInputSources(List<File> files) throws FileNotFoundException, IOException{
	    List<InputSource> result = new ArrayList<InputSource>();
	    for (File file : files) {
			if (!file.isFile()){
				//is a directory, so make a recursive call
				result.addAll(ConvertToInputSources(Arrays.asList(file.listFiles(
						//filter returning only .gz and .gml files
						new FileFilter(){
							public boolean accept(File file){
								return (file.getName().endsWith(".gz")||file.getName().endsWith(".gml"));
							}
						}
					)))
				);
			} else {
				//is a file
				InputStream stream = new FileInputStream(file);
	  			if (file.getName().endsWith(".gz")) {
	  				//is a gzipped file
	  				stream = new GZIPInputStream(stream);
	  			}
	  			result.add(new InputSource(stream));
	  		} 
		}
	    return result;
	}
	
	public MasterMapLoader(String database, String username, String password){
		this.database=database;
		this.userName=username;
		this.password=password;
	}
	
	public void load(List<File> files) throws FileNotFoundException, IOException{
		load(files, 2);
	}
	
	/**
	 * Load a list of files into the database. Number of worker threads to concurrently load files may be specified.
	 * @param files		List of File objects to load. May be directories containing GML files, or specific files. 
	 * @param threads	int representing the number of worker threads to load files in. 
	 */ 
	public void load(List<File> files, int threads) throws FileNotFoundException, IOException{
		ExecutorService pool = java.util.concurrent.Executors.newFixedThreadPool(threads);
		for (InputSource is : ConvertToInputSources(files)) {
			pool.submit(new FileLoader(is));
		}
		pool.shutdown();
	}

}
