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

import gov.nist.core.NamingThreadFactory;

import java.util.Enumeration;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

public class MultiPipelineExecutor<K> {
	private ExecutorService executor;
	private ConcurrentHashMap<K, SemaphoreLinkedList<SemaphoreRunnable<K>>> map =
			new ConcurrentHashMap<K, SemaphoreLinkedList<SemaphoreRunnable<K>>>();
	public MultiPipelineExecutor(int threads) {
		executor = Executors.newFixedThreadPool(threads, new NamingThreadFactory("jain_sip_multi_pipeline_executor"));
	}
	public synchronized void addTask(K key, Runnable task) {
		SemaphoreLinkedList<SemaphoreRunnable<K>> list = map.get(key);
		if(list == null) {
			list = new SemaphoreLinkedList<SemaphoreRunnable<K>>();
			map.put(key, list);
		}
		list.addFirst(new SemaphoreRunnable<K>(task, list.semaphore, this));
		this.notifyAll();
	}
	
	public synchronized void processTasks() {
		try {
			this.wait();
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		Enumeration<K> keys = map.keys();
		K key;
		while (keys.hasMoreElements()) {
			key = keys.nextElement();
			SemaphoreLinkedList<SemaphoreRunnable<K>> e = map.get(key);
			if(!e.isEmpty() && e.semaphore.tryAcquire()){
				Runnable task = e.pollLast();
				executor.execute(task);
			}
			//if(e.isEmpty()) map.remove(key); //CME, fix it
		}
	}
	
	public void remove(K key) {
		map.remove(key);
	}
	
	public static class SemaphoreLinkedList<A> extends LinkedList<A> {
		private static final long serialVersionUID = 1L;
		Semaphore semaphore = new Semaphore(1);
	}
	
	public static class SemaphoreRunnable<K> implements Runnable {

		protected Runnable wrappedTask;
		protected Semaphore semaphore;
		protected MultiPipelineExecutor<K> parent;
		public SemaphoreRunnable(Runnable task, Semaphore semaphore, MultiPipelineExecutor<K> parent) {
			this.wrappedTask = task;
			this.semaphore = semaphore;
			this.parent = parent;
		}
		
		public void run() {
			try {
				wrappedTask.run();
			} finally {
				semaphore.release();
				parent.notifyAll();
			}
		}
		
	}
}
