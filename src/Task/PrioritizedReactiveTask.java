package Task;

import org.osbot.rs07.Bot;
import org.osbot.rs07.script.MethodProvider;

import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class PrioritizedReactiveTask extends MethodProvider implements Comparable<PrioritizedReactiveTask> {

    public enum Priority {
        HIGH(1), NORMAL(0);
        private final int value;
        Priority(final int newValue) {
            value = newValue;
        }
        public int getValue() { return value; }
    }

    Priority priority; //allows for task with higher priority upon enqueueing to immediately interrupt presenting executing tasks of lower priority
    private static PriorityQueue<PrioritizedReactiveTask> taskQueue;
    private static Set<PrioritizedReactiveTask> activeTasks; //used to store all initialized threads to later kill all threads. ex: onStop()

    private volatile AtomicBoolean taskEnqueued = new AtomicBoolean(false); //Task instances should be singleton in the PQ. ex: Only 1 instance of FishingTask can be in the PQ at any time.
    private volatile AtomicBoolean runEnqueueTaskThread = new AtomicBoolean(false); //flag used to stop the thread that checks whether the task should be enqueued.
    volatile AtomicBoolean runTaskThread = new AtomicBoolean(false); //onloop sets this to false thread if it wants to stop execution of the task. This is usually followed by a switch to a higher priority task

    PrioritizedReactiveTask(Bot bot) {
        exchangeContext(bot);
    }

    public static PriorityQueue<PrioritizedReactiveTask> initializeTaskQueue() {
        if(taskQueue == null) {
            taskQueue = new PriorityQueue<>();
            activeTasks = new HashSet<>();
        }
        return taskQueue;
    }

    //stop all threads
    public static void onStopCleanUp() {
        for(PrioritizedReactiveTask task: activeTasks) {
            task.stopTask();
            if(task.runEnqueueTaskThread != null)
                task.runEnqueueTaskThread.set(false);
        }
        taskQueue = null;
        activeTasks = null;
    }

    //stop all task threads but not the task activation and encasement threads
    public static void onPauseCleanUp() {
        for(PrioritizedReactiveTask task: activeTasks) {
            task.stopTask();
        }
    }

    /**
     * Starts a thread that continuously checks whether its relevant task should execute.
     * If the task should execute, enqueues it into the taskQ.
     * ex: The DropTask enqueueing thread checks if the inventory is full of fish
     */
    public void startTaskTriggerCheckerThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                runEnqueueTaskThread.set(true);
                while(runEnqueueTaskThread.get()) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    if(taskEnqueued.get() || runTaskThread.get()) { //do not enqueue the task if said task is already enqueued or is already running.
                        continue;
                        //log(PrioritizedReactiveTask.this.getClass().getSimpleName() + " already in queue or is currently executing");
                    } else if(shouldTaskActivate()) {
                        System.out.println("Thread " + Thread.currentThread().getId() + " enqueued task: " + this.getClass().getSimpleName());
                        taskQueue.add(PrioritizedReactiveTask.this);
                        activeTasks.add(PrioritizedReactiveTask.this);
                        taskEnqueued.set(true);
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }).start();
    }

    /**
     * Starts the thread that executes the task
     */
    public void startTaskRunnerThread() {
        runTaskThread.set(true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    task();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    runTaskThread.set(false);
                }
            }
        }).start();
    }

    /**
     * Stops the above ^^^ Thread
     */
    public void stopTask() {
        if(runTaskThread != null)
            runTaskThread.set(false);
    }

    /**
     * Subclasses implement the task directions to execute
     * Inorder to have immediate task interruption on enqueueing of higher priority task
     * each step ought to check for runTaskThread==true and return if false
     */
    abstract void task() throws InterruptedException;

    /**
     * Subclasses implement when the task should execute
     * @return should the task be enqueued, used by startTaskTriggerCheckerThread()
     */
    abstract boolean shouldTaskActivate();

    @Override
    public int compareTo(PrioritizedReactiveTask other) {
        return other.priority.getValue() - this.priority.getValue();
    }

    long randomNormalDist(double mean, double stddev){
        return (long) Math.abs((new Random().nextGaussian() * stddev + mean));
    }

    public boolean isTaskRunning() {
        return runTaskThread.get();
    }

    public Priority getPriority() {
        return priority;
    }

    public void setTaskEnqueuedToFalse() {
        this.taskEnqueued.set(false);
    }
}