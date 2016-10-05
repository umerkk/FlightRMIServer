

import java.lang.reflect.Array;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface FlightServerInterface extends Remote {
	
	public Boolean bookFlight(String firstName, String lastName, String address, int phoneNumber, String destination, String deptDate, String time, String flightType) throws RemoteException;
	public Array getBookedFlightCount(String recordType) throws RemoteException;
	public Boolean editFlightRecord(int recordId, String fieldName, String newValue) throws RemoteException;

}
