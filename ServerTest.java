import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ServerTest {
	private Server server = new Server(5000);

	@Test 
	// make sure a non-used username can be created
	public void testAddUser() {
		server.addUsername("test");
		assertEquals(true, server.checkUsername("test"));
	}
	
	@Test 
	// make sure an existing username can be removed
	public void testRemoveUser() {
		server.removeUser("test");
		assertEquals(false, server.checkUsername("test"));
	}
	
	

	
	

}
