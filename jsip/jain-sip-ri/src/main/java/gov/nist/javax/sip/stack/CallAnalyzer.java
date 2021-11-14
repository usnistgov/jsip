/*
 * Conditions Of Use
 *
 * This software was developed by employees of the National Institute of
 * Standards and Technology (NIST), an agency of the Federal Government.
 * Pursuant to title 15 Untied States Code Section 105, works of NIST
 * employees are not subject to copyright protection in the United States
 * and are considered to be in the public domain.  As a result, a formal
 * license is not needed to use the software.
 *
 * This software is provided by NIST as a service and is expressly
 * provided "AS IS."  NIST MAKES NO WARRANTY OF ANY KIND, EXPRESS, IMPLIED
 * OR STATUTORY, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTY OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, NON-INFRINGEMENT
 * AND DATA ACCURACY.  NIST does not warrant or make any representations
 * regarding the use of the software or the results thereof, including but
 * not limited to the correctness, accuracy, reliability or usefulness of
 * the software.
 *
 * Permission to use this software is contingent upon your acceptance
 * of the terms of this agreement
 *
 * .
 *
 */
package gov.nist.javax.sip.stack;
import gov.nist.core.CommonLogger;
import gov.nist.core.StackLogger;
import gov.nist.javax.sip.SipStackImpl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.WeakHashMap;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * This class is a simple thread analysis utility which tracks the time each request is stuck inside a JAIN SIP thread.
 * It also runs periodic audits. If a request is stuck for too long a thread dump is logged. The parameters are specified
 * in the MetricAnalysisConfiguration class and is dynamically reconfigurable.
 * 
 * All fields are public without getters and setters for performance.
 * 
 * Most of the synchronization is achieved without locks
 * 
 * @author Vladimir Ralev
 *
 */
public  class CallAnalyzer {
	private static StackLogger logger = CommonLogger.getLogger(CallAnalyzer.class);
	
	/*
	 * This is a Thread -> Hashmap association, each hashmap can contain multiple metricts for the thread
	 */
	private Map<Thread, HashMap<MetricReference, Object>> threadMap = 
		new WeakHashMap<Thread, HashMap<MetricReference, Object>>();
	
	/*
	 * Here we collect statistics about each metric over all threads (sum, avg, etc)
	 */
	private MetricReferenceMap metricStatisticsMap = new MetricReferenceMap();
	
	private Timer timer = new Timer();
	private SipStackImpl stack;
	
	public CallAnalyzer(SipStackImpl stack) {
		this.stack = stack;
	}
	
	public static class TImeMetricInfo {
		public Long totalTime = new Long(0);
		public Long numberOfEvents = new Long(0);
		public Long averageTime = new Long(1);
		public Long lastLoggedEventTime = new Long(0);
		protected TimerTask task;
		protected MetricAnalysisConfiguration config = new MetricAnalysisConfiguration(5000, 5000, 5000); // default config
	}
	
	/**
	 * Call this method to reconfigure the given metric
	 * 
	 * @param ref
	 * @param config
	 */
	public void configure(MetricReference ref, MetricAnalysisConfiguration config) {
		metricStatisticsMap.get(ref).config = config;
		if(!isAnalysisStarted(ref)) {
			startAnalysis(ref);
		}
	}
	
	/**
	 * If the startAnalysis method was called and not stopped
	 * @param ref
	 * @return
	 */
	public boolean isAnalysisStarted(MetricReference ref) {
		return metricStatisticsMap.get(ref).task != null;
	}
	
	/**
	 * Get the current stats for the metric over all threads
	 * @param ref
	 * @return
	 */
	public TImeMetricInfo getMetricStats(MetricReference ref) {
		return metricStatisticsMap.get(ref);
	}
	
