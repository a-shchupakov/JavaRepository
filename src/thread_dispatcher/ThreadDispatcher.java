package thread_dispatcher;

import java.util.HashSet;
import java.util.Set;

public class ThreadDispatcher {
    private static ThreadDispatcher ourInstance = new ThreadDispatcher();

    public static ThreadDispatcher getInstance() {
        return ourInstance;
    }

    private ThreadMonitor threadMonitor;
    private final Set<ThreadedTask> tasks;

    private ThreadDispatcher() {
        this.tasks = new HashSet<>();
        this.threadMonitor = new ThreadMonitor(50, "D:\\IT\\ООП\\практика\\Tests\\testDispatcher", tasks);
        add(threadMonitor);
    }

    public void add(ThreadedTask task){
        synchronized (threadMonitor.lock) {
            synchronized ((Object) threadMonitor.changed) {
                this.tasks.add(task);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        task.run();
                        getInstance().threadStopped(task);
                    }
                }).start();
                threadMonitor.changed = true;
            }
        }
    }

    private void threadStopped(ThreadedTask task){
        synchronized (threadMonitor.lock) {
            synchronized ((Object) threadMonitor.changed) {
                this.tasks.remove(task);
                threadMonitor.changed = true;
            }
        }
    }
}
