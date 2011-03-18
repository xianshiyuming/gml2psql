import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;

/**
 * 
 */

/**
 * @author algr2
 *
 */
public class PostgresJDBCCopyThread implements Runnable {

	public static final Logger log = Logger.getLogger(PostgresJDBCCopyThread.class
			.getName());
	private Connection conn;
	private String stmt;
	private Reader in;
	public PostgresJDBCCopyThread(Connection conn, String stmt, Reader in) throws SQLException, IOException {
		this.conn = conn;
		this.stmt = stmt;
		this.in = in;
	}

	@Override
	public void run() {
		try {
			//Connection is threadsafe
			CopyManager copier = new CopyManager((BaseConnection)conn);
			//perform the copy, returning number of rows updated
			long rows = copier.copyIn(stmt, in);
			log.debug("finished copy");
			log.info(String.format("%d rows updated", rows));
		} catch (Exception e){
			log.error("error while copying", e);
		}
	}

}