	/**
	 * This is the configuration for the analysis task.
	 * The analysis job will be run every checkingInterval milliseconds. If an error occurs
	 * a thread dump will be logged, but there will be only one dump per minimumDumpInterval
	 * milliseconds. The dump will be logged only if a request is stuck in some thread for more
	 * than stuckTimeBeforeDump milliseconds.
	 * @author vladimirralev
	 *
	 */
	public static class MetricAnalysisConfiguration {
		public MetricAnalysisConfiguration(Long checkingInterval, Long minDumpInterval, Long stuckTimerBeforeDump) {
			this.checkingInterval = checkingInterval;
			this.minimumDumpInterval = minDumpInterval;
			this.stuckTimeBeforeDump = stuckTimerBeforeDump;
		}
		public MetricAnalysisConfiguration(int checkingInterval, int minDumpInterval, int stuckTimerBeforeDump) {
			this.checkingInterval = new Long(checkingInterval);
			this.minimumDumpInterval = new Long(minDumpInterval);
			this.stuckTimeBeforeDump = new Long(stuckTimerBeforeDump);
		}
		protected Long checkingInterval;
		protected Long minimumDumpInterval;
		protected Long stuckTimeBeforeDump;
	}
	
	/**
	 * This is just a name for certain statistic item. Such as a timestmp of request associated with a thread
	 * @author vladimirralev
	 *
	 */
	public static class MetricReference {
		public MetricReference(String name) {
			this.name = name;
		}
		public boolean equals(Object other) {
			if(other instanceof MetricReference) {
				MetricReference stat =(MetricReference) other;
				return stat.name.equals(this.name);
			}
			return false;
		}
		public int hashCode() {
			return this.name.hashCode();
		}
		public String name;
	}
	
	public static class MetricReferenceMap extends WeakHashMap<MetricReference, TImeMetricInfo> {
		/**
		 * 
		 */
		private static final long serialVersionUID = 393231609328924828L;

		public TImeMetricInfo get(Object key) {
			if(super.get(key) == null) {
				super.put((MetricReference) key, new TImeMetricInfo());
			}
			return super.get(key);
		}
	}
	
	public static class StackTrace {
		public StackTrace(int delta, String trace) {
			this.delta = delta;
			this.trace = trace;
		}
		public int delta;
		public String trace;
	}
	
	public static class ThreadInfo {
		public LinkedList<StackTrace> stackTraces = new LinkedList<StackTrace>();
		public Object data;
	}
	
	/**
	 * Rest all stats and start over with sum, average, etc
	 * @param metricReference
	 */
	public void resetStats(MetricReference metricReference) {
		TImeMetricInfo info = metricStatisticsMap.get(metricReference);
		info.totalTime = new Long(0);
		info.numberOfEvents = new Long(0);
		info.averageTime = new Long(1);
		info.lastLoggedEventTime = new Long(0);
	}

	public CallAnalyzer() {
	}
	
	/**
	 * Stop the analysis for a given metric, the analysis periodic job will be stopped and stack
	 * traces will no longer be produced.
	 * @param metricReference
	 */
	public void stopAnalysis(final MetricReference metricReference) {
		final TImeMetricInfo statInfo = metricStatisticsMap.get(metricReference);
		if(statInfo.task != null) {
			statInfo.task.cancel();
			statInfo.task = null;
		}
	}

