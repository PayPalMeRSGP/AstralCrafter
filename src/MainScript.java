import Task.CraftAstralTask;
import Task.PrioritizedReactiveTask;
import Task.RestockEssenceTask;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;

import java.util.Queue;

@ScriptManifest(author = "PayPalMeRSGP", name = MainScript.SCRIPT_NAME, info = "Astral Runecrafting", version = 0.5, logo = "")
public class MainScript extends Script {
    private long mainThreadID = Thread.currentThread().getId();
    static final String SCRIPT_NAME = "Astral_Crafter v0.02";
    private Queue<PrioritizedReactiveTask> taskQ;

    @Override
    public void onStart() throws InterruptedException {
        super.onStart();
        taskQ = PrioritizedReactiveTask.initializeTaskQueue();

        new CraftAstralTask(bot).startTaskTriggerCheckerThread();
        new RestockEssenceTask(bot).startTaskTriggerCheckerThread();
    }

    @Override
    public int onLoop() throws InterruptedException {
        if(!taskQ.isEmpty()) {
            PrioritizedReactiveTask currentTask = taskQ.poll();
            currentTask.setTaskEnqueuedToFalse();
            currentTask.startTaskRunnerThread();
            log("Thread " + mainThreadID + " awaiting task " + currentTask.getClass().getSimpleName() + " to finish.");
            while(currentTask.isTaskRunning()) {
                PrioritizedReactiveTask peeked = taskQ.peek();
                //interrupt the current task if one of greater priority is enqueued
                if(peeked != null && peeked.getPriority().getValue() > currentTask.getPriority().getValue()) {
                    currentTask.stopTask();
                }
            }
            // log("Finished task: " + currentTask.getClass().getSimpleName());
        }

        return 1000;
    }
}
