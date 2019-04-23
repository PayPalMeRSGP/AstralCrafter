package Task;

import org.osbot.rs07.Bot;
import org.osbot.rs07.api.model.RS2Object;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.event.InteractionEvent;
import org.osbot.rs07.utility.ConditionalSleep;

public class POHDetourTask extends PrioritizedReactiveTask {

    public POHDetourTask(Bot bot) {
        super(bot);
        this.priority = Priority.NORMAL;
    }

    @Override
    public void task() throws InterruptedException {
        if (inventory.contains("Teleport to house")) {
            if(inventory.interact("break", "Teleport to house")) {
                if(usePOHPool() && useLunarPortal()) {
                    log("Completed POH detour successful");
                } else {
                    log("POH detour unsuccessful");
                }
            }
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
        boolean poolFound = new ConditionalSleep(5000) {
            @Override
            public boolean condition() throws InterruptedException {
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
            boolean restored = new ConditionalSleep(2000) {
                @Override
                public boolean condition() throws InterruptedException {
                    return false;
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
            public boolean condition() throws InterruptedException {
                lunarPortal[0] = objects.closest("???"); //TODO: set
                return lunarPortal[0] != null;
            }
        }.sleep();

        if(portalFound) {
            InteractionEvent poolInteraction = new InteractionEvent(lunarPortal[0]);
            poolInteraction.setOperateCamera(false);
            execute(poolInteraction);
            return poolInteraction.hasFinished() || lunarPortal[0].interact();
        }
        log("WARN: unable to use portal, will retry soon.");
        return false;
    }

    @Override
    boolean shouldTaskActivate() {
        return !inventory.contains("Pure essence") && settings.getRunEnergy() < 20;
    }

    @Override
    String getClassName() {
        return null;
    }
}
