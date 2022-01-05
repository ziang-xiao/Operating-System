package nachos.threads;

import nachos.machine.*;
import java.util.*;

/**
 * Uses the hardware timer to provide preemption, and to allow threads to sleep
 * until a certain time.
 */
public class Alarm {
	/**
	 * Allocate a new Alarm. Set the machine's timer interrupt handler to this
	 * alarm's callback.
	 * 
	 * <p>
	 * <b>Note</b>: Nachos will not function correctly with more than one alarm.
	 */

	public static Queue<WaitingThread> q = new PriorityQueue<>();

	public Alarm() {
		Machine.timer().setInterruptHandler(new Runnable() {
			public void run() {
				timerInterrupt();
			}
		});
	}

	/**
	 * The timer interrupt handler. This is called by the machine's timer
	 * periodically (approximately every 500 clock ticks). Causes the current
	 * thread to yield, forcing a context switch if there is another thread that
	 * should be run.
	 */
	public void timerInterrupt() {
		/**
		 * pop all the threads that should be waken (set status to ready) in the priority queue 
		 * and push them into the readyqueue.
		 */

		boolean intStatus = Machine.interrupt().disable();
		while(this.q.size() > 0 && this.q.peek().wakeTime <= Machine.timer().getTime()) {
			WaitingThread wt = this.q.poll();
			wt.t.ready();
		}
		Machine.interrupt().restore(intStatus);
		KThread.currentThread().yield();
	}

	/**
	 * Put the current thread to sleep for at least <i>x</i> ticks, waking it up
	 * in the timer interrupt handler. The thread must be woken up (placed in
	 * the scheduler ready set) during the first timer interrupt where
	 * 
	 * <p>
	 * <blockquote> (current time) >= (WaitUntil called time)+(x) </blockquote>
	 * 
	 * @param x the minimum number of clock ticks to wait.
	 * 
	 * @see nachos.machine.Timer#getTime()
	 */
	public void waitUntil(long x) {
		// for now, cheat just to get something working (busy waiting is bad)
		if(x <= 0) {
			return;
		}

		//copied from KThread.java fork(), otherwise KThread.sleep() will throw error on assert
		boolean intStatus = Machine.interrupt().disable(); 

		long wakeTime = Machine.timer().getTime() + x;
		// refer to the static variable q in Alarm, a priority queue sorted by wakeTime
		q.add(new WaitingThread(KThread.currentThread(), wakeTime, null));

		// Put the current thread to sleep and let Alarm.timerInterrupt() handle the waking (in another work, set to ready)
		KThread.currentThread().sleep();

		Machine.interrupt().restore(intStatus);
	}


        /**
	 * Cancel any timer set by <i>thread</i>, effectively waking
	 * up the thread immediately (placing it in the scheduler
	 * ready set) and returning true.  If <i>thread</i> has no
	 * timer set, return false.
	 * 
	 * <p>
	 * @param thread the thread whose timer should be cancelled.
	 */
    public boolean cancel(KThread thread) {
		boolean intStatus = Machine.interrupt().disable();	
		boolean found = false;
	
		// Lib.assertTrue(Machine.interrupt().disabled());
		for(WaitingThread wt:this.q) {
			if(wt.t == thread) {
				found = true;
				this.q.remove(wt);
				thread.ready();
				break;
			}
		}
		Machine.interrupt().restore(intStatus);
		return found;
	}

	public class WaitingThread implements Comparable<WaitingThread>{
		public KThread t;
		public long wakeTime;

		public WaitingThread(KThread t, long wakeTime, Condition2 cv) {
			this.t = t;
			this.wakeTime = wakeTime;
		}

		@Override
		public int compareTo(WaitingThread wt) {
			if(wt.wakeTime > this.wakeTime) {
				return -1;
			}
			else if(wt.wakeTime == this.wakeTime) {
				return 0;
			}
			else {
				return 1;
			}
		}
	}

	public static void alarmTest1() {
		int durations[] = {-1, 0, 1, 1000, 10*1000, 100*1000};
		long t0, t1;

		for (int d : durations) {
			t0 = Machine.timer().getTime();
			ThreadedKernel.alarm.waitUntil (d);
			t1 = Machine.timer().getTime();
			System.out.println ("alarmTest1: waited for " + (t1 - t0) + " ticks");
		}
    }

}

