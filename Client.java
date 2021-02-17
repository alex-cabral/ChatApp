/**
 * This class controls the user end of the application. It connects to the server via the specified host and port.
 * To avoid issues with timing and locking, the client starts a thread to connect to for receiving messages from the server.
 * We found that with just one thread, we were able to avoid locking issues, so send to the server directly from this class.
 */

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {

	/**
	 * The main method runs immediately at program startup. It ensures the user runs the class correctly and parses for the
	 * hostname and port.
	 * @param 	args, input by the user
	 */
	public static void main(String[] args) {
		if (args.length != 2) { 
			System.err.println("Usage: java Client <host> <port>");
			System.exit(1);
		}
		String host = args[0];
		int port = Integer.parseInt(args[1]);
		execute(host, port);
	}
	
	/**
	 * This method starts the socket and thread to communicate with the server. 
	 * It also initializes a loop to keep the program running and encodes input from the user to send to the server.
	 * @param 	host
	 * @param 	port
	 */
	public static void execute(String host, int port) {
		Protocol p = new Protocol(); // for encoding user input according to wire protocol
		try{
	        Socket socket = new Socket(host, port);
	        DataOutputStream out = new DataOutputStream(socket.getOutputStream()); //outputs to the server
	        ClientThread listener = new ClientThread(socket); 
	        listener.start(); // handle listening from the server

	        Scanner scanner = new Scanner(System.in);
	        String userInput;
	        
	        
	        while((userInput = scanner.nextLine()) != null) {
	        	byte[] userInputBytes = p.encode(userInput);
	        	char type = 's'; // in our case, we are always sending strings
	        	out.writeChar(type);
	        	out.writeInt(userInputBytes.length);
	        	out.write(userInputBytes);
	            out.flush();
	            if (userInput.equals("QUIT")) {
	            	listener.stopRunning();
	            	System.out.println(">> Thanks for using the chat app!");
	            	listener.sleep(1000);
	            	break;
	            }
	        }
	        
	        //close the streams to prevent resource leaks
	        out.close();
	        socket.close();
	        scanner.close();
            listener.interrupt();

	    }catch(Exception e){
	        e.printStackTrace();
	    }   
	}
}
