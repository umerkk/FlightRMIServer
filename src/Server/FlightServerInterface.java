package Server;


import java.lang.reflect.Array;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface FlightServerInterface extends Remote {
	
	public String[] bookFlight(String firstName, String lastName, String address, String phoneNumber, String destination, String deptDate, String time, String flightType) throws RemoteException;
	public int getBookedFlightCount(String managerId) throws RemoteException;
	public String[] editFlightRecord(int recordId, String fieldName, String newValue) throws RemoteException;
	public String[] addFlightRecord (String arrivalCity, String deptDate, String deptTime, int economySeats, int businessSeats, int firstClassSeats) throws RemoteException;
	public String[] removeFlight (String deptDate, int recordId) throws RemoteException;
	
	
}
