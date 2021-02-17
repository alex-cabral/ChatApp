/**
 * The protocol class contains the state and functionality for our wire protocol.
 * The basis of the wire protocol is to encode the Strings as bytes then decode them back to Strings
 * The encoding includes one byte for the data type (always a string), 4 bytes for an integer to store the length of the data,
 * and N bytes to hold the value of the data
 */
import java.io.*;
import java.nio.charset.Charset;

public class Protocol {
	
	/**
	 * We hard coded in the charset for encoding so that local defaults don't mess things up.
	 */
	private final Charset charset = Charset.forName("ASCII");
	
	/**
	 * This method encodes a string based on the byte encoding we're using for the protocol
	 * @param 	s, the string to encode
	 * @return	the string encoded as bytes
	 */
	public byte[] encode(String s) {
		char type = 's'; // s for string
		byte[] dataInBytes = s.getBytes(charset);
		return dataInBytes;
	}
	
	/** 
	 * This methods decodes a DataInputStream storing bytes back into a string based on the encoding
	 * @param 	stream, the DataInputStream received from the client
	 * @return	str, the String that the client originally typed
	 * @throws 	IOException 
	 */
	public String decode(DataInputStream stream) throws IOException {
		char dataType = stream.readChar();
		int length = stream.readInt();
		String str = "";
		
		/**
		 * We know for our purposes that the data type is always a string, but this allows for more flexibility in the future.
		 */
		if(dataType == 's') {
		    byte[] messageByte = new byte[length];
		    
		    /**
		     * We need this boolean and the while loop to read the data in chunks because there is not guarantee that all bytes 
		     * will get read in at once.
		     */
		    boolean end = false; 
		    StringBuilder dataString = new StringBuilder(length);
		    int totalBytesRead = 0;
		    while(!end) {
		        int currentBytesRead = stream.read(messageByte); // read can only read in chunks at a time
		        totalBytesRead = currentBytesRead + totalBytesRead;
		        if(totalBytesRead <= length) {
		            dataString.append(new String(messageByte, 0, currentBytesRead, charset));
		        } else {
		            dataString.append(new String(messageByte, 0, length - totalBytesRead + currentBytesRead, charset));
		        }
		        if(dataString.length()>=length) { // stop once the entire string is rebuilt
		            end = true;
		        }
		    }
		    str = dataString.toString(); // convert to a String
		}
		return str;
	}

}
