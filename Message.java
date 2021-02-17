/**
 * This class stores the state and functionality for a message sent between users.
 * The main purpose for this class is the getter methods, so that it's easy to see if a user has unread messages.
 */
public class Message {
	/**
	 * The fields of a message. They include Strings for the sender, recipient, and message.
	 */
	private String sender;
	private String recipient;
	private String message;
	private final String delimiter = "-|::|-";
	
	/*
	 * Constructor to take in the parameters and set the fields appropriately
	 */
	public Message(String sender, String recipient, String message) {
		this.sender = sender;
		this.recipient = recipient;
		this.message = message;
	}
	
	public String getSender() {
		return sender;
	}
	
	public String getRecipient() {
		return recipient;
	}
	
	public String getMessage() {
		return message;
	}
	
	public String toString() {
		return sender + delimiter + recipient + delimiter + message;
	}
}
