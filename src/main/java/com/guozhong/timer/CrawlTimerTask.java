package com.guozhong.timer;

import java.util.Calendar;
import java.util.TimerTask;

import com.guozhong.CrawlTask;


public final class CrawlTimerTask extends TimerTask{

	/**
	 * 每天开始爬行的时间
	 */
	protected int hour ;
	
	/**
	 * 执行各后续任务之间的时间间隔，单位是毫秒。
	 */
	protected long period ;
	
	protected int endHour;
	
	protected CrawlTask crawlTask;
	
	public CrawlTimerTask(int satrthour,long period, int endHour,CrawlTask crawlTask) {
		this.hour = satrthour;
		this.period = period;
		this.endHour = endHour;
		this.crawlTask = crawlTask;
	}
	
	public CrawlTimerTask getNextStepTask(){
		return new CrawlTimerTask(hour,  period, endHour, crawlTask); 
	}
	
	
	@Override
	public void run() {
		while(!runable()){
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		new Thread(crawlTask).start();
	}
	
	private final boolean runable(){
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());
		if((calendar.get(Calendar.HOUR_OF_DAY) >= hour && calendar.get(Calendar.HOUR_OF_DAY) <= endHour)){
			return true;
		}
		return false;
	}

	public int getHour() {
		return hour;
	}

	public long getPeriod() {
		return period;
	}
	
	public int getEndHour() {
		return endHour;
	}
}