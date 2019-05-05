package Task;

import org.osbot.rs07.Bot;
import org.osbot.rs07.api.model.RS2Object;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.event.InteractionEvent;
import org.osbot.rs07.utility.ConditionalSleep;

public class POHDetourTask extends ReactiveTask {

    public POHDetourTask(Bot bot) {
        super(bot);
    }

    @Override
    public void task() throws InterruptedException {
        if (objects.closest("Portal") != null
                || inventory.contains("Teleport to house")
                && inventory.interact("break", "Teleport to house")) {
            if(usePOHPool()) {
                settings.setRunning(true);
                log("restored stats with pool");
                if(useLunarPortal()) {
                    log("used lunar portal");
                    boolean atLunarIsland = new ConditionalSleep(15000, 500) {
                        @Override
                        public boolean condition() {
                            return npcs.closest("Rimae Sirsalis") != null;
                        }
                    }.sleep();
                    if(atLunarIsland) {
                        log("POH detour successful");
                        return;
                    }
                }
            }
            log("POH detour unsuccessful");

        } else {
            log("Stop Condition -> no house teleports");
            bot.getScriptExecutor().stop(false);
        }
    }

    private boolean usePOHPool() {
        if(settings.getRunEnergy() > 90 && skills.getDynamic(Skill.HITPOINTS) > 50) {
            return true; //if this task runs again and player is healthy, allow this interaction to be skipped
        }
        final RS2Object[] pohPool = new RS2Object[1];
        boolean poolFound = new ConditionalSleep(8000, 500) {
            @Override
            public boolean condition() {
                pohPool[0] = objects.closest("Ornate rejuvenation pool",
                        "Fancy rejuvenation pool",
                        "Rejuvenation pool",
                        "Revitalisation pool");
                return pohPool[0] != null;
            }
        }.sleep();

        if (poolFound) {
            InteractionEvent poolInteraction = new InteractionEvent(pohPool[0]);
            poolInteraction.setOperateCamera(false);
            execute(poolInteraction);
            boolean restored = new ConditionalSleep(5000) {
                @Override
                public boolean condition() {
                    return settings.getRunEnergy() >= 99;
                }
            }.sleep();
            return restored || pohPool[0].interact();
        }
        log("WARN: unable to find pool, will retry soon.");
        return false;
    }

    private boolean useLunarPortal() {
        final RS2Object[] lunarPortal = new RS2Object[1];
        boolean portalFound = new ConditionalSleep(5000) {
            @Override
            public boolean condition() {
                lunarPortal[0] = objects.closest("Lunar Isle Portal");
                return lunarPortal[0] != null;
            }
        }.sleep();

        if(portalFound) {
            InteractionEvent portalInteraction = new InteractionEvent(lunarPortal[0]);
            portalInteraction.setOperateCamera(false);
            execute(portalInteraction);
            return portalInteraction.hasFinished() || lunarPortal[0].interact();
        }
        log("WARN: unable to use portal, will retry soon.");
        return false;
    }

    @Override
    boolean shouldEnqueue() throws InterruptedException {
        Thread.sleep(ENQUEUE_POLLING_INTERVAL_MS);
        if(!inventory.contains("Teleport to house")) {
            this.canRunEnqueueTaskThread.set(false);
            return false;
        }
        return !inventory.contains("Pure essence") && settings.getRunEnergy() < 20;
    }

    @Override
    public String getClassName() {
        return "POHDetourTask";
    }
}
