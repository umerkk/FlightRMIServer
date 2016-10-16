package UnitTests;

import static org.junit.Assert.*;

import java.rmi.RemoteException;

import org.junit.Test;

import Server.FlightServer;

public class BookFlightTest {

	FlightServer flight = new FlightServer();

	public void setUp() throws Exception {
		flight.addFlightRecord("Karachi", "2016-12-25", "21:05", 5,4,3);
	}

	@Test
	public void test() throws RemoteException {
		String[] response = new String[2];
		response[0] = "Success";
		response[1] = "Your flight has been successfully booked.";
		
		assertArrayEquals(response,flight.bookFlight("sdf", "sdfsd", "Sdfdsf", "32423", "Karachi", "2016-12-25", "21:05","economy"));
		
	}
}
