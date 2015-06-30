package com.guozhong.downloader.driverpool;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.openqa.selenium.WebDriver;

import com.guozhong.component.listener.WebDriverLifeListener;
import com.guozhong.downloader.impl.ZhongWebDriver;
import com.guozhong.downloader.impl.ZhongHttpClient;





/**
 * 目前只会缓存能不执行JS的Driver，因为能执行JS的Driver在多次执行后会出现报错
 * @author code4crafter@gmail.com <br>
 * Date: 13-7-26 <br>
 * Time: 下午1:41 <br>
 */
public final class WebDriverPool extends DriverPoolInterface{
	
	public final static int DEFAULT_TIMEOUT = 15;//默认加载网页超时8秒

    private final static int DEFAULT_NOTJSDRIVER = 10;

    
    /**
     * 不可执行JS的驱动
     */
    private int notjsdriver ;
    
    private final static int STAT_RUNNING = 1;

    private final static int STAT_CLODED = 2;
    
    private int pageLoadTimeout ;

    private AtomicInteger stat = new AtomicInteger(STAT_RUNNING);
    
    private List<WebDriverLifeListener> webDriverLifeListeners = new ArrayList<WebDriverLifeListener>(); 

    /**
     * 统计所有用过的webDriverList。最后释放掉
     */
    private List<ZhongWebDriver> webDriverList = Collections.synchronizedList(new ArrayList<ZhongWebDriver>());
    
    /**
     * 使用缓存队列
     */
    private LinkedBlockingQueue<ZhongWebDriver> queue = new LinkedBlockingQueue<ZhongWebDriver>();
    

    public WebDriverPool(int notjsdriver,int pageLoadTimeout) {
        this.notjsdriver = notjsdriver;
        this.pageLoadTimeout = pageLoadTimeout;
    }

    public WebDriverPool() {
        this(DEFAULT_NOTJSDRIVER,DEFAULT_TIMEOUT);
    }

    /**
     * 从池中取得一个WebDriver
     * @return
     * @throws InterruptedException
     */
    public final ZhongWebDriver get(boolean isExeJs) throws InterruptedException {
        checkRunning();
        ZhongWebDriver poll ;
        if(isExeJs){
        	poll = new ZhongWebDriver(true);
        }else{
        	if(webDriverList.size() < min_drivers){
        		synchronized (webDriverList) {
        			if(webDriverList.size() < min_drivers){
        				createExtendWebDriver();
        			}
        		}
        	}
        	poll = queue.poll();
        }
        
        if (poll != null) {
            return poll;
        }
        
        if (webDriverList.size() < max_drivers) {//如果webDriver使用的数量没有达到capacity则继续创建webDriver
            synchronized (webDriverList) {
                if (webDriverList.size() < max_drivers) {
                	createExtendWebDriver();
                }
            }
        }
        return queue.take();//此方法并不保证立即返回WebDriver，有可能等待之前的WebDriver执行完回到pool中
    }

	private void createExtendWebDriver() {
		ZhongWebDriver e = new ZhongWebDriver(false);
		int driverIndex = webDriverList.size();
		e.setIndex(driverIndex);
		e.manage().timeouts().pageLoadTimeout(pageLoadTimeout, TimeUnit.SECONDS);
		invokeListener(e,true);
		queue.add(e);
		webDriverList.add(e);
	}

    public final void returnToPool(ZhongWebDriver webDriver) {//将WebDriver添加到pool中
        checkRunning();
        if(webDriver.isJavascriptEnabled()){
        	webDriver.quit();
        }else{
        	webDriver.clearHeaders();
        	queue.add(webDriver);
        }
    }
    
    /**
     * 监测是否在运行
     */
    protected final void checkRunning() {
        if (!stat.compareAndSet(STAT_RUNNING, STAT_RUNNING)) {
            throw new IllegalStateException("Already closed! please open");
        }
    }
    
    /**
     * 打开
     */
    public final void open(){
    	if (!stat.compareAndSet(STAT_CLODED, STAT_RUNNING)) {
            //throw new IllegalStateException("Already open!");
            System.out.println("WebDriverPool Already open!");
        }
    }

    /**
     * 关闭所有的WebDriver
     */
    public final void closeAll() {
        boolean b = stat.compareAndSet(STAT_RUNNING, STAT_CLODED);
        if (!b) {
            throw new IllegalStateException("Already closed!");
        }
        for (ZhongWebDriver webDriver : webDriverList) {
        	invokeListener(webDriver, false);
            webDriver.quit();
        }
        webDriverList.clear();
        queue.clear();
    }
    
    public final void setPageLoadTimeout(int timeout){
    	for (WebDriver driver : webDriverList) {
			driver.manage().timeouts().pageLoadTimeout(timeout, TimeUnit.SECONDS);
		}
    	this.pageLoadTimeout = timeout;
    }

	@Override
	public Object getDriver(int driverIndex) {
		if(getIndexs.contains(driverIndex)){
			return null;
		}else{
			getIndexs.add(driverIndex);
		}
		for (ZhongWebDriver client : this.webDriverList) {
			if(client.getIndex() == driverIndex){
				queue.remove(client);//队列移除实例。防止处理未完成之前使用
				return client;
			}
		}
		return null;
	}

	@Override
	public void handleComplete(Object driver) {
		ZhongWebDriver extendWebDriver = (ZhongWebDriver) driver;
		getIndexs.remove(extendWebDriver.getIndex());//清除限制
    	queue.add(extendWebDriver);//回到队列
	}
	
	public void addWebDriverLifeListener(WebDriverLifeListener webDriverLifeListener){
		if(!webDriverLifeListeners.contains(webDriverLifeListener)){
			webDriverLifeListeners.add(webDriverLifeListener);
		}
	}
	
	public boolean removeWebDriverLifeListener(WebDriverLifeListener webDriverLifeListener){
		int index = webDriverLifeListeners.indexOf(webDriverLifeListener);
		if(index != -1){
			webDriverLifeListeners.remove(index);
		}
		return index !=-1;
	}
	
	private void invokeListener(ZhongWebDriver armWebDriver,boolean isCreate){
		for (WebDriverLifeListener webDriverLifeListener : webDriverLifeListeners) {
			if(isCreate){
				webDriverLifeListener.onCreated(armWebDriver.getIndex(), armWebDriver);
			}else{
				webDriverLifeListener.onQuit(armWebDriver.getIndex(), armWebDriver);
			}
		}
	}

}
