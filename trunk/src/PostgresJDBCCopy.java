import java.io.IOException;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.io.Reader;
import java.io.Writer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import org.apache.log4j.Logger;
import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;


public class PostgresJDBCCopy extends PipedWriter{
	
	public static Logger log = Logger.getLogger(PostgresJDBCCopy.class
			.getName());
	private String tempTableName;
	private String tableName;
	private Connection conn;
	private Reader reader; 
	private Writer writer;
	
	/**
	 * Create a writer to the database. 
	 * @param database
	 * @param username
	 * @param password
	 * @param tableName
	 * @param cols			a Set of column names in the order in which the fields will be written.
	 * @param update 		a boolean indicating whether to replace existing features with the with later versions
	 * @throws IOException	Exception thrown if psql exited with errors.
	 */
	public PostgresJDBCCopy(String database, String username, String password, String tableName, Set<String> cols, boolean update) throws IOException {
		// this needs to be an instance of PipedWriter
		super();
		tempTableName = "tmp_" + org.apache.commons.lang.RandomStringUtils.randomAlphanumeric(6);
		this.tableName = tableName;
		
		try {
			//load the driver
			Class.forName("org.postgresql.Driver");
		} catch (ClassNotFoundException e) {
			throw new IOException(e);
		}
		//build the connection string
		String connectionString = String.format("jdbc:postgresql://%s:%s/%s", "localhost", "5432", database);
		Properties connProps = new Properties();
		connProps.setProperty("user", username);
		connProps.setProperty("password", password);
		try {
			//make the connection
			log.info("connecting to " + connectionString);
			log.info("with properties: " + connProps.toString());
			this.conn=DriverManager.getConnection(connectionString, connProps);
		} catch (SQLException e) {
			throw new IOException(e);
		}
		try {
			//create the tables
			/*PreparedStatement createTable =  conn.prepareStatement("CREATE TABLE ? ( LIKE ? )");
			createTable.setString(1, tempTableName);
			createTable.setString(2, this.tableName);
			createTable.executeUpdate();*/
			Statement createTable = conn.createStatement();
			StringBuilder sb = new StringBuilder();
			//create a temp table with random name with to store input, which will include dupe TOIDS/PK
			sb.append("CREATE TABLE ");
			sb.append(tempTableName);
			sb.append(" ( LIKE ");
			sb.append(tableName);
			sb.append(" );");
			createTable.execute(sb.toString());
			//convert writer to reader
			reader = new PipedReader(this);
			//this is postgres specific: cast to a postgres connection
			CopyManager copier = new CopyManager((BaseConnection)conn);
			StringBuilder copyBld = new StringBuilder();
			copyBld.append("COPY ");
			copyBld.append(tempTableName);
			copyBld.append(" (");
			for (Iterator<String> iterator = cols.iterator(); iterator.hasNext();) {
				String colName = iterator.next().toString();
				copyBld.append(colName);
				if (iterator.hasNext()) copyBld.append(", ");
			}
			//delimiter cannot be comma is these are used in the PostGIS GeomFromText function
			copyBld.append(") FROM STDIN WITH DELIMITER AS '|'");
			String copyQry = copyBld.toString();
			log.debug("built query: " + copyQry);
			new PostgresJDBCCopyThread(conn, copyQry, reader).run();
		} catch (SQLException e) {
			throw new IOException(e);
		}
	}

	@Override
	public void close() throws IOException {
		try{
			//copy from temp table into tableName
			StringBuilder sb = new StringBuilder();
			sb.append("INSERT INTO ");
			sb.append(this.tableName);
			sb.append(" SELECT * FROM ");
			sb.append(this.tempTableName);
			sb.append(" WHERE fid NOT IN (SELECT fid FROM ");
			sb.append(this.tableName); 
			sb.append("); ");
			Statement st = conn.createStatement();
			st.execute(sb.toString());
			log.debug(sb.toString());
			st.close();
			//delete the temp table
			StringBuilder drop = new StringBuilder();
			drop.append("DROP TABLE ");
			drop.append(this.tempTableName);
			drop.append(";");
			Statement dropStatement = conn.createStatement();
			dropStatement.execute(drop.toString());
			dropStatement.close();
			//close the database connection
			if (conn!=null) conn.close();
		} catch (SQLException e){
			throw new IOException(e);
		} finally{
			super.close();
		}
	}

}
