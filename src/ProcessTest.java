import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.apache.log4j.Logger;


public class ProcessTest {

	public static final Logger log = Logger.getLogger(ProcessTest.class
			.getName());
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		//Runtime rt = Runtime.getRuntime();
		
		try {
/*			Process p1 = rt.exec(
					new String[] {
							"C:\\Program Files\\PostgreSQL\\8.3\\bin\\psql.exe",
							"-U postgres",
							"-c \"COPY (SELECT 'hello' AS text) TO stdout;\"",
							"-d smartmap"
					},
					new String[] {"PGPASSWORD=Secre7", "PGPASSWD=Secre7"}
			);*/
			
			ProcessBuilder pb = new ProcessBuilder("psql.exe",
													"-Upostgres",
													//"-c COPY (SELECT 'hello' AS text) TO stdout;",
													"-dsmartmap");
			pb.environment().put("PGPASSWORD", "Secre7");
			pb.directory(new File("C:\\Program Files\\PostgreSQL\\8.3\\bin\\"));
			Process p1 = pb.start();
			
			Writer stdin = new BufferedWriter(new OutputStreamWriter(p1.getOutputStream()));
			stdin.write("COPY (SELECT 'gjgjhg' AS text) TO stdout;");
			stdin.flush();
			stdin.close();
			
			StringBuffer outputBuffer = new StringBuffer();
			StringBuffer errBuffer = new StringBuffer();
			
			new InputStreamHandler(outputBuffer, p1.getInputStream());
			new InputStreamHandler(errBuffer, p1.getErrorStream());
			
			//waitFor returns the exit code of the process. 0 == OK.
			log.debug(p1.waitFor());
			
			log.debug("psql says: " + outputBuffer.toString());
			log.debug("psql Error: " + errBuffer.toString());
			
			
			
			/*BufferedReader reader1 = new BufferedReader(new InputStreamReader(p1.getInputStream()));
			BufferedReader errReader1 = new BufferedReader(new InputStreamReader(p1.getErrorStream()));
			String line;
			System.out.println("psql says:");
			while ((line = reader1.readLine()) != null) {
			    System.out.println(line);
			}
			System.out.println("psql errors:");
			while ((line = errReader1.readLine()) != null) {
			    System.out.println(line);
			}
			
			reader1.close();
			errReader1.close();
			*/
			//System.out.printf("exit value: %d", p1.exitValue());
		} catch (Exception e) {
			e.printStackTrace();
			log.fatal("Exception", e);
		}
	}

}
