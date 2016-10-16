package UnitTests;

import static org.junit.Assert.*;
import java.rmi.RemoteException;
import org.junit.Test;
import Server.FlightServer;

public class AddFlightTest {
	FlightServer flight = new FlightServer();

	@Test
	public void test() throws RemoteException {
		assertTrue("sdf",flight.addFlightRecord("Karachi", "2016-12-25", "21:05", 5,4,3));
	}

}
