package com.guozhong.downloader.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DriverCommand;
import org.openqa.selenium.remote.ErrorCodes;
import org.openqa.selenium.remote.JsonToBeanConverter;
import org.openqa.selenium.remote.Response;

import com.google.common.collect.ImmutableMap;
import com.guozhong.page.Status;

/**
 * 重写的ZhongChromeDriver，支持取得Cookie的功能
 * @author 郭钟
 *
 */
public final class ZhongChromeDriver extends org.openqa.selenium.chrome.ChromeDriver implements JavaScriptDriver{
	
	/**
	 * 缺省的请求等待500ms
	 */
	public static final long REQUEST_WAIT_TIME = 500;
	
	private long requestWaitTime = REQUEST_WAIT_TIME;
	
	private int index ;
	
	private int statusCode = 0;
	

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public ZhongChromeDriver() {
		super();
		// TODO Auto-generated constructor stub
	}

	public ZhongChromeDriver(Capabilities capabilities) {
		super(capabilities);
		// TODO Auto-generated constructor stub
	}

	public ZhongChromeDriver(ChromeDriverService service, Capabilities capabilities) {
		super(service, capabilities);
		// TODO Auto-generated constructor stub
	}

	public ZhongChromeDriver(ChromeDriverService service, ChromeOptions options) {
		super(service, options);
		// TODO Auto-generated constructor stub
	}

	public ZhongChromeDriver(ChromeDriverService service) {
		super(service);
		// TODO Auto-generated constructor stub
	}

	public ZhongChromeDriver(ChromeOptions options) {
		super(options);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void get(String url) {
		Response response = execute(DriverCommand.GET, ImmutableMap.of("url", url));
		statusCode = response.getStatus();
		if (statusCode  == 0) {
		     //  statusCode < 200 || statusCode > 299
			statusCode = 200;
		}else if (statusCode == ErrorCodes.UNKNOWN_COMMAND) {
		   //  response.setStatus(ErrorCodes.UNKNOWN_COMMAND);
			statusCode = 500;
		 } else {
		   //  response.setStatus(ErrorCodes.UNHANDLED_ERROR);
			 statusCode = 999;
		 }
		try {
			Thread.sleep(requestWaitTime);//考虑浏览器渲染需要时间每个页面停留
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

    @SuppressWarnings({"unchecked"})
    protected Set<Cookie> getCookies() {
      Object returned = execute(DriverCommand.GET_ALL_COOKIES).getValue();

      List<Map<String, Object>> cookies =
          new JsonToBeanConverter().convert(List.class, returned);
      Set<Cookie> toReturn = new HashSet<Cookie>();
      for (Map<String, Object> rawCookie : cookies) {
        String name = (String) rawCookie.get("name");
        String value = (String) rawCookie.get("value");
        String path = (String) rawCookie.get("path");
        String domain = (String) rawCookie.get("domain");
        boolean secure = rawCookie.containsKey("secure") && (Boolean) rawCookie.get("secure");

        Number expiryNum = (Number) rawCookie.get("expiry");
        Date expiry = expiryNum == null ? null : new Date(
            TimeUnit.SECONDS.toMillis(expiryNum.longValue()));

        toReturn.add(new Cookie.Builder(name, value)
            .path(path)
            .domain(domain)
            .isSecure(secure)
            .expiresOn(expiry)
            .build());
      }

      return toReturn;
    }
    
    public void dump(Collection<com.guozhong.request.Cookie> collection){
    	Set<Cookie> cookies = getCookies();
    	com.guozhong.request.Cookie item = null;
    	for (Cookie cookie: cookies) {
    		item = new com.guozhong.request.Cookie(cookie.getName(), cookie.getValue());
    		item.setDomain(cookie.getDomain());
    		item.setExpiry(cookie.getExpiry());
    		item.setPath(cookie.getPath());
    		collection.add(item);
		}
    }
    
    public Cookie getCookieNamed(String name) {
        Set<Cookie> allCookies = getCookies();
        for (Cookie cookie : allCookies) {
          if (cookie.getName().equals(name)) {
            return cookie;
          }
        }
        return null;
      }

    public void addCookie(Cookie cookie) {
        cookie.validate();
        execute(DriverCommand.ADD_COOKIE, ImmutableMap.of("cookie", cookie));
      }

      public void deleteCookieNamed(String name) {
        execute(DriverCommand.DELETE_COOKIE, ImmutableMap.of("name", name));
      }

      public void deleteCookie(Cookie cookie) {
        deleteCookieNamed(cookie.getName());
      }

      public void deleteAllCookies() {
        execute(DriverCommand.DELETE_ALL_COOKIES);
      }

	public long getRequestWaitTime() {
		return requestWaitTime;
	}

	/**
	 * 通常渲染页面都需要等待时间。等待多久阻绝于机器的性能和带宽，建议设置在500ms-2000ms之间
	 * @param requestWaitTime
	 */
	public void setRequestWaitTime(long requestWaitTime) {
		this.requestWaitTime = requestWaitTime;
	}

	public int getStatusCode() {
		return statusCode;
	}
	
      
}
