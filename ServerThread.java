import java.io.*;
import java.net.*;
import java.util.ArrayList;


/**
	 * The ClientThread class controls the connection for each client to the server via a Socket.
	 * Each time a new client is connected, a new instance of ClientThread is created.
	 * Because it uses threading, it extends the Thread class.
	 */
	public class ServerThread extends Thread{
		private int id;
		private Socket socket;
		private String username;
		private Server server;
		private PrintWriter writer;
		private final String instructions = "\n>> To send a message to a user, enter @username message (ex: @testuser hi!).\n"
				+ ">> You can also enter any of the following commands: \n>> USERS (to see all users in the database)\n"
				+ ">> DELETE (to delete your account) \n>> UNREAD (to check for unread messages) \n>> QUIT (to quit the app) \n" 
				+ ">> HELP (to see the instructions again).\n";
		private final String loginString = ">> Please type LOGIN if you already have an account or CREATE to make a new one.\n"
				+ "You can also enter QUIT to quit the program.\n";
		private Protocol p;
		
		/** 
		 * The ClientThread constructor, which takes in a Socket and Server
		 * @param 	socket and server this thread connect to
		 */
		public ServerThread(Socket socket, Server server) {
			this.socket = socket;
			this.server = server;
			p = new Protocol();
		}
		
		/** 
		 * This method sets the ClientThread username based on the login or account creation info
		 * @param 	username
		 */
		public void setUsername(String username) {
			this.username = username.toLowerCase();
		}
		
		public String getUsername() {
			return this.username;
		}
		
		/**
		 * This method sends a message to the client when the user is online.
		 * @param message
		 */
		public void sendMessage(String sender, String message) {
			writer.println("\n>> " + sender + ": " + message + "\n");
		}
	
		
		/**
		 * This method runs the thread
		 * It is very long with a lot of logic to handle the different cases from the user
		 */
		public void run() {
			try {
	            //BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream())); // input from client
	            DataInputStream reader = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
				writer = new PrintWriter(socket.getOutputStream(), true);  // output to client
	            writer.println("\n>> Welcome to the chat app! \n");

	            
				/**
				 * First, force the user to login or create an account.
				 * This loop runs until a username is set. It gives the user the option to change from LOGIN to CREATE and vice versa
				 * In case they forget whether or not they have an account already.
				 */
				do {
		            writer.println(loginString);
					String account = p.decode(reader).trim(); // decode into a String
					
					/**
					 * This code got a bit messy and might be easier to read in a switch/case - sorry
					 * Essentially, it covers all of the logic for logging in and creating an account.
					 * It ensures that anyone logging in uses a real username from the database, 
					 * and prevents a new user from creating an account with a username that's already taken.
					 */
					if (account.equalsIgnoreCase("login")) {
						writer.println("\n>> Enter your username\n");
						String username = p.decode(reader);
						if (!server.checkUsername(username)) {
							writer.println("\n>> Sorry, that username is not in our system.\n");
			 			}
						else {
							writer.println("\n>> Welcome back " + username + "!\n");
							setUsername(username);
						}
					}
					else if (account.equalsIgnoreCase("create")) {
						writer.println("\n>> Enter your desired username");
						String username = p.decode(reader);
						if (server.checkUsername(username)) {
							writer.println("\n>> Sorry, that username is already taken. Please try another one.\n");
						}
						else {
							server.addUsername(username);
							writer.println("\n>> Welcome, " + username);
							setUsername(username);
						}
					}
					else if (account.equalsIgnoreCase("quit")) { // let the user quit if they want
						socket.close();
					}
				}
				while (username == null);
				
				server.addThread(this); // now that the thread has a username associated with it, add it to active threads

				if (server.checkForUnreadMessages(username)) { // check for any undelivered messages on login to let the user know
					writer.println("\n>> You have unread messages.\n");
				}
				
				writer.println(instructions); // then print instructions for actions after login
				String input;
				
				do { 
					input = p.decode(reader).trim();

					/**
					 * Easy cases to print out all users in the database and reprint instructions.
					 */
					if (input.equals("USERS")) {
						writer.println("\n>> " + server.getUsernames() + "\n");
					}
					else if (input.equals("HELP")) {
						writer.println(instructions);
					}
					
					/**
					 * If the user wants to delete their account, first check if they have unread messages.
					 * If they do, let them know and give option to not delete.
					 * Otherwise, delete
					 */
					else if (input.equals("DELETE")) {
						if (server.checkForUnreadMessages(username)) {
							writer.println("\n>> You have unread messages. Are you sure you want to delete your account?\n");
							String yesNo = p.decode(reader).trim();
							if ((yesNo.equalsIgnoreCase("no")) || (yesNo.equalsIgnoreCase("n"))) {
								writer.println("\n>> Account not deleted.\n");
								continue;
							};
						}
						
						server.removeUser(username);
						writer.println("\n>> Account for " + username + " deleted.\n");
						break;
					}
					
					/**
					 * If the first character is an '@' then it's a message to another user
					 * This code block first checks if the recipient doesn't exist and alerts the sender
					 * If the recipient does exist and is online, the message is sent immediately and user is told.
					 * If not, user is told recipient if offline.
					 */
					else if (input.length() > 0 && input.charAt(0) == '@' ) {
						String recipient = input.substring(1, input.indexOf(' ')); // username is from the @ to first space
						String message = input.substring(input.indexOf(' ') + 1); // message is from first space to end
						if (!server.checkUsername(recipient)) { // if the recipient does not exist
							writer.println("\n>> Sorry the user " + recipient + " does not exist.\n");
							continue;
						}
						boolean sent = server.sendMessage(username, recipient, message); // message includes sender info
						if (sent) {
							writer.println("\n>> Message sent to " + recipient + ".\n");
						}
						else {
							writer.println("\n>>" + recipient + " is currently offline. They will be notified of your message next time they login.\n");
						}
					}
					
					/**
					 * If the user wants to see unread messages, first check to see if there are any and alert if not
					 * If there are get the list and then print them out on separate lines
					 */
					else if (input.equals("UNREAD")) {
						ArrayList<Message> unreads = server.getUnreadMessages(username);
						if (unreads.size() < 1) {
							writer.println("\n>> You have no unread messages. \n");
							continue;
						}
						for (int i = 0; i < unreads.size(); i++) {
							Message m = unreads.get(i);
							sendMessage(m.getSender(), m.getMessage());
						}
					}
					/**
					 * Generic message for any unsupported prompt
					 */
					else {
						writer.println(">> Sorry I don't understand how to process that.\n");
					}
				}
				
				while (!input.equals("QUIT"));
				
				
			// this code executes when the user enters QUIT or DELETE
				writer.println(">> Thanks for using the chat app!\n");
				server.removeThread(this);
				socket.close();
				
				
			} catch (IOException e) {
				e.printStackTrace();
			} 
		}

	}