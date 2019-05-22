package net.floodlightcontroller.wireless.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import net.floodlightcontroller.wireless.master.CarObu;
import net.floodlightcontroller.wireless.master.WirelessMaster;

public class ObuInfoShow extends ServerResource {
	
	private WebResponse response;
	@Get("json")
	public WebResponse retreive() throws IOException {
		
		response = new WebResponse();
		List<CarObu> obuList = new ArrayList<>();
    	WirelessMaster wm = (WirelessMaster)getContext().getAttributes().
        					get(WirelessMaster.class.getCanonicalName());
    	for (CarObu wc : wm.getObus()){
    		/*
    		 * Only add the clients that have completed the connection.
    		 */
    			obuList.add(wc);
    		
    	}
    	//System.out.println("LLLLLLLLLLLLLLLLLLLLLLLLL");
    	if (obuList.size() == 0) {
    		response.setCode(WebResponse.ErrorCode.RESOURCE_NOT_EXIST.getValue());
    		response.setMsg("no car");
    	}
    	else {
    		response.setCode(WebResponse.ErrorCode.SUCCESS.getValue());
    		response.setMsg("success");
    		response.setData(obuList);
    	}
    	return response;
    }
		
}

