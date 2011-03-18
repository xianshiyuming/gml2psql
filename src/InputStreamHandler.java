import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Logger;

/**
 * A non-blocking handler for reading a {@link Process}' stdout and stderr.  
 * From {@link http://oreilly.com/pub/h/1092}: 
 * When reading any stream the java methods will block when nothing is available and the stream is left open.
 * When you have a started a process using Runtime.getRuntime().exec(command) you will may find that if you try 
 * to read either the stream returned by getErrorStream or the stream returned by getInputStream your application
 *  will hang. This happens when data has been sent to the stream you are not reading and no further input comes 
 *  on the stream you are reading (e.g. an error message sent to the error stream while you are reading the input stream).
 *  As there is data waiting to be read from one of the streams the process will not exit and close the stream you are 
 *  reading, so your application will hang waiting for data that will never arrive.
 *  
 *  Example application code:
 *  <code>
 *  Process application = Runtime.getRuntime().exec(command);
 *  
 *  StringBuffer inBuffer = new StringBuffer();
 *  InputStream inStream = application.getInputStream();
 *  new InputStreamHandler( inBuffer, inStream );
 *  
 *  StringBuffer errBuffer = new StringBuffer();
 *  InputStream errStream = application.getErrorStream();
 *  new InputStreamHandler( errBuffer , errStream );
 *  
 *  application.waitFor();
 * </code>
 * 
 * @author Al Sutton
 * @author a4709393
 * 
 */
public class InputStreamHandler extends Thread {
	public static final Logger log = Logger.getLogger(InputStreamHandler.class
			.getName());
	/**
	 * Stream being read
	 */
	private InputStream m_stream;

	/**
	 * The StringBuffer holding the captured output
	 */
	private StringBuffer m_captureBuffer;

	/**
	 * Constructor.
	 * 
	 * @param captureBuffer 	the StringBuffer holding the capture output
	 * @param stream			the InputStream being read
	 */		
	public InputStreamHandler( StringBuffer captureBuffer, InputStream stream ){
		m_stream = stream;
		m_captureBuffer = captureBuffer;
		start();
	}

	/**
	 * Stream the data.
	 */
	@Override
	public void run()
	{
		try
		{
			int nextChar;
			while( (nextChar = m_stream.read()) != -1 ){
				m_captureBuffer.append((char)nextChar);
			}
		}catch( IOException ioe ){
			//do nothing
			log.error("IOException caught while streaming data",ioe);
		}
	}
}
