package com.guozhong.downloader.driverpool;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.openqa.selenium.chrome.ChromeDriver;

import com.guozhong.component.listener.ChromeDriverLifeListener;
import com.guozhong.downloader.impl.ZhongChromeDriver;
import com.guozhong.downloader.impl.ZhongHttpClient;




/**
 * 目前只会缓存能不执行JS的Driver，因为能执行JS的Driver在多次执行后会出现报错
 * Date: 13-7-26 <br>
 * Time: 下午1:41 <br>
 */
public final class ChromeWebDriverPool extends DriverPoolInterface{
	
	public final static int DEFAULT_TIMEOUT = 30;//默认加载网页超时8秒

	public final static int DEFAULT_MAX_DRIVER = 10;//默认最大开启的谷歌浏览器数
    
    private final static int STAT_RUNNING = 1;

    private final static int STAT_CLODED = 2;
    
    private int pageLoadTimeout ;
    
    private AtomicInteger stat = new AtomicInteger(STAT_RUNNING);

    /**
     * 统计所有用过的webDriverList。好最后释放掉
     */
    private List<com.guozhong.downloader.impl.ZhongChromeDriver> webDriverList = Collections.synchronizedList(new ArrayList<com.guozhong.downloader.impl.ZhongChromeDriver>());
    /**
     * store webDrivers available
     */
    private BlockingDeque<com.guozhong.downloader.impl.ZhongChromeDriver> queue = new LinkedBlockingDeque<com.guozhong.downloader.impl.ZhongChromeDriver>();
    
    /**
     * 监听器
     */
    private List<ChromeDriverLifeListener> chromeDriverLifeListeners = new ArrayList<ChromeDriverLifeListener>();
    

    public ChromeWebDriverPool(int max_driver ,int pageLoadTimeout) {
        this.pageLoadTimeout = pageLoadTimeout;
        this.max_drivers = max_driver;
    }

    public ChromeWebDriverPool() {
        this(DEFAULT_MAX_DRIVER,DEFAULT_TIMEOUT);
    }

    /**
     * 从池中取得一个WebDriver
     * @return
     * @throws InterruptedException
     */
    public final ZhongChromeDriver get() throws InterruptedException {
        checkRunning();
        com.guozhong.downloader.impl.ZhongChromeDriver poll ;
        if(webDriverList.size() < min_drivers){
        	synchronized (webDriverList) {
        		if(webDriverList.size() < min_drivers){
        			createChromeWebDriver();
        		}
        	}
        }
        poll = queue.poll();
        if (poll != null) {
            return poll;
        }
        if (webDriverList.size() < max_drivers) {//如果webDriver使用的数量美誉达到capacity则继续创建webDriver
            synchronized (webDriverList) {
                if (webDriverList.size() < max_drivers) {
                	createChromeWebDriver();
                }
            }
        }
        return queue.take();//此方法并不保证立即返回WebDriver，有可能等待之前的WebDriver执行完回到pool中
    }

	private void createChromeWebDriver() {
		com.guozhong.downloader.impl.ZhongChromeDriver e = new com.guozhong.downloader.impl.ZhongChromeDriver();
		int driverIndex = webDriverList.size();
		e.setIndex(driverIndex);
		e.manage().timeouts().pageLoadTimeout(pageLoadTimeout, TimeUnit.SECONDS);
		invokeListener(e, true);
		queue.add(e);
		webDriverList.add(e);
	}

    public final void returnToPool(com.guozhong.downloader.impl.ZhongChromeDriver webDriver) {//将WebDriver添加到pool中
        checkRunning();
        queue.add(webDriver);
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
            System.out.println("ChromeWebDriverPool Already open!");
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
        for (com.guozhong.downloader.impl.ZhongChromeDriver chromeDriver : webDriverList) {
        	invokeListener(chromeDriver, false);
        	chromeDriver.quit();
        }
        webDriverList.clear();
        queue.clear();
    }
    
    public final void setPageLoadTimeout(int timeout){
    	for (ChromeDriver driver : webDriverList) {
			driver.manage().timeouts().pageLoadTimeout(timeout, TimeUnit.SECONDS);
		}
    	this.pageLoadTimeout = timeout;
    }


	public void addChromeDriverLifeListener(ChromeDriverLifeListener chromeDriverLifeListener) {
		if(!chromeDriverLifeListeners.contains(chromeDriverLifeListener)){
			chromeDriverLifeListeners.add(chromeDriverLifeListener);
		}
	}
	
	public boolean removeChromeDriverLifeListener(ChromeDriverLifeListener chromeDriverLifeListener){
		int index = chromeDriverLifeListeners.indexOf(chromeDriverLifeListener);
		if(index != -1){
			chromeDriverLifeListeners.remove(chromeDriverLifeListener);
		}
		return index != -1;
	}
	
	private void invokeListener(com.guozhong.downloader.impl.ZhongChromeDriver chromeDriver ,boolean isCreate){
		for (ChromeDriverLifeListener chromeDriverLifeListener : chromeDriverLifeListeners) {
			if(isCreate){
				chromeDriverLifeListener.onCreated(chromeDriver.getIndex(), chromeDriver);
			}else{
				chromeDriverLifeListener.onQuit(chromeDriver.getIndex(), chromeDriver);
			}
		}
	}

	@Override
	public Object getDriver(int driverIndex) {
		if(getIndexs.contains(driverIndex)){
			return null;
		}else{
			getIndexs.add(driverIndex);
		}
		for (com.guozhong.downloader.impl.ZhongChromeDriver client : webDriverList) {
			if(client.getIndex() == driverIndex){
				queue.remove(client);//队列移除实例。防止处理未完成之前使用
				return client;
			}
		}
		return null;
	}


	@Override
	public void handleComplete(Object driver) {
		com.guozhong.downloader.impl.ZhongChromeDriver chromeDriver = (com.guozhong.downloader.impl.ZhongChromeDriver) driver;
		getIndexs.remove(chromeDriver.getIndex());//清除限制
    	System.out.println("加入"+chromeDriver.getIndex());
    	queue.add(chromeDriver);//回到队列
	}

}
