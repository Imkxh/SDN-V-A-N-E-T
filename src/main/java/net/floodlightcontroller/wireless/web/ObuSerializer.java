package net.floodlightcontroller.wireless.web;

import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.ArrayList;

import org.projectfloodlight.openflow.types.MacAddress;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import net.floodlightcontroller.wireless.master.CarObu;

/*import net.floodlightcontroller.wireless.master.IApAgent;
import net.floodlightcontroller.wireless.applications.ClientIPPing;*/

public class ObuSerializer extends JsonSerializer<CarObu> {
	
	@Override
	public void serialize(CarObu carobu, JsonGenerator jgen, 
			SerializerProvider provider) throws IOException, JsonProcessingException {
		jgen.writeStartObject();
		jgen.writeStringField("macAddress", carobu.getMacAddress().toString());
		jgen.writeStringField("time", carobu.getStatus().getObuTime());
		jgen.writeStringField("longitude", carobu.getStatus().getLongitude());
		jgen.writeStringField("latitude", carobu.getStatus().getLatitude());
		jgen.writeStringField("altitude", carobu.getStatus().getAltitude());
		jgen.writeStringField("speed", carobu.getStatus().getSpeed());
		jgen.writeStringField("heading", carobu.getStatus().getHeading());
		jgen.writeStringField("climb", carobu.getStatus().getClimb());
		
		jgen.writeObjectFieldStart("rssiMap");
		
		List<String> rssiList = new ArrayList<>();
		for (MacAddress obuMac : carobu.getStatus().getRssiMap().keySet()) {
			
			String obumac = obuMac.toString();
			String rssi = carobu.getStatus().getRssiMap().get(obuMac);
			String arssiLists = obumac+" "+rssi;
			rssiList.add(arssiLists);
			jgen.writeStringField(obumac, rssi);
			
		}
	//jgen.writeStringField("rssiMap", carobu.getStatus().getRssiMap());
		
		jgen.writeEndObject();
		//jgen.writeStringField("rssilist", carobu.getStatus().getRssiList());
		jgen.writeEndObject();
	}
}
