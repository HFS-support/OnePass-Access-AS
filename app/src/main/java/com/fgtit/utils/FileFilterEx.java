package com.fgtit.utils;

import java.io.File;
import java.io.FileFilter;

public class FileFilterEx implements FileFilter {
	String condition = "";  
	
	public FileFilterEx(String condition) {  
		this.condition = condition;  
	} 
	
	@Override  
	public boolean accept(File pathname) {  
		 String filename = pathname.getName();  
		 if (filename.lastIndexOf(condition) != -1) {  
			 return true;  
		 } else  
			 return false;  
	}

}
