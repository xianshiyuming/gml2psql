import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
	 * Commandline utility to load OS MasterMap GML files onto PostGIS. This class parses commandline arguments and 
	 * passes to an instnce of MasterMapLoader.
	 * @param args
	 * @author a4709393
	 */
public class osmm2psql {

	private static Options options = new Options();
	public static final Option username = new Option("u", "user", true, 
			"postgres username [default postgres]");
	public static final Option db = new Option("db", true, "PostGIS database to load data into");
	public static final Option pw = new Option("pw", "password", true, "database password [default load from PG_PASSWORD]");
	public static final Option psql = new Option("psql", true, "path to psql.exe [default load from path]");
	public static final Option help = new Option("h", "help", false, "show this info");
	public static final Option threads = new Option("t", "threads", true, 
			"number of threads to use to read files [default 2]");
	static{
		options.addOption(help);
		options.addOption(username);
		options.addOption(db);
		options.addOption(pw);
		options.addOption(psql);
		options.addOption(threads);
	}
	
	private CommandLine cmd;
	private List<File> files = new ArrayList<File>();
		
	public void loadArgs(String[] args){
		CommandLineParser parser = new GnuParser();
		try {
			cmd=parser.parse(options, args);
		} catch (ParseException e) {
			System.err.println("error parsing arguments");
			e.printStackTrace();
			System.exit(1);
		}
		
		if (cmd.hasOption("h")){
			//print usage message
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("java -jar osmm2psql OPTIONS file|directory [file2|directory2]...", options);
		} else if(cmd.hasOption(pw.getOpt())) {
			//check required options
			
			
			//put the file/directory names into an array of Files 
			for (String file : cmd.getArgs()) {
				files.add(new File(file));
			}
			//load the files
			MasterMapLoader maploader = new MasterMapLoader(cmd.getOptionValue(db.getOpt()),
					cmd.getOptionValue(username.getOpt()),cmd.getOptionValue(pw.getOpt()));
			//try/catch
			try {
				maploader.load(files);
			} catch (FileNotFoundException e) {
				System.out.printf("%s was not found", e.getMessage());
			} catch (IOException e) {
				System.out.println("Error loading files");
			}
		}
	}
	
	public static void main(String[] args) {
		osmm2psql osmm2psql = new osmm2psql();
		osmm2psql.loadArgs(args);
	}

}
