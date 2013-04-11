package efruchter.tp.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An easy way to queue up ordered jobs. Don't foregt to start it.
 * 
 * @author toriscope
 * 
 */
public class JobQueue {

    private final List<Job> jobs;
    private final long wait;
    private final boolean doOldestJobFirst;
    private boolean keepRunning = false;

    private final Thread thread = new Thread() {
        public void run() {
            while (keepRunning) {
                while (!jobs.isEmpty()) {
                    (doOldestJobFirst ? jobs.remove(0) : jobs.remove(jobs.size() - 1)).action();
                }
                try {
                    Thread.sleep(wait);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    /**
     * Create a job queue that executes "jobs" in a particular order, with a
     * particular delay.
     * 
     * @param doOldestJobFirst
     *            do the oldest job in the queue first.
     */
    public JobQueue(final boolean doOldestJobFirst) {
        this(500, doOldestJobFirst);
    }

    /**
     * Create a job queue that executes "jobs" in a particular order, with a
     * particular delay.
     * 
     * @param milliWait
     *            millisecond wait at leats this long before polling for new
     *            jobs.
     * @param doOldestJobFirst
     *            do the oldest job in the queue first.
     */
    public JobQueue(final long milliWait, final boolean doOldestJobFirst) {
        this.wait = milliWait;
        this.doOldestJobFirst = doOldestJobFirst;
        jobs = Collections.synchronizedList(new ArrayList<Job>());
    }

    /**
     * Start the jobbing. Don't ever do this more than once.
     */
    public synchronized void start() {
        keepRunning = true;
        thread.start();
    }

    public synchronized void stop() {
        keepRunning = false;
    }

    /**
     * Add a job to ber performed when there is an opportunity.
     * 
     * @param job
     */
    public void addJob(final Job job) {
        jobs.add(job);
    }

    /**
     * A job to perform.
     * 
     * @author toriscope
     * 
     */
    public static interface Job {
        void action();
    }
}
