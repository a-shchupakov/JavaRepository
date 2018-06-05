package thread_dispatcher;

import java.util.HashSet;
import java.util.Set;

public class ThreadDispatcher {
    private static ThreadDispatcher ourInstance = new ThreadDispatcher();

    public static ThreadDispatcher getInstance() {
        return ourInstance;
    }

    private final Set<ThreadedTask> tasks;

    private ThreadDispatcher() {
        this.tasks = new HashSet<>();
        add(new ThreadMonitor(50, "D:\\IT\\ООП\\практика\\Tests\\testDispatcher", tasks));
    }

    public void add(ThreadedTask task){
        synchronized (tasks) {
            this.tasks.add(task);
            new Thread(() -> {
                task.run();
                getInstance().threadStopped(task);
            }).start();
        }
    }

    private void threadStopped(ThreadedTask task){
        synchronized (tasks) {
            this.tasks.remove(task);
        }
    }
}
