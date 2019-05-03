package Task;

import org.osbot.rs07.Bot;
import org.osbot.rs07.script.MethodProvider;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class ReactiveTask extends MethodProvider {

    private static Queue<ReactiveTask> taskQueue;
    private static Set<ReactiveTask> activeTasks; //used to store all initialized threads to later kill all threads.
    private volatile AtomicBoolean taskEnqueueLock = new AtomicBoolean(false);
    volatile AtomicBoolean canRunEnqueueTaskThread = new AtomicBoolean(false); //flag used to stop the thread that checks whether the task should be enqueued.

    int ENQUEUE_POLLING_INTERVAL_MS;

    ReactiveTask(Bot bot, int enqueuePollInterval) {
        ENQUEUE_POLLING_INTERVAL_MS = enqueuePollInterval;
        exchangeContext(bot);
    }

    ReactiveTask(Bot bot) {
        exchangeContext(bot);
    }

    public static Queue<ReactiveTask> initializeTaskQueue() {
        if(taskQueue == null) {
            taskQueue = new LinkedList<>();
            activeTasks = new HashSet<>();
        }
        return taskQueue;
    }

    //stop all threads
    public static void onStopCleanUp() {
        for(ReactiveTask task: activeTasks) {
            if(task.canRunEnqueueTaskThread != null)
                task.canRunEnqueueTaskThread.set(false);
        }
        taskQueue = null;
        activeTasks = null;
    }

    /**
     * Starts a thread that continuously checks whether its relevant task should execute.
     * If the task should execute, enqueues it into the taskQ.
     * ex: The DropTask enqueueing thread checks if the inventory is full of fish
     */
    public void startEnqueueTaskThread() {
        new Thread(() -> {
            canRunEnqueueTaskThread.set(true);
            while(canRunEnqueueTaskThread.get()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                try {
                    if(shouldEnqueue() && !taskEnqueueLock.get()) {
                        log("Thread " + Thread.currentThread().getId() + " enqueued task: " + getClassName());
                        taskQueue.add(ReactiveTask.this);
                        activeTasks.add(ReactiveTask.this);
                        taskEnqueueLock.set(true);
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * Subclasses implement the task directions to execute
     * Inorder to have immediate task interruption on enqueueing of higher priority task
     * each step ought to check for taskIsRunning==true and return if false
     */
    public abstract void task() throws InterruptedException;

    /**
     * Subclasses implement when the task should execute
     * @return should the task be enqueued, used by startEnqueueTaskThread()
     */
    abstract boolean shouldEnqueue() throws InterruptedException;

    long randomNormalDist(double mean, double stddev){
        return (long) Math.abs((new Random().nextGaussian() * stddev + mean));
    }

    public void liftTaskEnqueueLock() {
        this.taskEnqueueLock.set(false);
    }

    public abstract String getClassName();
}