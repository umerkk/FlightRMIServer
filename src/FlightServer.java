

import java.lang.reflect.Array;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class FlightServer implements FlightServerInterface {
	

	private ConcurrentHashMap<String,ArrayList<Flights>> flightList = new ConcurrentHashMap<String,ArrayList<Flights>>();
	private ConcurrentHashMap<String,ArrayList<Passenger>> passengerList = new ConcurrentHashMap<String,ArrayList<Passenger>>();
	public String serverLocation = "";
	
	@Override
	public Boolean bookFlight(String firstName, String lastName, String address, int phoneNumber,
			String destination, String deptDate, String time, String flightType) throws RemoteException {

		boolean returnVal = false;
		String hashkey = lastName.substring(0, 1);
		if(!passengerList.containsKey(hashkey)) {
			passengerList.put(hashkey, new ArrayList<Passenger>());
		}
		Flights availableFlight = checkFlightAvailability(deptDate,time,flightType);
		if(availableFlight != null) {
			Passenger tempPassenger = new Passenger();
			tempPassenger.firstName = firstName; tempPassenger.lastName = lastName; tempPassenger.address = address; tempPassenger.phoneNumber = phoneNumber;
			tempPassenger.destination = destination; tempPassenger.deptDate = deptDate;	tempPassenger.deptTime = time; tempPassenger.flightClass = flightType;
			try {
				if(availableFlight.seats.replace(flightType, ((int) availableFlight.seats.get(flightType))-1) != null) {
					passengerList.get(hashkey).add(tempPassenger);
					returnVal =  true;
					Logger.writeLog("Success","bookFlight", serverLocation, tempPassenger.stringify(), null);
				} else {
					returnVal =  false;
					Logger.writeLog("Error","bookFlight", serverLocation, "Problem while deducting available seat count from FlightList. Extra info: "+tempPassenger.stringify(), null);
				}
			} catch (Exception e)
			{
				//Log the error
				Logger.writeLog("Error","bookFlight", serverLocation, "Problem while deducting available seat count and adding passenger to booking list. Extra info: "+e.getMessage()+". Object info: "+tempPassenger.stringify(), null);
			}
		} else {
			//Flight is not available.
			returnVal =  false;
			Logger.writeLog("Error","bookFlight", serverLocation, "No available seat in the requested flight ("+deptDate+' '+time+' '+flightType+")", null);
		}



		return returnVal;
		
		//return true;
	}

	private Flights checkFlightAvailability(String deptDate, String deptTime, String flightType)
	{
		if(this.flightList.containsKey(deptDate)) {
			for(Flights f : flightList.get(deptDate)) {
				if(f.deptTime.equals(deptTime) && f.seats.get(flightType) != 0) {
					return f;
				}
			}
		}
		return null;
	}
	
	
	
	
	
	@Override
	public Array getBookedFlightCount(String recordType) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean editFlightRecord(int recordId, String fieldName, String newValue) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void initServer(String serverName, int port) throws Exception
	{
		Registry myRegistry = null;
		
		try {
			myRegistry  = LocateRegistry.createRegistry(4221);
		} catch (java.rmi.server.ExportException ne) {
			// If we are running in tango server mode, there may be a registry already existing.
			myRegistry  = LocateRegistry.getRegistry(4221);
		}
		
		
		Remote serverObject = UnicastRemoteObject.exportObject(this, port);
		myRegistry.bind(serverName, serverObject);
	}
	
	public static void main(String[] args) {
		String serverLocation = "_unknown";
		int port = 15033;

		try{
			if (System.getSecurityManager() == null)
	            System.setSecurityManager ( new SecurityManager() );
			if(!args[0].equals(""))
				serverLocation = args[0];
			
			if(args[1] != null)
				port = Integer.parseInt(args[1]);
			
			new FlightServer().initServer(serverLocation, port);
			System.out.println("'"+serverLocation+"' server is up and running on port "+port);
			
		} catch (Exception e)
		{
			System.out.println(e.getMessage());
		}
	}
	
	

}
