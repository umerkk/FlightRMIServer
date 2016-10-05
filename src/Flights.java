

import java.util.HashMap;

public class Flights {

	public String deptCity;
	public String arrivalCity;
	public String deptDate;
	public String deptTime;
	public HashMap<String,Integer> seats = new HashMap<String,Integer>();
	
	public void Flights() {
		seats.put("Economy", 0);
		seats.put("Business", 0);
		seats.put("FirstClass", 0);
	}
}
