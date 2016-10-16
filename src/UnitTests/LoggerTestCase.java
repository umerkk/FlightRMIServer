package UnitTests;

import static org.junit.Assert.*;

import org.junit.Test;

import Server.Logger;

public class LoggerTestCase {


	
	@Test
	public void test() {
		String arrivalCity = "karachi|MTL2222";
		String managerId=arrivalCity.substring(arrivalCity.indexOf("|")+1,arrivalCity.length());
		arrivalCity = arrivalCity.substring(0,arrivalCity.indexOf("|"));
		
		assertTrue(Logger.writeLog("Error","addFlightRecord", "MTL", "There already exists another flight with the same flight parameters ("+' '+arrivalCity+")", managerId));
	}

}
