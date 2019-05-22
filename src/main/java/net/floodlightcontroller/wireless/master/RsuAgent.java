package net.floodlightcontroller.wireless.master;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import org.projectfloodlight.openflow.types.MacAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import net.floodlightcontroller.core.IOFSwitch;

public class RsuAgent implements IRsuAgent {

	protected static Logger log = LoggerFactory.getLogger(WirelessMaster.class);
	
	private static final String WRITE_HANDLER_TEXT = "text";
	private static final String WRITE_HANDLER_SAFE = "1";
	private static final String WEITE_HANDLER_POWER = "2";
	private static final String READ_HANDLER_CLIENT_STATS = "client_stats";
	private static final String READ_GET_AP_MAC = "get_ap_mac";
	
	// Connect to control socket on WirelessAgent
		private Socket odinAgentSocket = null;
		private PrintWriter outBuf;
		private BufferedReader inBuf;
		private IOFSwitch ofSwitch;
		private long lastHeard;
		private InetAddress ipAddress;
		private MacAddress apMac = null;
		private RsuInfo rsuInfo;
		private boolean isReboot;
		
		private final int ODIN_AGENT_PORT = 6777;
		
		public void sendText() {
			/*
			String text = "ssssssssssss";
			invokeWriteHandler(WRITE_HANDLER_TEXT, text);*/
			
			String text = invokeReadHandler(READ_GET_AP_MAC, null);
			System.out.println("ssss--->"+text);
			
		}
		
		public void addObuDistanceMsg(String msg) {
			
			invokeObuWriteHandler(WRITE_HANDLER_SAFE,msg);
		}
		
		public void addObuPowerMsg(String msg) {
			
			invokeObuWriteHandler(WEITE_HANDLER_POWER,msg);
		}
		
		
		public boolean getisReboot() {
			return isReboot;
		}
		public void setisReboot(boolean bl) {
			this.isReboot = bl;
		}
		
		
		public InetAddress getIpAddress() {
			return ipAddress;
		}
		
		public long getLastHeard() {
			return lastHeard;
		}

		public void setLastHeard(long t) {
			this.lastHeard = t;
		}
		
		public int init(InetAddress host) {
			/**
			 * FIXME: need to add openflow entry.
			 */
			try {
				odinAgentSocket = new Socket(host.getHostAddress(), ODIN_AGENT_PORT);
				outBuf = new PrintWriter(odinAgentSocket.getOutputStream(), true);
				inBuf = new BufferedReader(new InputStreamReader(odinAgentSocket
						.getInputStream()));
			} catch (UnknownHostException e) {
				e.printStackTrace();
				return -1;
			} catch (IOException e) {
				e.printStackTrace();
				return -1;
			}

			ipAddress = host;
		/*	connectedTime = System.currentTimeMillis();
			clientSet = new HashSet<WirelessClient>();*/
			return 0;
		}
		
		
		//******************重写IRsuAgent方法********************//
		
		//**********************************************//
		//对Obu的下发控制消息
		private synchronized void invokeObuWriteHandler(String handlerName,
				String handlerText) {
			String str = "2" + " " + handlerName + " " + handlerText;
			
			//System.out.println("str--->"+str);
			outBuf.println(str.toLowerCase());
		}
		//对Rsu的下发控制消息
		private synchronized void invokeRsuWriteHandler(String handlerName,
				String handlerText) {
			String str = "1" + " " + handlerName + " " + handlerText;
			outBuf.println(str.toLowerCase());
		}
		
		/**
		 * Internal method to invoke a read handler on the WiAgent
		 * 
		 * @param handlerName OdinAgent handler
		 * @return read-handler string
		 */
		private synchronized String invokeReadHandler(String handlerName, String handlerText) {

			if (handlerText == null || handlerText.equals("")) {
				outBuf.println("read " + handlerName);
			}
			else {
				outBuf.println("read " + handlerName + " " + handlerText);
			}
			
			/*
			 * received read data format:
			 * ----------------------------------------
			 * wiagent handlerName data_length data
			 * ----------------------------------------
			 */
			try {
				/**
				 * parse header
				 */
				int blankNum = 0;
				char[] headbuf = new char[64];
				int i = 0;
				while(true) {
					char c = (char) inBuf.read();
					if (c == ' ') {
						blankNum++;
						if (blankNum == 3) break;
					}
					headbuf[i++] = c;
					if (i ==64) {
						log.warn("invokeReadHandler received data exceeds the set length");
						return null;
					}
				}
				String[] head = new String(headbuf).trim().split(" ");
				if (!head[1].equals(handlerName)) return null;
				
				/**
				 * parse data
				 */
				int dataLength = 0;
				try {
					dataLength = Integer.parseInt(head[2]);
				} catch (NumberFormatException e) {
					e.printStackTrace();
					return null;
				}
				char[] databuf = new char[dataLength];
				for (i = 0; i < dataLength; i++) {
					char c = (char) inBuf.read();
					databuf[i] = c;
				}
				String data = new String(databuf);
				return data;
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}
		
}
