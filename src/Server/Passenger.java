package Server;


public class Passenger {

	public String firstName;
	public String lastName;
	public String address;
	public String phoneNumber;
	public String destination;
	public String flightClass;
	public String deptDate;	
	public String deptTime;

	public void Passener(){}
	
	public String stringify()
	{
		return "{FirstName= "+firstName+", LastName= "+lastName+", Address= "+address+", PhoneNumber= "+phoneNumber+", Destination= "+destination+", FlightClass= "+flightClass+", DeptDate= "+deptDate+", DeptTime= "+deptTime+"}";
		
	}
}
