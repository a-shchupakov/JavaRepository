package thread_dispatcher;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class ThreadMonitor extends ThreadedTask {
    private final int sleepTime;
    private final Path fileToWrite;
    private final Set<ThreadedTask> tasks;

    ThreadMonitor(int sleepTime, String root, Set<ThreadedTask> tasks){
        this.tasks = tasks;
        this.sleepTime = sleepTime;
        this.fileToWrite = new File(root).toPath().resolve("active_threads.txt");
    }

    @Override
    public void run() {
        while (true) {
            List<String> active;
            synchronized (tasks) {
                active = getActive();
            }
            writeActive(active);
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                return;
            }
        }
    }

    private List<String> getActive(){
        List<String> active = new ArrayList<>();
        for (ThreadedTask task: tasks)
            active.add(task.toString());
        return active;
    }

    private void writeActive(List<String> activeThreads){
        System.out.println("Threads:");
        for (String task: activeThreads) {
            System.out.println("\t" + task);
        }
        try {
            Files.write(fileToWrite, activeThreads, Charset.forName("UTF-8"));
        } catch (IOException e) {
            return;
        }
    }
}
