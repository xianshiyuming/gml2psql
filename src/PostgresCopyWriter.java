import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;


@Deprecated
public class PostgresCopyWriter extends Writer  {
	public static final Logger log = Logger.getLogger(PostgresCopyWriter.class
		.getName());
	private Writer output;
	private Process psql;
	private StringBuffer stderr;
	private StringBuffer stdout;
	private Thread stdoutHandler;
	private Thread stderrHandler;
	private String tempTableName;
	private String tableName;
	
	/**
	 * Create a writer to the database. Make sure to ensure the process exits.
	 * @param database
	 * @param username
	 * @param password
	 * @param tableName
	 * @param cols			a Set of column names in the order in which the fields will be written.
	 * @param update 		a boolean indicating whether to replace existing features with the with later versions
	 * @throws IOException	Exception thrown if psql exited with errors.
	 */
	public PostgresCopyWriter(String database, String username, String password, String tableName, Set<String> cols, boolean update) throws IOException {
		tempTableName = "tmp_" + org.apache.commons.lang.RandomStringUtils.randomAlphanumeric(6);
		this.tableName = tableName;
		StringBuilder sb = new StringBuilder();
		//create a temp table with random name with to store input, which will include dupe TOIDS/PK
		sb.append("CREATE TABLE ");
		sb.append(tempTableName);
		sb.append(" ( LIKE ");
		sb.append(tableName);
		sb.append(" );");
		sb.append("COPY ");
		sb.append(tempTableName);
		sb.append(" (");
		for (Iterator<String> iterator = cols.iterator(); iterator.hasNext();) {
			String colName = iterator.next().toString();
			sb.append(colName);
			if (iterator.hasNext()) sb.append(", ");
		}
		//delimiter cannot be commas is these are used in the PostGIS GeomFromText function
		sb.append(") FROM STDIN WITH DELIMITER AS '|';");
		String copyQry = sb.toString();
		log.debug("built query: " + copyQry);
		
		ProcessBuilder pb = new ProcessBuilder("C:\\Program Files\\PostgreSQL\\8.3\\bin\\psql.exe",
												"-U" + username,
												"-c " + copyQry,
												"-d" + database);
		//TODO: safer to output this to stdin
		pb.environment().put("PGPASSWORD", password);
		//send errors to stdout
		//pb.redirectErrorStream(true);
		//pb.directory(new File("C:\\Program Files\\PostgreSQL\\8.3\\bin\\"));
		this.psql = pb.start();
		this.output = new BufferedWriter(new OutputStreamWriter(psql.getOutputStream()));
		this.stdout = new StringBuffer();
		this.stderr = new StringBuffer();
		this.stdoutHandler = new InputStreamHandler(this.stdout, psql.getInputStream()); 
		this.stderrHandler = new InputStreamHandler(this.stderr, psql.getErrorStream());
	}

	/**
	 * Close the stdin stream, copy non duplicate features from temp table and exit the psql process.
	 */
	@Override
	public void close() throws IOException {
		//close COPY output
		log.debug("closing output");
		this.output.write("\\.\n");
		//copy from temp table into tableName
		StringBuilder sb = new StringBuilder();
		sb.append("INSERT INTO ");
		sb.append(this.tableName);
		sb.append(" SELECT * FROM ");
		sb.append(this.tempTableName);
		sb.append(" WHERE fid NOT IN (SELECT fid FROM ");
		sb.append(this.tableName); 
		sb.append("); ");
		this.output.write(sb.toString());
		log.debug(sb.toString());
		this.output.close();
		int exitCode;
		try {
			//waitFor returns the exit code of the process. 0 == OK.
			exitCode = psql.waitFor();
		} catch (InterruptedException e) {
			throw new IOException(e.getMessage()); 
		}
		if (exitCode != 0) throw new IOException("psql exited with error code: " + exitCode + 
												". Error was: " + stderr.toString()); 
		log.debug("psql exited with error code: " + exitCode);
		log.debug("psql says: " + stdout.toString());
		log.debug("psql Error: " + stderr.toString());
	}

	@Override
	public void flush() throws IOException {
		this.output.flush();
	}

	@Override
	public void write(char[] cbuf, int off, int len) throws IOException {
		this.output.write(cbuf, off, len);
	}

	@Override
	public void write(String str) throws IOException {
		this.output.write(str);
	}
}
