package UnitTests;

import static org.junit.Assert.*;

import org.junit.Test;

import Server.FlightServer;

public class UDPRequestTest {

	FlightServer flight = new FlightServer();
	
	@Test
	public void test() {
		
		assertEquals(0, flight.sendUDPRequest(1007,"MTL1111"));
		
		
	}

}
