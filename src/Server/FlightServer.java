package Server;


import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FlightServer implements FlightServerInterface {

	final int REGISTRY_PORT = 4221;
	private static ConcurrentHashMap<String,ArrayList<Flights>> flightList = new ConcurrentHashMap<String,ArrayList<Flights>>();
	private static ConcurrentHashMap<String,ArrayList<Passenger>> passengerList = new ConcurrentHashMap<String,ArrayList<Passenger>>();
	private static ConcurrentHashMap<Integer,String> recordToDateMapper = new ConcurrentHashMap<Integer,String>();
	public String serverLocation = "";
	private static int recordId=1;
	HashMap<String,Integer> udpPorts = new HashMap<String,Integer>();


	@Override
	public String[] bookFlight(String firstName, String lastName, String address, String phoneNumber,
			String destination, String deptDate, String time, String flightType) throws RemoteException {
		
		String[] response = new String[2];
		

		boolean returnVal = false;
		String hashkey = lastName.substring(0, 1);
		if(!passengerList.containsKey(hashkey)) {
			passengerList.put(hashkey, new ArrayList<Passenger>());
		}
		Flights availableFlight = checkFlightAvailability(deptDate,time,flightType,destination.toLowerCase());
		if(availableFlight != null) {
			Passenger tempPassenger = new Passenger();
			tempPassenger.firstName = firstName; tempPassenger.lastName = lastName; tempPassenger.address = address; tempPassenger.phoneNumber = phoneNumber;
			tempPassenger.destination = destination; tempPassenger.deptDate = deptDate;	tempPassenger.deptTime = time; tempPassenger.flightClass = flightType;
			try {
				synchronized(this) {
					int currentSeats = availableFlight.seats.get(flightType.toLowerCase());
					if(availableFlight.seats.replace(flightType.toLowerCase(), --currentSeats) != null) {
						passengerList.get(hashkey).add(tempPassenger);
						response[0] = "Success";
						response[1] = "Your flight has been successfully booked.";
						//returnVal =  true;
						Logger.writeLog("Success","bookFlight", serverLocation, tempPassenger.stringify(), null);
					} else {
						//returnVal =  false;
						response[0] = "Error";
						response[1] = "Problem while deducting available seats from the FlightList for the Boooking record ("+tempPassenger.stringify()+")";
						Logger.writeLog("Error","bookFlight", serverLocation, "Problem while deducting available seat count from FlightList. Extra info: "+tempPassenger.stringify(), null);
					}
				}
			} catch (Exception e)
			{
				//Log the error
				response[0] = "Error";
				response[1] = "Problem while deducting available seats from the FlightList for the Boooking record ("+tempPassenger.stringify()+")";
				Logger.writeLog("Error","bookFlight", serverLocation, "Problem while deducting available seat count and adding passenger to booking list. Extra info: "+e.getMessage()+". Object info: "+tempPassenger.stringify(), null);
			}
		} else {
			//Flight is not available.
			//returnVal =  false;
			response[0] = "Error";
			response[1] = "There is no flight flying at the desired dates OR there are not enough vacant seats for this reservation. ";
			
			Logger.writeLog("Error","bookFlight", serverLocation, "No available seat in the requested flight ("+deptDate+' '+time+' '+flightType+")", null);
		}



		return response;

		//return true;
	}

	private Flights checkFlightAvailability(String deptDate, String deptTime, String flightType,String destination)
	{
		if(flightList.containsKey(deptDate)) {
			for(Flights f : flightList.get(deptDate)) {
				if(f.deptTime.equalsIgnoreCase(deptTime) && f.seats.get(flightType.toLowerCase()) > 0 && f.arrivalCity.equalsIgnoreCase(destination)) {
					return f;
				}
			}
		}
		return null;
	}



	@Override
	public String[] addFlightRecord(String arrivalCity, String deptDate, String deptTime,int economySeats, int businessSeats, int firstClassSeats)
			throws RemoteException {

		String[] response = new String[2];
		String managerId=arrivalCity.substring(arrivalCity.indexOf("|")+1,arrivalCity.length());
		arrivalCity = arrivalCity.substring(0,arrivalCity.indexOf("|"));
		
		Flights existingRecord=null;
		if(flightList.containsKey(deptDate)) {
			for(Flights f : flightList.get(deptDate)) {
				if(f.deptTime.equalsIgnoreCase(deptTime) && f.arrivalCity.equalsIgnoreCase(arrivalCity)) {
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

			if(flightList.get(deptDate) == null)
			{
				synchronized(this)
				{
					recordToDateMapper.put(myFlight.recordId, deptDate);
					flightList.put(deptDate, new ArrayList<Flights>());
				}
			}

			synchronized(this) {
				flightList.get(deptDate).add(myFlight);
			}
			Logger.writeLog("Success","addFlightRecord", serverLocation, myFlight.stringify(), managerId);
			response[0] = "Success";
			response[1] = "Flight has been successfully added into our records.";
			

		} else {

			Logger.writeLog("Error","addFlightRecord", serverLocation, "There already exists another flight with the same flight parameters ("+deptDate+' '+deptTime+' '+arrivalCity+")", managerId);
			response[0] = "Error";
			response[1] = "There exists anothjer flight with the same flight parameters.";
			
		}
		
		return response;

	}

	@Override
	public String[] removeFlight(String deptDate, int recordId) throws RemoteException {
		// TODO Auto-generated method stub

		String[] response = new String[2];
		String managerId=deptDate.substring(deptDate.indexOf("|")+1,deptDate.length());
		deptDate = deptDate.substring(0,deptDate.indexOf("|"));
		
		
		boolean isFound = false;
		if(flightList.containsKey(deptDate)) {
			ArrayList<Flights> list = flightList.get(deptDate);
			for(int k=0;k<list.size();k++)
			{
				if(list.get(k).recordId == recordId)
				{

					list.remove(k);
					isFound=true;
					if(list.size() < 1)
						flightList.remove(deptDate);
					Logger.writeLog("Success","removeFlight", serverLocation, "DeptDate:"+deptDate+", RecordID: "+recordId, managerId);

				}
			}
			if(isFound)
			{
				response[0] = "Success";
				response[1] = "Your flight has been successfully removed.";
				
			} else {
				response[0] = "Error";
				response[1] = "No flight was found with the specified RECORD ID";
				
			}
		} else {
			Logger.writeLog("Error","removeFlight", serverLocation, "No Flight exists on the spcified date ("+deptDate+' '+recordId+")", managerId);

			response[0] = "Error";
			response[1] = "No flight on the specified date ("+deptDate+", "+recordId+")";
		}
		return response;

	}


	@Override
	public int getBookedFlightCount(String managerId) throws RemoteException {
		// TODO Auto-generated method stub

		int totalBooking=0;
		totalBooking+=this.getActualBookedFlightCount();

		for (Map.Entry<String, Integer> entry : udpPorts.entrySet()) {
			if(!entry.getKey().equalsIgnoreCase(serverLocation)){

				totalBooking+=this.sendUDPRequest(entry.getValue(),managerId);

			}
		}
		return totalBooking;

	}

	private int getActualBookedFlightCount()
	{
		int totalBooking=0;

		for (Map.Entry<String, ArrayList<Passenger>> entry : passengerList.entrySet()) {
			totalBooking += entry.getValue().size();
		}

		return totalBooking;

	}

	@Override
	public String[] editFlightRecord(int recordId, String fieldName, String newValue) throws RemoteException {
		// TODO Auto-generated method stub

		String[] response = new String[2];
		String managerId=newValue.substring(newValue.indexOf("|")+1,newValue.length());
		newValue = newValue.substring(0,newValue.indexOf("|"));
		
		
		String deptDate = "";
		boolean returnVal = false;
		if(recordToDateMapper.containsKey(recordId)) {
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
							Logger.writeLog("Success","editFlightRecord", serverLocation, "FieldName:"+fieldName+", FieldValue: "+newValue, managerId);
							response[0] = "Success";
							response[1] = "Flight record is successfully modified (FieldName:"+fieldName+", FieldValue: "+newValue+")";
						} catch (NumberFormatException nmb)
						{
							Logger.writeLog("Error","editFlightRecord", serverLocation, "Invalid value for the field name ("+fieldName+") is provided. Server is expecting a number for seat value ", managerId);
							//return false;
							response[0] = "Error";
							response[1] = "Invalid value for the field name ("+fieldName+") is provided. Server is expecting a number for seat value ";

						}

					} else {
						try {

							java.lang.reflect.Field field = temp.getClass().getField(fieldName);
							field.set(temp, newValue);
							Logger.writeLog("Success","editFlightRecord", serverLocation, "FieldName:"+fieldName+", FieldValue: "+newValue, managerId);
							response[0] = "Success";
							response[1] = "Flight record is successfully modified (FieldName:"+fieldName+", FieldValue: "+newValue+")";
						} catch (NoSuchFieldException e)
						{
							Logger.writeLog("Error","editFlightRecord", serverLocation, "Invalid field id ("+fieldName+")", managerId);
							response[0] = "Error";
							response[1] = "Invalid field id ("+fieldName+")";

						} catch (IllegalArgumentException e) {
							Logger.writeLog("Error","editFlightRecord", serverLocation, "IllegalArgumentException occured in ("+fieldName+")", managerId);
							response[0] = "Error";
							response[1] = "IllegalArgumentException occured in ("+fieldName+")";

						} catch (IllegalAccessException e) {
							Logger.writeLog("Error","editFlightRecord", serverLocation, "IllegaAccessException occured in field id ("+fieldName+")", managerId);
							response[0] = "Error";
							response[1] = "IllegaAccessException occured in field id ("+fieldName+")";
						}
					}


				}
			}


		} else {
			Logger.writeLog("Error","editFlightRecord", serverLocation, "No record exists with the specified recordId ("+recordId+")", managerId);
			response[0] = "Error";
			response[1] = "There is no such record exists for the specified Record ID ("+recordId+")";
		}


		return response;
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

	private void setUDPPorts()
	{
		this.udpPorts.put("WSL", 10007);
		this.udpPorts.put("MTL", 10008);
		this.udpPorts.put("NDH", 10009);
	}

	private int getUDPPorts(String serverName)
	{
		if(udpPorts.containsKey(serverName))
			return udpPorts.get(serverName);
		else
			return -1;
	}

	public int sendUDPRequest(int serverPort, String managerId)
	{
		byte[] sendData = new byte[1024];
		byte[] receiveData = new byte[1024];
		//sendData = "getBookedFlightCount".getBytes();

		try {
			DatagramSocket clientSocket = new DatagramSocket();
			//clientSocket.setSoTimeout(5000);
			InetAddress IPAddress = InetAddress.getByName("localhost");

			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, serverPort);
			clientSocket.send(sendPacket);

			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			clientSocket.receive(receivePacket);

			int returnCount = ByteBuffer.wrap(receivePacket.getData()).getInt();
			Logger.writeLog("Success","getFlightCount", serverLocation, "UDP Request of GetFlightCount returned ("+returnCount+") at port ("+serverPort+")", managerId);
			clientSocket.close();

			return returnCount;

		} catch (Exception e)
		{
			Logger.writeLog("Error","getFlightCount", serverLocation, "UDP Request of GetFlightCount returned ERROR: ("+e.getMessage()+") at port ("+serverPort+")", managerId);
			return 0;
		}
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

			FlightServer myServer = new FlightServer();
			myServer.initServer(serverLocation, port);
			System.out.println("RMI: '"+serverLocation+"' server is up and running on port "+port);

			myServer.setUDPPorts();
			int myUdpPort = myServer.getUDPPorts(serverLocation);
			if(myUdpPort != -1)
			{
				DatagramSocket udpSocket = new DatagramSocket(myUdpPort);
				System.out.println("UDP: '"+serverLocation+"' server is up and running on port "+myUdpPort);

				byte[] receiveData = new byte[1024];
				byte[] sendData = new byte[1024];
				while(true)
				{
					DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
					udpSocket.receive(receivePacket);

					InetAddress senderIp = receivePacket.getAddress();
					int senderPort = receivePacket.getPort();


					sendData = toBytes(myServer.getActualBookedFlightCount());

					//Logger.writeLog("Debugging","UDpRequest", serverLocation, "The Return value is: ("+myServer.getActualBookedFlightCount()+")", "UDPRequests");

					DatagramPacket sendPacket =new DatagramPacket(sendData, sendData.length, senderIp, senderPort);
					udpSocket.send(sendPacket);

				}
			}
		} catch (Exception e)
		{
			System.out.println(e.getMessage());
		}
	}

	public static byte[] toBytes(int i)
	{
		byte[] result = new byte[4];

		result[0] = (byte) (i >> 24);
		result[1] = (byte) (i >> 16);
		result[2] = (byte) (i >> 8);
		result[3] = (byte) (i /*>> 0*/);

		return result;
	}


}
