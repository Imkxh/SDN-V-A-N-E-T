package net.floodlightcontroller.wireless.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import net.floodlightcontroller.wireless.master.WirelessMaster;

public class ObuDistanceShow extends ServerResource {
	
	private WebResponse response;

	@Get("json")
	public WebResponse retreive() throws IOException {
		response = new WebResponse();
		List<String> distanceList = new ArrayList<String>();
		WirelessMaster wm = (WirelessMaster)getContext().getAttributes().
				get(WirelessMaster.class.getCanonicalName());
		
		distanceList = wm.getObusLists();
		
		if (distanceList == null) {
    		response.setCode(WebResponse.ErrorCode.RESOURCE_NOT_EXIST.getValue());
    		response.setMsg("no other car");
    	}
    	else {
    		response.setCode(WebResponse.ErrorCode.SUCCESS.getValue());
    		response.setMsg("success");
    		response.setData(distanceList);
    	}
		
		
		return response;
	}
}
