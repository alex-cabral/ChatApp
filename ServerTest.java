import static org.junit.Assert.*;

import org.junit.Test;

public class ServerTest {
	private Server server = new Server(5000);
	
	@Test 
	// make sure a non-used username can be created
	public void testAddUsername() {
		server.addUsername("test");
		assertEquals(true, server.checkUsername("test"));
	}
	
	
	@Test
	// make sure a message can't get sent to a user who doesn't exist
	public void testSendMessage() {
		assertEquals(false, server.sendMessage("test", "notauser", ""));
	}

	@Test
	// make sure a user without unread messages isn't flagged as having them,
	// but that a user with unread messages is
	public void testCheckForUnreadMessages() {
		assertEquals(false, server.checkForUnreadMessages("test"));
		assertEquals(true, server.checkForUnreadMessages("alice"));
	}
	

	@Test
	// make sure someone with unread messages gets a non null object back
	public void testGetUnreadMessages() {
		assertNotNull(server.getUnreadMessages("alice"));
	}

	@Test
	// make sure removing a user removes them from the list
	public void testRemoveUser() {
		server.removeUser("test");
		assertEquals(false, server.checkUsername("test"));
	}

}
