package thread_dispatcher;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ThreadMonitor extends ThreadedTask {
    final Object lock;
    boolean changed;
    private final int sleepTime;
    private final Path fileToWrite;
    private final Set<ThreadedTask> tasks;

    public ThreadMonitor(int sleepTime, String root, Set<ThreadedTask> tasks){
        this.tasks = tasks;
        this.lock = new Object();
        this.changed = false;
        this.sleepTime = sleepTime;
        this.fileToWrite = new File(root).toPath().resolve("active_threads.txt");
    }

    @Override
    public void run() {
        while (true) {
            synchronized (lock) {
                synchronized ((Object) changed) {
                    if (changed) {
                        writeActive();
                        changed = false;
                    }
                }
            }
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                return;
            }
        }
    }

    private void writeActive(){
        List<String> linesToWrite = new ArrayList<>();
        System.out.println("Threads:");
        for (ThreadedTask task : tasks) {
            linesToWrite.add(task.toString());
            System.out.println("\t" + task.toString());
        }
        try {
            Files.write(fileToWrite, linesToWrite, Charset.forName("UTF-8"));
        } catch (IOException e) {
            return;
        }
    }
}
