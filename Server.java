import java.io.*;
import java.util.*;
import java.net.*;

/** 
 * The Server class stores all of the state and functionality of the server.
 * This class can be run from the console, and should generally be run before the client class to ensure that the connection works.
 * Only one instance of the Server class is needed, even with multiple clients.
 * It contains a nested class, ClientThread, which handles all of the threading for multiple clients.
 */
public class Server {
	
	/** 
	 * The fields for the ChatServer class
	 */
	private int port;
	private ArrayList<ServerThread> activeThreads;
	private ArrayList<String> usernames;
	private ArrayList<Message> unreadMessages;
	private int clientId;
	private boolean running;
	private final String usernameFile = "usernames.txt";
	private final String messageFile = "unreadMessages.txt"; // assume this is tab separated?
	private final String delimiter = "-|::|-";
	
	/**
	 * The constructor for the ChatServer class sets the port as specified by the user 
	 * and initializes an array list to store the client threads.
	 * It also populates the array list of usernames that already exist in the app so that duplicates are not created
	 * Finally, it initializes a new Array List to store the usernames of people logged in.  Initially that is empty.
	 * @param port
	 */
	public Server(int port) {
		this.port = port;
		this.activeThreads = new ArrayList<ServerThread>();
		this.usernames = loadFromFile(usernameFile);
		this.unreadMessages = processMessages(loadFromFile(messageFile));
	}
	
	/**
	 * This method opens a text file that contains all of the usernames that have already been created.
	 * This prevents a user from creating a duplicate username and also allows for users to log in and out under the same name.
	 * Right now this text file is hardcoded in and just stored in the src folder - both bad practices!
	 * This method is only ever called in this class so is private
	 * @return	an ArrayList of usernames, which are all Strings
	 */
	private ArrayList<String> loadFromFile(String filename) {
		ArrayList<String> aList = new ArrayList<String>();
	    try  { // using BufferedReader because it's fast
	    	BufferedReader br = new BufferedReader(new FileReader(filename));
	        String line;
	        while ((line = br.readLine()) != null) { // loop through each line as long as it's not null
	        	aList.add(line);
	        }
	        br.close(); // close the BufferedReader to prevent resource leak
	    } catch(IOException e) {
	        System.out.println("Error creating BufferedReader: " + e);
	    }
	    return aList;
	}
	
	/**
	 * This method returns the list of usernames to print to users when they request to see it
	 */
	public String getUsernames() {
		return usernames.toString();
	}
	
	/**
	 * This method starts the server and keeps it running through an infinite loop to allow new connections from multiple clients.
	 * It uses a ServerSocket to wait for new connections, then a Socket for the client connections.
	 */
	public void start() {
		running = true; // need this boolean to not have an infinite loop so the ServerSocket can be closed
		try {
			ServerSocket serverSocket = new ServerSocket(port); // start the ServerSocket at the port
			// keep the server running for new connections
			while (running) {
				System.out.println("Server waiting on port : " + port); // for debugging purposes
				Socket socket = serverSocket.accept(); // accept new connection from client
				ServerThread thread = new ServerThread(socket, this); // start a new thread on the client socket
				thread.start(); // start the thread
			} 
			// this next try/catch took a while to figure out but is needed to actually close the ServerSocket and avoid resource leak
			try { 
				serverSocket.close();
			}
			catch (IOException e) {
				System.out.println("Error closing ServerSocket: " + e);
			}

		} catch (IOException e) {
			System.out.println("Error creating new ServerSocket: " + e); // add where this is happening for debugging
		}
	}
	
	/**
	 * This method adds a thread once a user has an established username.
	 * This is then used to see who's online for sending messages.
	 * @param 	thread to remove
	 */
	public void addThread(ServerThread thread) {
		activeThreads.add(thread);
	}
	
	/**
	 * This method removes a thread when a user chooses to quit the program.
	 * @param	the ServerThread to remove
	 */
	public void removeThread(ServerThread thread) {
		activeThreads.remove(thread);
	}
	
	/**
	 * This method adds a username when a new account is created. 
	 * It adds it to the list of usernames for ease of future checking
	 * Also adds it to the file storing all of the usernames so it is saved for next time the server is run
	 * @param 	username
	 */
	public void addUsername(String username) {
		usernames.add(username.toLowerCase().trim());
		addLineToFile(username, usernameFile);
	}
	
	/**
	 * This method takes in an ArrayList of messages and converts them to Message objects for easier usage, especially searching usernames.
	 * @param 	messages, the ArrayList of string messages to convert
	 * @return	an ArrayList of Message objects
	 */
	public ArrayList<Message> processMessages(ArrayList<String> messages) {
		ArrayList<Message> messageObjects = new ArrayList<Message>();
		for (int i = 0; i < messages.size(); i++) {
			String message = messages.get(i);
			String[] parts = message.split(delimiter);
			Message m  = new Message(parts[0], parts[1], parts[2]); // assuming all strings are stored in order of sender, recipient, message
			messageObjects.add(m);
		}
		return messageObjects;
	}
	
