package com.fgtit.app;

public class TempVals {
	
	private static TempVals instance;
	
	
	public static TempVals getInstance() {
    	if(null == instance) {
    		instance = new TempVals();
    	}
    	return instance;
    }
	
	public byte[] tempInfo=new byte[512];
	public byte[] tempFP=new byte[2048];
	public int fpCount=0;
}
