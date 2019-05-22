package net.floodlightcontroller.wireless.master;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.projectfloodlight.openflow.types.MacAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RsuMsgServer implements Runnable {


	protected static Logger log = LoggerFactory.getLogger(RsuMsgServer.class);

	// Wireless Message types
	private final String WIRELESS_MSG_PING = "ping";
	private final String WIRELESS_MSG_PUBLISH = "publish";
	private final String WIRELESS_MSG_TEST = "test";
	
	
	private final int ODIN_SERVER_PORT;
	private final int MSG_FORMAT_SECTIONS = 2;

	private DatagramSocket controllerSocket;
	private final ExecutorService executor;
	private final WirelessMaster carMaster;

	public RsuMsgServer(WirelessMaster om, int port, ExecutorService executor) {
		this.carMaster = om;
		this.ODIN_SERVER_PORT = port;
		this.executor = executor;
	}

	@Override
	public void run() {

		try {
			controllerSocket = new DatagramSocket(ODIN_SERVER_PORT);//UDP
		} catch (IOException e) {
			e.printStackTrace();
		}
//建立UDP连接，接收来自AP主动上传的信息
		while (true) {
			try {
				final byte[] receiveData = new byte[1024]; 
				final DatagramPacket receivedPacket = new DatagramPacket(receiveData, receiveData.length);
				controllerSocket.receive(receivedPacket);
				executor.execute(new AgentMsgHandler(receivedPacket));
			} catch (IOException e) {
				log.error("controllerSocket.accept() failed: " + ODIN_SERVER_PORT);
				e.printStackTrace();
				System.exit(-1);
			}
		}
	}

	/** Protocol handlers **/

	/**
	 * ---> 处理来自agent的ping消息
	 * @param agentAddr
	 */
	/*private void receivePing(final InetAddress agentAddr) {
		carMaster.handleAgentPing(agentAddr);
	}*/
	
	
	private void receiveTest(final InetAddress agentAddr,final String msg) {
		carMaster.handleRsuTest(agentAddr,msg);
	}
	
	
	private String analysisMsg (String msg) {
		/**
		 * 
		 */
		final String[] reMsg = msg.split("#");
		//int len = reMsg.length;
		
		/*int i;
		for(i = 0 ; i < len ; i++) {	
			System.out.println("re--->"+reMsg[i]+"<---");
		}*/
		//经度
		String lngs = reMsg[2];
		String[] lng = lngs.split(" ");
		//纬度
		String lats = reMsg[3];
		String[] lat = lats.split(" ");
		//System.out.println("lat--->"+lat[1]+"<---");
		//速度
		/*String mphs = reMsg[5];	
		String[] mph = mphs.split(" ");*/
		
		String newMsg = reMsg[0]+"#"+reMsg[1]+"#"+lng[0]+"#"+lat[1]+
				"#"+reMsg[4]+"#"+reMsg[5]+"#"+reMsg[6]+"#"+reMsg[7]+"#"+reMsg[8]+"#"+reMsg[9]+"#"+reMsg[10];
		
		/**
		 *   msg--->44:94:fc:95:d3:73#2019-04-24t07:59:01.000z#106.60440367 e# 29.53913128 n#1493.383 ft#0.00 mph#0.0 deg (true)#0.00 ft/min#3d fix (10455 secs)#08:bd:43:be:34:85#-34
         *newMsg--->44:94:fc:95:d3:73#2019-04-24t07:59:01.000z#106.60440367#29.53913128#1493.383 ft#0.00 mph#0.0 deg (true)#0.00 ft/min#3d fix (10455 secs)#08:bd:43:be:34:85#-34
		 */
		//System.out.println("newMsg--->"+newMsg+"<---");
		return newMsg;
	}
	
	private String getSafeMsg(InetAddress agentAddr,String msg) {
		
		final String[] reMsg = msg.split("#");
		String safeMsg = agentAddr.getHostAddress()+" "+reMsg[0]+" "+reMsg[9];

		return safeMsg;
	}
	
	//*********************订阅模块暂时不处理*********************//
/*	
	*//**
	 * ---> 接收agent主动上传的消息并处理
	 * @param agentAddr agnet的IP地址
	 * @param clientHwAddress 终端的MAC地址
	 * @param eventType 订阅事件的类型，主要标识agent主动上传的消息类型，String类型
	 * @param params 订阅事件的参数，String类型
	 *//*
	private void receivePublish(final InetAddress agentAddr, 
			final MacAddress clientHwAddress, final String eventType, final String params) {
		wirelessMaster.handleAgentPublish(agentAddr, clientHwAddress, eventType, params);
	}*/


	private class AgentMsgHandler implements Runnable {
		final DatagramPacket receivedPacket;

		public AgentMsgHandler(final DatagramPacket dp) {
			receivedPacket = dp;
		}

		// Agent message handler
		public void run() {
			
			/**
			 * 传入数据格式：:
			 * ----------------------------------------------------------------------------------------------------------
			 * | obu1_mac | time | longitude | latitude | altitude | speed | heading | climb | status | obu2_mac | rssi |
			 * ----------------------------------------------------------------------------------------------------------
			 * 
			 * -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
			 * | 44:94:fc:95:d3:73 | 2019-04-24t07:59:01.000z | 106.60440367 | 29.53913128 | 1493.383 ft| 0.00 | 0.0 deg (true)| 0.00 ft/min | 3d fix (10455 secs) | 08:bd:43:be:34:85 | -34 |
			 * -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
			 */
			
			
			final String msg = new String(receivedPacket.getData()).trim().toLowerCase();
			final InetAddress agentAddr = receivedPacket.getAddress();
			
			//System.out.println("--->: "+msg);
			String newMsg = analysisMsg(msg);
			
			carMaster.buildTcp(agentAddr);//建立与rsu之间的TCP连接
			//carMaster.handleRsuTest(agentAddr,newMsg);//处理MSG消息(不完善，没有监听模块)
			carMaster.handleRsuMsg(newMsg);
			
			//**********************处理安全消息**************************//
			String safeMsg = getSafeMsg(agentAddr,msg);
			//System.out.println("safeMsg--->"+safeMsg+"<---");
			carMaster.handleSafeMsg(safeMsg);
			
			//System.out.println(agentAddr);
		/*	if (msg.equals(WIRELESS_MSG_PING)) {
				receivePing(agentAddr);
				return;
			}*/
			
			/**
			 * message format:
			 * -----------------------------------------
			 * | type | client mac | payload		   |
			 * -----------------------------------------
			 */
			final String[] fields = msg.split(" ", MSG_FORMAT_SECTIONS);
			final String msg_type = fields[0];
//			final String staAddress = fields[1];
			
			
			switch (msg_type) {
			/*case WIRELESS_MSG_PUBLISH://只是上传的无缝切换的订阅
				if (fields.length < 3) {
					return;
				}
				String payload = fields[2];
				String[] subFields = payload.split(" ", 2);
				if (subFields.length == 2) {
					receivePublish(agentAddr, MacAddress.of(staAddress), 
							subFields[0], subFields[1]);
				}
				break;*/
			/**
			 * 数据格式：:
			 * -------------------------------------------------------------
			 * | type | client mac | longitude | latitude | speed | rssilist |
			 * -------------------------------------------------------------
			 */
			case WIRELESS_MSG_TEST:
				if (fields.length < 2) {
					return;
				}
				
				String payload = fields[1];
				
				System.out.println("bbb: "+payload);
				receiveTest(agentAddr, payload );				
				
				break;
			}
		}
	}

	

}
