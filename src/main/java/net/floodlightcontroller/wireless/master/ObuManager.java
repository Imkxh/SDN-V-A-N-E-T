package net.floodlightcontroller.wireless.master;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

import org.projectfloodlight.openflow.types.MacAddress;

public class ObuManager {
	
	private static final double EARTH_RADIUS = 6378137;//地球半径,单位m.
	private static final int POWER_VALUE = 23;//控制功率值
	private static final int OBU_NUM = 100;//车辆最大密度值
	
	
	private final Map<MacAddress, CarObu> obuMap = 
			new ConcurrentHashMap<> ();
	
	protected void addObu (final MacAddress obuMacAddress) {
		obuMap.put(obuMacAddress, new CarObu (obuMacAddress));
	}
	
	protected void removeObu(final MacAddress obuMacAddress) {
		obuMap.remove(obuMacAddress);
	}
	
	protected CarObu getObu (final MacAddress obuHwAddress) {
		return obuMap.get(obuHwAddress);
	}
	
	protected Set<CarObu> getObusFromMap() {
		
		Set<CarObu> obus = new HashSet<CarObu>();
		
		/*for (MacAddress mac : ObuMap.keySet()) {
			System.out.println(mac.toString());
			
		}
		System.out.println("vvvvvvvv");
		for (CarObu value : ObuMap.values()) { 
			obus.add(value);
		}*/

		for (MacAddress mac : obuMap.keySet()) {
			//System.out.println(mac.toString());
			CarObu co = obuMap.get(mac);
			obus.add(co);
		}
		
		return obus;
		
	}
	
	protected boolean inObuMap (MacAddress obuMac) {
		return obuMap.containsKey(obuMac);
	}
	
	//*************************Obu功率控制******************************//
	public List<String> handleObuPowerControl() {
		
		List<String> powerList = new ArrayList<String>();
		String power = null;
		int size = obuMap.size();
		if(size < OBU_NUM)
		{
			for(MacAddress mac : obuMap.keySet()) {
				power = mac.toString() + " " + POWER_VALUE;
				powerList.add(power);
			}
		
			return powerList;
		}
		else
			return null;
	
		
	}
	//************************计算两车距离******************************//
	public double countCarDistance (MacAddress mac1,MacAddress mac2) {
		
		double distance = 0;
		double lng1 = 0;//经度1
		double lat1 = 0;//纬度1
		double lng2 = 0;//经度2
		double lat2 = 0;//纬度2
		CarObu car1 = getObu(mac1);
	
		if(getObu(mac2) == null) {
			return 0;
		}
		else {
		CarObu car2 = getObu(mac2);
		ObuStatus status1 = car1.getStatus();
		ObuStatus status2 = car2.getStatus();
		
		lng1 = Double.parseDouble(status1.getLongitude());
		lat1 = Double.parseDouble(status1.getLatitude());
		
		lng2 = Double.parseDouble(status2.getLongitude());
		lat2 = Double.parseDouble(status2.getLatitude());
		
		distance = GetDistance(lng1,lat1,lng2,lat2);
		
		return distance;
		}
	}
	
    /**
     * 根据两点间经纬度坐标（double值），计算两点间距离，单位为米
     * @param lng1
     * @param lat1
     * @param lng2
     * @param lat2
     * @return
     */
    public static double GetDistance(double lng1, double lat1, double lng2, double lat2)
    {
       double radLat1 = rad(lat1);
       double radLat2 = rad(lat2);
       double a = radLat1 - radLat2;
       double b = rad(lng1) - rad(lng2);
       double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a/2),2) + 
        Math.cos(radLat1)*Math.cos(radLat2)*Math.pow(Math.sin(b/2),2)));
       s = s * EARTH_RADIUS;
       s = Math.round(s * 10000) / 10000;
       return s;
    }
	
    private static double rad(double d)
    {
       return d * Math.PI / 180.0;
    }
	//**********************************************************//
    
    //*****************获取ObuMap的Key列表************************//
    
  //遍历所有车，计算两辆车之间的距离
    /**
	 * -----------------------------------------
	 *  obuMac1 obuMac2 distance
	 *  obuMac1 obuMac3 distance
	 *  ohuMac2 obuMac3 diatance
	 * -----------------------------------------
	 */
    public List<String> getObusList() {
	   
	   int num = obuMap.size();
	   int i;
	   int j;
	   int nua = 0;
	   //System.out.println("---->"+num);
	   MacAddress[] obus = new MacAddress[num];	
	   List<String> distanceList = new ArrayList<String>();
	   for (MacAddress obumac : obuMap.keySet()) {
		   
		   obus[nua] = obumac;
		   nua++;     
	   }
	  // countCarDistance
	   if(num >= 2) {
		   
		   for(i = 0 ; i < num ; i++) {
			  
			   for(j = i+1;j < num;j++) {
				   
				   double a = countCarDistance(obus[i],obus[j]);
				   String b = obus[i].toString()+" "+obus[j]+" "+a;
				   distanceList.add(b);
			   }   
		   }
		   return distanceList;
	   }
	   else
		   return null ;      
   }    
}
