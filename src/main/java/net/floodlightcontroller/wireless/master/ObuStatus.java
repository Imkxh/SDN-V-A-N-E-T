package net.floodlightcontroller.wireless.master;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.projectfloodlight.openflow.types.MacAddress;

import com.sun.javafx.collections.MappingChange.Map;

public class ObuStatus {

	private MacAddress macAddress;
	private String time;
	private String longitude;//经度
	private String latitude;//纬度
	private String altitude;
	private String speed;//速度
	private String heading;
	private String climb;
	private String status;
	private String rssilist;
	private ConcurrentHashMap<MacAddress,String> rssiMap = new ConcurrentHashMap<MacAddress,String>();

	
	public ObuStatus() {
		
	}

	public ObuStatus(MacAddress macAddress,String time,String longitude,
			String latitude,String altitude,String speed,String heading,String climb,String status,String rssi) {
		super();
		this.macAddress = macAddress;
		this.time = time;
		this.longitude = longitude;
		this.latitude = latitude;
		this.altitude = altitude;
		this.speed = speed;
		this.heading = heading;
		this.climb =climb;
		this.status = status;
		//this.rssilist = rssi;
	}
	
	public void setAllStatus(MacAddress macAddress,String time,String longitude,
			String latitude,String altitude,String speed,String heading,String climb,String status,String obumac2 ,String rssi) {
		
		this.macAddress = macAddress;
		this.time = time;
		this.longitude = longitude;
		this.latitude = latitude;
		this.altitude = altitude;
		this.speed = speed;
		this.heading = heading;
		this.climb =climb;
		this.status = status;
		//this.rssilist = rssi;
		
		//ConcurrentHashMap<MacAddress, String> newMap = new ConcurrentHashMap<MacAddress,String>();
		
		//System.out.println("M---------------->");
		
		//System.out.println("M---------------->"+obumac2);
		String obuBlack = "00:00:00:00:00:00";
		if(obumac2 != obuBlack) {
			rssiMap.put(MacAddress.of(obumac2), rssi);
			rssiMap.remove(MacAddress.of(obuBlack));
		}
		//System.out.println("M--->"+rssiMap.get(MacAddress.of(obumac2)));
		//System.out.println("---------------->");
		//newMap.put(MacAddress.of(obumac2), rssi);
	}
	
	/*public void addRssiMap(String mac,String rssi) {
		rssiMap.put(MacAddress.of(mac), rssi);
	}*/
	
	public void removeRssiMap(MacAddress macAddress) {
		rssiMap.remove(macAddress);
	}
	//macAddress
	public MacAddress getObuMacAddress() {
		return macAddress;
	}
	public void setObuMacAddress(MacAddress macAddress) {
		this.macAddress = macAddress;
	}

	//time
	public String getObuTime() {
		return time;
	}
	public void setObuTime(String time) {
		this.time = time;
	}	
	
	//longitude
	public String getLongitude() {
		return longitude;
	}
	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}

	//latitude
	public String getLatitude() {
		return latitude;
	}
	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}

	//altitude
	public String getAltitude() {
		return altitude;
	}
	public void setAltitude(String altitude) {
		this.altitude = altitude;
	}
	
	//speed
	public String getSpeed() {
		return speed;
	}
	public void setSpeed(String speed) {
		this.speed = speed;
	}
	
	//heading
	public String getHeading() {
		return heading;
	}
	public void setHeading(String heading) {
		this.heading = heading;
	}

	//climb
	public String getClimb() {
		return climb;
	}
	public void setClimb(String climb) {
		this.climb = climb;
	}

	//status
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	
/*	//rssilist
	public String getRssiList() {
		return rssilist;
	}
	public void setRssiList(String list) {
		this.rssilist = list;
	}*/
	
	//rssiMap
	public ConcurrentHashMap<MacAddress,String> getRssiMap() {
		return rssiMap;
	}
	public void setRssiMap(ConcurrentHashMap<MacAddress,String> map) {
		this.rssiMap = map;
	}
	
	//rssilist
	
	public String getRssiList() {
		
		String rssilist = "";
		
		//List<String> rssiList = new ArrayList<>();
		
		for(MacAddress obuMac : rssiMap.keySet()) {
			
			System.out.println("M--->"+obuMac.toString());
		}
		
		
		
		for (MacAddress obuMac : getRssiMap().keySet()) {
			
			String obumac = obuMac.toString();
			String rssi = getRssiMap().get(obuMac);
			String arssiLists = obumac+" "+rssi;
			
			rssilist = rssilist+arssiLists+" ";
			//rssiList.add(arssiLists);
		}
		return rssilist;
		
	}

/*	@Override
	public String toString() {
		return "ObuStatus [macAddress=" + macAddress + ", longitude=" + longitude + ", latitude="
				+ latitude + ", speed=" + speed + ", rssilist=" + rssilist + "]";
	}*/
	
	
}
