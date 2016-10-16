

import java.util.HashMap;

public class Flights {

	public int recordId;
	public String deptCity;
	public String arrivalCity;
	public String deptDate;
	public String deptTime;
	public HashMap<String,Integer> seats = new HashMap<String,Integer>();
	
	public void Flights() {
		seats.put("economy", 0);
		seats.put("business", 0);
		seats.put("firstclass", 0);
	}
	
	public String stringify()
	{
		return "{RecordId= "+recordId+", DeptCity= "+deptCity+", arrivalCity= "+arrivalCity+", deptDate= "+deptDate+", deptTime= "+deptTime+", Economy= "+seats.get("economy")+", Business= "+seats.get("business")+", FirstClass= "+seats.get("firstclass")+"}";
		
	}
}