	/**
	 * This method checks for any unread messages when a user logs in or deletes their account
	 * @param username
	 */
	public boolean checkForUnreadMessages(String username) {
		int count = 0;
		for (int i = 0; i < unreadMessages.size(); i++) {
			Message m = unreadMessages.get(i);
			if (m.getRecipient().equals(username)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * This method returns all of the unread messages for a specified user.
	 * Because the messages are added to the end of the ArrayList when they are sent, 
	 * then read from the ArrayList starting at the beginning, they should be in chronological order.
	 * @param 	username searching for unread messages
	 * @return	the ArrayList of unread messages
	 */
	public ArrayList<Message> getUnreadMessages(String username) {
		ArrayList<Message> unreads = new ArrayList<Message>();
		for (int i = 0; i < unreadMessages.size(); i++) {
			Message m = unreadMessages.get(i);
			if (m.getRecipient().equals(username)) {
				unreads.add(m);
				unreadMessages.remove(i); // then remove that message from the arraylist and decrement i because the values have shifted
				i--;
			}
		}
		/**
		 * Then make sure the unreadMessages file is updated as well
		 * Ideally this code would not be here so that the user can get their unread messages quickly, 
		 * But in a non persisting server, we want to ensure the information is updated immediately.
		 */
		ArrayList<String> unreadStrings = new ArrayList<String>();
		for (Message m : unreadMessages) {
			unreadStrings.add(m.toString());
		}
		rewriteFile(messageFile, unreadStrings);
		return unreads;
	}
	
	/**
	 * This method checks if a username already exists in the list of usernames stored in the server.
	 * It is used for logging in, creating accounts, and sending messages.
	 * @param 	username
	 * @return	true if username is in list, false if not
	 */
	public boolean checkUsername(String username) {
		if (usernames.contains(username.toLowerCase())) {
			return true;
		}
		return false;
	}
	
	/**
	 * This method removes a username from the server's list when a user asks to delete it.
	 * It then calles rewriteFile() to remove the username from the stored file
	 * @param username
	 */
	public void removeUser(String username) {
		usernames.remove(username);
		rewriteFile(usernameFile, usernames);
	}
	
	/**
	 * This method rewrites the stored files when items should be removed from them.
	 * In particular, it's used when user accounts are deleted and when unread messages are sent.
	 * The method creates a temp file, writes the new arrayList to that file, then deletes the old one and renames the temp
	 * @param 	f, the file to rewrite
	 * @param 	al, the arraylist of Strings to write in (with the unwanted messages missing)
	 */
	public void rewriteFile(String f, ArrayList<String> al) {
		FileWriter fileWriter;
		File newFile = new File("temp" + f);
		try {
			fileWriter = new FileWriter(newFile);
			PrintWriter printWriter = new PrintWriter(fileWriter);
			for (int i = 0; i < al.size(); i++) {
				printWriter.println(al.get(i));
			}
			
			// then delete the old file and rename the temp file
			File oldFile = new File(f);
			oldFile.delete();
			newFile.renameTo(oldFile);
			printWriter.close();
		}
		catch (IOException e) {
			System.out.println("Error rewriting " + f + ": " + e);
		} 
	}
	
	/**
	 * This method opens a file and adds a line to the end of it
	 * It is used to store new usernames when an account is created and to store unsent messages
	 * @param 	line, the line to add to the file
	 * @param 	filename, the file to add the line to
	 */
	public void addLineToFile(String line, String filename) {
	    FileWriter fileWriter;
		try {
			fileWriter = new FileWriter(filename, true); //Set true for append mode
		    PrintWriter printWriter = new PrintWriter(fileWriter);
		    printWriter.println(line);  //New line
		    printWriter.close();
		} catch (IOException e) {
			System.out.println("Error saving " + line + " to " + filename + ": " + e);
		} 
	}
	
	/**
	 * This method checks to see if a user is online based on the active threads that are being stored.
	 * It goes through each thread and checks the username to see if it matches the one to search for.
	 * It returns the thread to 
	 * @param 	username to look for
	 * @return	true if the user is online, false if not
	 */
	
	public ServerThread checkThreads(String username) {
		for (int i = 0; i < activeThreads.size(); i++) {
			ServerThread thread = activeThreads.get(i);
			if (thread.getUsername().equals(username)) { 
				return thread;
			}
		}
		return null;
	}
	
	/**
	 * This method sends a message to a user who is in the database
	 * @param 	sender, who sent the message
	 * @param 	recipient, who is supposed to receive the message
	 * @param 	message, the text to be sent
	 * @return	boolean, true if the user is online and false if not to alert the recipient.
	 */
	public boolean sendMessage(String sender, String recipient, String message) {
		ServerThread thread = checkThreads(recipient.toLowerCase());
		if (thread != null) { // if  the user is online
			thread.sendMessage(sender, message);
			return true;
		}
		else { // otherwise store it in unread messages so it can be sent later when the user logs in
			Message m = new Message(sender, recipient, message.replaceAll("\n", ""));
			unreadMessages.add(m);
			String messageString = sender + delimiter + recipient + delimiter + message; // use format dictated for the file
			addLineToFile(messageString, messageFile);
			return false;
		}
	}
	
	/**
	 * The main method is run immediately when the ChatServer class is run from the console.
	 * It starts the server at the port specified by the user (or the default if none is specified).
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length != 1) {
			System.err.println("Usage: java Server <port number>");
			System.exit(1);
		}

		int port = 0;
		
		try {
			port = Integer.parseInt(args[0]);
		}
			
		catch (NumberFormatException e) {
			System.err.println("Enter an integer port number. Usage is: java Server <port>");
			System.exit(1);
		}
		
		// might want to add a try/catch around this
		Server server = new Server(port);
		server.start();
	}
	
}