	/**
	 * Start the analysis job that will check the status of the requests periodically and make thread dumps
	 * if some request is stuck.
	 * @param metricReference
	 */
	public void startAnalysis(final MetricReference metricReference) {
		stopAnalysis(metricReference);
		resetStats(metricReference);
		final TImeMetricInfo statInfo = metricStatisticsMap.get(metricReference);
		
		statInfo.task = new TimerTask() {
			@Override
			public void run() {
				try {
					Long lastDump = statInfo.lastLoggedEventTime;
					
					// if there has been enough time since the last dump proceed with the check
					if(System.currentTimeMillis() - lastDump>statInfo.config.minimumDumpInterval) {
						
						// check all threads for requests that are stuck for too long
						Iterator<Entry<Thread, HashMap<MetricReference, Object>>> threadInfos = threadMap.entrySet().iterator();
						while(threadInfos.hasNext()) {
							Entry<Thread, HashMap<MetricReference, Object>> info = threadInfos.next();
							Long entryTime = (Long) info.getValue().get(metricReference);
							if(!entryTime.equals(Long.MIN_VALUE)) {
								Long delta = System.currentTimeMillis() - entryTime;
								
								// if a thread is stuck for too long log it
								if(logger != null && delta>statInfo.config.stuckTimeBeforeDump) {
									logger.logWarning("Offending thread:\n" + getCurrentStack(info.getKey()));

									StringBuilder sb = new StringBuilder();
									Thread[] threads = new Thread[5000];
									int count = Thread.enumerate(threads);
									for(int q=0; q<count; q++) {
										long threadStuck = 0;
										HashMap<MetricReference,Object> subInfo = threadMap.get(threads[q]);
										if(subInfo != null) {
											Long stamp = (Long) threadMap.get(threads[q]).get(metricReference);
											if(stamp != null) {
												threadStuck = System.currentTimeMillis() - stamp;
											}
											if(stamp != Long.MIN_VALUE) {
												sb.append("->Stuck time:" + threadStuck  + " " + getCurrentStack(threads[q]));
											}
										}
									}
									logger.logWarning(sb.toString());
									threads = null;
									break;
								}
							}
						}
					}
				} catch (Exception ex) {
					//Ignore excpetions here - even concurrent modification exceptions are not critical
				}}
		};
		timer.scheduleAtFixedRate(statInfo.task, statInfo.config.checkingInterval, statInfo.config.checkingInterval);
	}
	
	/**
	 * Stop everything
	 */
	public void stop() {
		timer.cancel();
		timer = null;
	}


	public Long getTime(Thread threadId, MetricReference metricReference) {
		HashMap<MetricReference,Object> attribs = getAttributes(threadId);
		return (Long) attribs.get(metricReference);
	}

	/**
	 * You can associate Objects for a given thread and display them later for more analysis items.
	 * 
	 * @param threadId
	 * @param objectName
	 * @param object
	 */
	public void setObject(Thread threadId, MetricReference objectName, Object object) {
		getAttributes(threadId).put(objectName, object);
	}

	/**
	 * Retrieve items associated with the thread
	 * 
	 * @param threadId
	 * @param objectName
	 * @return
	 */
	public Object getObject(Thread threadId, String objectName) {
		return getAttributes(threadId).get(objectName);
	}

	public synchronized HashMap<MetricReference,Object> getAttributes(Thread threadId) {
		HashMap<MetricReference,Object> threadLocal = threadMap.get(threadId);
		if(threadLocal == null) {
			threadLocal = new HashMap<MetricReference,Object>();
			threadMap.put(threadId, threadLocal);
		}
		return threadLocal;
	}
	
	/**
	 * Enter a traced zone by the name of metricReference for the current thread. This puts the enter timestamp
	 * and all lost call calculations will be based on this timestamp
	 * 
	 * @param threadId
	 * @param metricReference
	 */
	public void enter(MetricReference metricReference) {
		Thread threadId = Thread.currentThread();
		enter(threadId, metricReference);
	}
	
	/**
	 * Leave a traced zone by the name of metricReference for the specified thread. This puts the timestamp in
	 * inactive mode. No more analysis will be done on this thread.
	 * 
	 * @param threadId
	 * @param metricReference
	 */
	public void leave(MetricReference metricReference) {
		Thread threadId = Thread.currentThread();
		leave(threadId, metricReference);
	}

	/**
	 * Enter a traced zone by the name of metricReference for the specified thread. This puts the enter timestamp
	 * and all lost call calculations will be based on this timestamp.
	 * 
	 * @param threadId
	 * @param metricReference
	 */
	public void enter(Thread threadId, MetricReference metricReference) {
		HashMap<MetricReference,Object> attribs = getAttributes(threadId);
		attribs.put(metricReference, System.currentTimeMillis());

	}
	
