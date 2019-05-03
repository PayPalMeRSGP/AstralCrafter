import Task.*;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;

import java.util.*;

@ScriptManifest(author = "PayPalMeRSGP", name = MainScript.SCRIPT_NAME, info = "Astral Runecrafting", version = 0.5, logo = "")
public class MainScript extends Script {
    static final String SCRIPT_NAME = "Astral_Crafter v0.19";
    private Queue<ReactiveTask> taskQ;

    @Override
    public void onStart() throws InterruptedException {
        super.onStart();
        taskQ = ReactiveTask.initializeTaskQueue();

        new RepairPouchesTask(bot).startEnqueueTaskThread();
        new CraftAstralTask(bot).startEnqueueTaskThread();
        new RestockEssenceTask(bot).startEnqueueTaskThread();
        new POHDetourTask(bot).startEnqueueTaskThread();

    }

    @Override
    public int onLoop() throws InterruptedException {
        if(!taskQ.isEmpty()) {
            ReactiveTask currentTask = taskQ.poll();
            log("Running: " + currentTask.getClassName());
            currentTask.task();
            currentTask.liftTaskEnqueueLock();
        }
        return 500;
    }

    @Override
    public void onExit() throws InterruptedException {
        super.onExit();
        ReactiveTask.onStopCleanUp();
    }


}
