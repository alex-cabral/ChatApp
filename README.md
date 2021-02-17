# ChatApp
A Basic Multithreaded Java Chat Application

README

This file contains the instructions to run our application and the design notes of our wire protocol.

All .java and .class files are stored in this folder. You can also find Notes.pdf, which includes the following text and additional notes about our design and development process. 


How to run our chat server

1.	First open up the server in the terminal using the following command:

javac Server.java && java Server <port>

We did the testing on port numbers between 4000 and 9000 so feel free to use one of those.

2.	Next, open a client using the following command:

javac Client.java && java Client <host> <port>
	
The host can be your local device ip address which you can find by typing ifconfig in the terminal and using the value shown in inet at lo0.  

3.	You can open multiple clients in various terminal windows to test communication between the clients.

We already have four users created in the system – Alice, Sadie, Robbie, and Charlie. Alice, Sadie, and Charlie all have unread messages. You can use those for testing if you would like. 

User Actions Supported by Our Server

Whenever a client connection is opened, the server sends the following message after a welcome:

`Please type LOGIN if you already have an account or CREATE to make a new one. You can also enter QUIT to quit the program"`

1.	When a user writes LOGIN, they are prompted to enter a username. An error message is displayed if the username is not recognized by the server. A welcome message is displayed on successful login.
2.	When a user writes CREATE, they are prompted to enter a unique username. An error message is displayed if the username already exists. A welcome message is displayed on successful creation of the account. Note the user is logged in now and is active.
3.	When a user writes QUIT, the socket closes and the user can no longer interact with the client server chat system. Note, there is a small bug here due to a threading issue that we unfortunately could not find a solution for in the given timeframe. The functionality still works, but the server sends one additional message that is output to the client.

Once the user is logged in, our server supports further actions from the user. These are listed below:

4.	USERS: Typing USERS prints the list of all usernames stored in the server backend.
5.	DELETE: Typing deletes the account of the current user and closes the socket. However, if the user has unread messages, the system alerts them and gives an option to not delete the account. In this case they would then have to still use the command to view their unread messages. Note there is a tiny bug here similar to that of QUIT, where there are issues with the threading, so an exception is printed. However, the backend functionality still works as expected.
6.	HELP: Typing HELP provides users the list of all instructions that they can perform.
7.	UNREAD: Typing UNREAD checks to see if the user has unread messages and either alerts them that they do not or prints all of the unread messages on separate lines. The unread messages are then deleted from the “database” (i.e. text file storing them).
8.	@username <message>: Finally, our chat application allows users to send a message to a specific username using this syntax. If the recipient user is active, the message is delivered to the recipient. If the recipient user is not active, then the message is added to the list of unread messages for the recipient user on the server backend and they user is alerted that they have unread messages whenever they next log in. 


Wire Protocol

We thought a lot about the different options here – passing strings, passing objects, creating our own bastardized version of JSON, etc. Jim suggested that because we used Java, it would be more difficult to write a protocol because Java uses types. However, he also told us that using just strings was admitting defeat, so we settled on an alternate method. 

Our wire protocol is a simple byte-based protocol. The protocol allots one byte to store the data type (in our case always strings), 4 bytes to store an integer of the data length, and N bytes to store the data value. Strings are encoded based on the protocol in the Client class then sent over DataInput and DataOutput streams as bytes, then decoded based on the protocol by the ServerThread assigned to the client. The Protocol class includes the code for the encoding and decoding. All strings sent across the wire use the protocol, including every command sent by the user and every response sent from the server.



