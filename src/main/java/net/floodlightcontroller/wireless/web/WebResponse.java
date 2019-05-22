package net.floodlightcontroller.wireless.web;

public class WebResponse {
	
	private int code;
	private String msg;
	private Object data;
	
	public WebResponse() {
		
	}
	

	public int getCode() {
		return code;
	}


	public void setCode(int code) {
		this.code = code;
	}


	public String getMsg() {
		return msg;
	}
	
	public void setMsg(String msg) {
		this.msg = msg;
	}
	
	public Object getData() {
		return data;
	}
	
	public void setData(Object data) {
		this.data = data;
	}
	
	public static enum ErrorCode {
		SUCCESS(1000),
		PARAMS_MISSING(2000),
		PARAMS_ILLEGAL(2001),
		RESOURCE_NOT_EXIST(3000);
		
		private final int value;
		
		ErrorCode(int value) {
			this.value = value;
		}
		
		public int getValue() {
			return value;
		}
	}
}
