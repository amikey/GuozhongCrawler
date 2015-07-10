package com.guozhong.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class JsoupUtil {
	
	/**
	 * 抽取form表单中所有的键值对
	 * @param form
	 * @param exclude  排除的字段
	 * @return
	 */
	public static Map<String,String> extraFormValue(Element form ,String ... exclude){
		if(form == null){
			return null;
		}
		Map<String,String> keyValuePair = new HashMap<String, String>();
		List<String> eliminate = null;
		if(exclude != null){
			eliminate = Arrays.asList(exclude);
		}
		Elements inputs = form.select("input[name]");
		for (Element input : inputs) {
			String name = input.attr("name");
			if( eliminate!=null && eliminate.contains(name)){
				continue;
			}
			keyValuePair.put(name, input.attr("value"));
		}
		return keyValuePair;
	}
}
