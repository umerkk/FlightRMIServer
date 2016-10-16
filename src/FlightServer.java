

import java.lang.reflect.Array;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.text.Format.Field;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class FlightServer implements FlightServerInterface {
	
	final int REGISTRY_PORT = 4221;
	private ConcurrentHashMap<String,ArrayList<Flights>> flightList = new ConcurrentHashMap<String,ArrayList<Flights>>();
	private ConcurrentHashMap<String,ArrayList<Passenger>> passengerList = new ConcurrentHashMap<String,ArrayList<Passenger>>();
	private ConcurrentHashMap<Integer,String> recordToDateMapper = new ConcurrentHashMap<Integer,String>();
	public String serverLocation = "";
	private static int recordId=1;
	
	@Override
	public Boolean bookFlight(String firstName, String lastName, String address, String phoneNumber,
			String destination, String deptDate, String time, String flightType) throws RemoteException {

		boolean returnVal = false;
		String hashkey = lastName.substring(0, 1);
		if(!passengerList.containsKey(hashkey)) {
			passengerList.put(hashkey, new ArrayList<Passenger>());
		}
		Flights availableFlight = checkFlightAvailability(deptDate,time,flightType,destination);
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

	private Flights checkFlightAvailability(String deptDate, String deptTime, String flightType,String destination)
	{
		if(this.flightList.containsKey(deptDate)) {
			for(Flights f : flightList.get(deptDate)) {
				if(f.deptTime.equals(deptTime) && f.seats.get(flightType) != 0 && f.arrivalCity.equals(destination)) {
					return f;
				}
			}
		}
		return null;
	}
	
	
	
	@Override
	public Boolean addFlightRecord(String arrivalCity, String deptDate, String deptTime,int economySeats, int businessSeats, int firstClassSeats)
			throws RemoteException {
		
		Flights existingRecord=null;
		if(this.flightList.containsKey(deptDate)) {
			for(Flights f : flightList.get(deptDate)) {
				if(f.deptTime.equals(deptTime) && f.arrivalCity.equals(arrivalCity)) {
					existingRecord =  f;
				}
			}
		}
		
		if(existingRecord == null) {
		Flights myFlight = new Flights();
		myFlight.recordId = recordId++; myFlight.arrivalCity = arrivalCity; myFlight.deptDate = deptDate; myFlight.deptTime = deptTime;
		myFlight.seats.put("economy", economySeats);
		myFlight.seats.put("business", businessSeats);
		myFlight.seats.put("firstclass", firstClassSeats);
		
		if(this.flightList.get(deptDate) == null)
		{
			synchronized(this)
			{
				this.recordToDateMapper.put(myFlight.recordId, deptDate);
				this.flightList.put(deptDate, new ArrayList<Flights>());
			}
		}
		
		synchronized(this) {
		this.flightList.get(deptDate).add(myFlight);
		}
		Logger.writeLog("Success","addFlightRecord", serverLocation, myFlight.stringify(), null);
		return true;
			
		} else {
			
			Logger.writeLog("Error","addFlightRecord", serverLocation, "There already exists another flight with the same flight parameters ("+deptDate+' '+deptTime+' '+arrivalCity+")", null);
			return false;
		}
		
	}

	@Override
	public Boolean removeFlight(String deptDate, int recordId) throws RemoteException {
		// TODO Auto-generated method stub
		
		boolean isFound = false;
		if(this.flightList.containsKey(deptDate)) {
			ArrayList<Flights> list = flightList.get(deptDate);
			for(int k=0;k<list.size();k++)
			{
				if(list.get(k).recordId == recordId)
				{
					
					list.remove(k);
					isFound=true;
					Logger.writeLog("Success","removeFlight", serverLocation, "DeptDate:"+deptDate+", RecordID: "+recordId, null);

				}
			}
			return isFound;
		} else {
			Logger.writeLog("Error","removeFlight", serverLocation, "No Flight exists on the spcified date ("+deptDate+' '+recordId+")", null);

			return false;
		}
		
	}
	
	
	@Override
	public Array getBookedFlightCount(String recordType) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean editFlightRecord(int recordId, String fieldName, String newValue) throws RemoteException {
		// TODO Auto-generated method stub
		
		String deptDate = "";
		boolean returnVal = false;
		if(this.recordToDateMapper.containsKey(recordId)) {
			deptDate = recordToDateMapper.get(recordId);

			ArrayList<Flights> list = flightList.get(deptDate);
			for(int k=0;k<list.size();k++)
			{
				Flights temp = list.get(k);
				if(temp.recordId == recordId)
				{
					if(fieldName.equalsIgnoreCase("economy") || fieldName.equalsIgnoreCase("business") || fieldName.equalsIgnoreCase("firstclass"))
					{
						try {
							temp.seats.replace(fieldName.toLowerCase(), Integer.parseInt(newValue));
							Logger.writeLog("Success","editFlightRecord", serverLocation, "FieldName:"+fieldName+", FieldValue: "+newValue, null);
							returnVal = true;
						} catch (NumberFormatException nmb)
						{
							Logger.writeLog("Error","editFlightRecord", serverLocation, "Invalid value for the field name ("+fieldName+") is provided. Server is expecting a number for seat value ", null);
							//return false;
							
						}
						
					} else {
						try {

							java.lang.reflect.Field field = temp.getClass().getField(fieldName);
							field.set(temp, newValue);
							Logger.writeLog("Success","editFlightRecord", serverLocation, "FieldName:"+fieldName+", FieldValue: "+newValue, null);
							returnVal = true;
						} catch (NoSuchFieldException e)
						{
							Logger.writeLog("Error","editFlightRecord", serverLocation, "Invalid field id ("+fieldName+")", null);
							returnVal =  false;
						} catch (IllegalArgumentException e) {
							Logger.writeLog("Error","editFlightRecord", serverLocation, "IllegalArgumentException occured in ("+fieldName+")", null);
							returnVal = false;
							
						} catch (IllegalAccessException e) {
							Logger.writeLog("Error","editFlightRecord", serverLocation, "IllegaAccessException occured in field id ("+fieldName+")", null);
							returnVal = false;
						}
					}


				}
			}


		} else {
			Logger.writeLog("Error","editFlightRecord", serverLocation, "No record exists with the specified recordId ("+recordId+")", null);
			returnVal = false;
		}


		return returnVal;
	}
	
	public void initServer(String serverName, int port) throws Exception
	{
		Registry myRegistry = null;
		
		try {
			myRegistry  = LocateRegistry.createRegistry(REGISTRY_PORT);
		} catch (java.rmi.server.ExportException ne) {
			// If we are running in tango server mode, there may be a registry already existing.
			myRegistry  = LocateRegistry.getRegistry(REGISTRY_PORT);
		}
		
		
		Remote serverObject = UnicastRemoteObject.exportObject(this, port);
		myRegistry.bind(serverName, serverObject);
		this.serverLocation = serverName;
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
