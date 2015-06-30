package com.guozhong.util;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class PropertiesUtils {
	
	public static Map<String, String> getFieldByProperty(String filename){
		HashMap<String, String> map = new HashMap<String, String>();
		Properties properties = new Properties();
		InputStream in = null;
		in = PropertiesUtils.class.getResourceAsStream("/" + filename);
		try{
			properties.load(in);
			
			Set<Object> set = properties.keySet();
			for (Object key : set) {
				map.put(key.toString(), properties.getProperty(key.toString()));
			}
			
            properties.clear();
            properties = null;
			in.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		if (map.isEmpty()) {
			return null;
		} else {
			return map;
		}
	}
}
