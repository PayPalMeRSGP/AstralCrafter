import Task.CraftAstralTask;
import Task.POHDetourTask;
import Task.PrioritizedReactiveTask;
import Task.RestockEssenceTask;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;

import java.util.*;

@ScriptManifest(author = "PayPalMeRSGP", name = MainScript.SCRIPT_NAME, info = "Astral Runecrafting", version = 0.5, logo = "")
public class MainScript extends Script {

    private long mainThreadID = Thread.currentThread().getId();
    static final String SCRIPT_NAME = "Astral_Crafter v0.16";
    private Queue<PrioritizedReactiveTask> taskQ;

    @Override
    public void onStart() throws InterruptedException {
        super.onStart();
        taskQ = PrioritizedReactiveTask.initializeTaskQueue();

        new CraftAstralTask(bot).startTaskTriggerCheckerThread();
        new RestockEssenceTask(bot).startTaskTriggerCheckerThread();
        new POHDetourTask(bot).startTaskTriggerCheckerThread();
    }

    @Override
    public int onLoop() {
        if(!taskQ.isEmpty()) {
            PrioritizedReactiveTask currentTask = taskQ.poll();
            currentTask.setTaskEnqueuedToFalse();
            currentTask.startTaskRunnerThread();

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

    @Override
    public void pause() {
        super.pause();
        PrioritizedReactiveTask.onPauseCleanUp();
    }

    @Override
    public void onExit() throws InterruptedException {
        super.onExit();
        PrioritizedReactiveTask.onStopCleanUp();
    }


}
