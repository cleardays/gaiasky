package gaia.cu9.ari.gaiaorbit.util.concurrent;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The default thread factory
 */
public class GaiaSandboxThreadFactory implements ThreadFactory {
    private static final AtomicInteger poolNumber = new AtomicInteger(1);
    private final ThreadGroup group;
    private final AtomicInteger threadNumber = new AtomicInteger(0);
    private final String namePrefix;

    public GaiaSandboxThreadFactory(String threadNamePrefix) {
	SecurityManager s = System.getSecurityManager();
	group = (s != null) ? s.getThreadGroup() :
		Thread.currentThread().getThreadGroup();
	namePrefix = threadNamePrefix +
		poolNumber.getAndIncrement();
    }

    public Thread newThread(Runnable r) {
	Thread t = new GSThread(group, r,
		namePrefix + threadNumber.get(), threadNumber.getAndIncrement());
	if (t.isDaemon())
	    t.setDaemon(false);
	if (t.getPriority() != Thread.NORM_PRIORITY)
	    t.setPriority(Thread.NORM_PRIORITY);
	return t;
    }

    public class GSThread extends Thread {
	public int index;

	public GSThread(ThreadGroup group, Runnable r, String name, int index) {
	    super(group, r, name);
	    this.index = index;
	}
    }
}