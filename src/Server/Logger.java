package Server;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {

	
	public static boolean writeLog(String typeOfMsg, String operation, String serverName, String additionalText, String managerId)
	{
		String filename = serverName+"-";
		String managerTxt = "";
		if(managerId!=null)
		{
			filename+=managerId;
			managerTxt = "Manager with ID "+managerId;
		} else {
			filename+="general";
			managerTxt = "USER";
		}
		filename+=".log";
		
		try {
			String path = System.getProperty("user.dir") + "/logs/";
			File file = new File(path+ filename);
			file.getParentFile().mkdirs();
			if (!file.exists()) {
				file.createNewFile();
			}
			BufferedWriter bw = new BufferedWriter(new FileWriter(file, true)); // append the result

			DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
			Date date = new Date();
			
			String logData = "["+dateFormat.format(date)+"] "+typeOfMsg+": Client executed \""+operation+"\" on server \""+serverName+"\". Invocation came from "+managerTxt+" with additional following information ("+additionalText+").";
			
			bw.write(logData);
			bw.newLine();
			bw.flush();
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		return true;
		
	}
}