	/**
	 * Leave a traced zone by the name of metricReference for the specifed thread. No more analysis will be done
	 * on this thread.
	 * 
	 * @param threadId
	 * @param metricReference
	 */
	public void leave(Thread threadId, MetricReference metricReference) {
		TImeMetricInfo info = metricStatisticsMap.get(metricReference);
		HashMap<MetricReference,Object> attribs = getAttributes(threadId);
		long delta = System.currentTimeMillis() - (Long) attribs.get(metricReference);
		info.totalTime += delta;
		info.numberOfEvents ++;
		info.averageTime = info.totalTime/info.numberOfEvents;
		attribs.put(metricReference, Long.MIN_VALUE);
	}

	/**
	 * Current stacktrace of the thread
	 * @param thread
	 * @return
	 */
	public String getCurrentStack(Thread thread) {
		StringBuilder sb = new StringBuilder();
		sb.append("\n" + thread.getName() + " " + thread.getId() + " " + thread.getState().toString() + "\n");
		StackTraceElement[] ste = thread.getStackTrace();
		for( StackTraceElement el : ste ) {
			sb.append(" " + el.toString() + "\n");
		}
		return sb.toString();
	}
	
	/**
	 * Returns the stacktraces of all threads
	 * @return
	 */
	public String getThreadDump() {
		StringBuilder sb = new StringBuilder();
		Thread[] threads = new Thread[5000];
		int count = Thread.enumerate(threads);
		for(int q=0; q<count; q++) {
			sb.append(getCurrentStack(threads[q]));
		}
		return sb.toString();
	}
	
	/**
	 * Number of threads that are executing requests right now or have executed requests and are idle and not
	 * garbage collected. You can check for leaks here.
	 * @return
	 */
	public int getNumberOfThreads() {
		return threadMap.size();
	}

	public static void main(String[] arg) throws InterruptedException {
		ExecutorService ex = Executors.newFixedThreadPool(1000);
		final CallAnalyzer tp = new CallAnalyzer();
		final MetricReference sec = new MetricReference("sec");
		MetricReference se1c = new MetricReference("se111c");
		tp.configure(sec, new MetricAnalysisConfiguration(500,500,500));
		tp.startAnalysis(sec);
		tp.startAnalysis(se1c);
		Runnable r = new Runnable() {
			
			public void run() {
				tp.enter(sec);
				try {
					if(++count % 10000==0) {
						System.out.println("Avg " + tp.getMetricStats(sec).averageTime);
						Thread.sleep(1000);
					}
					
					Thread.sleep(100);
					
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				tp.leave(sec);
			}
		};
		for(int q=0; q<2000000; q++) {
			ex.execute(r);
		}
		
		System.out.println("size:" + tp.threadMap.size() + " " + tp.metricStatisticsMap.size());
		//Thread.sleep(5000);
		ex.shutdown();
		ex.awaitTermination(200, TimeUnit.SECONDS);
		ex.shutdownNow();
		System.gc();
		System.out.println("size:" + tp.threadMap.size() + " " + tp.metricStatisticsMap.size());
		System.gc();Thread.sleep(5000);
		System.gc();
		System.out.println("size:" + tp.threadMap.size() + " " + tp.metricStatisticsMap.size());
		System.gc();
		se1c = null;
		System.gc();Thread.sleep(5000);
		System.out.println("size:" + tp.threadMap.size() + " " + tp.metricStatisticsMap.size());
		System.gc();
		System.gc();Thread.sleep(5000);
		System.out.println("size:" + tp.threadMap.size() + " " + tp.metricStatisticsMap.size());
		System.gc();Thread.sleep(5000);
		System.gc();
		System.out.println("size:" + tp.threadMap.size() + " " + tp.metricStatisticsMap.size());
		System.gc();
		System.gc();Thread.sleep(5000);
		System.out.println("size:" + tp.threadMap.size() + " " + tp.metricStatisticsMap.size());
		System.gc();
		System.gc();Thread.sleep(5000);
		System.out.println("size:" + tp.threadMap.size() + " " + tp.metricStatisticsMap.size());
		System.gc();
		if(tp.threadMap.size() >0) {
			throw new RuntimeException("Should be zero by this point. Leak.");
		}
		
	}
	static int count =0;

}