package guozhong.kanzhun.proxy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.guozhong.proxy.ProxyIp;
import com.guozhong.proxy.ProxyIpPool;

public class KanzhunProxyIpPool extends ProxyIpPool {
	public static final String IP_RESOURCE = "你的代理IP请求地址";//地址请求的个数必须设置为initProxyIp(int size)中size的个数
	public KanzhunProxyIpPool(int initSize, long pastTime, int max_use_count) {
		super(initSize, pastTime, max_use_count);
	}

	private Pattern extractIp = Pattern.compile("([\\d]{1,3}\\.[\\d]{1,3}\\.[\\d]{1,3}\\.[\\d]{1,3}):(\\d+)");
	
	@Override
	protected List<ProxyIp> initProxyIp(int size) throws Exception {
		List<ProxyIp>   ip = new ArrayList<ProxyIp>();
		URL url = null;
		BufferedReader br = null;
		StringBuffer buf = new StringBuffer();
		try {
			url = new URL(IP_RESOURCE);
			InputStream in = url.openStream();
			br = new BufferedReader(new InputStreamReader(in,"utf-8"));
			String temp = null;
			
			while((temp = br.readLine())!=null){
				buf.append(temp).append("\n");
			}
			
			ProxyIp proxy = null;
			Matcher matcher = extractIp.matcher(buf);
			while(matcher.find()){
				proxy = new ProxyIp(matcher.group(1), Integer.parseInt(matcher.group(2)));
				ip.add(proxy);
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(buf);
		}finally{
			if(br != null){
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return ip;
	}

}
