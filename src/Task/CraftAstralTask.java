package Task;

import org.osbot.rs07.Bot;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.model.RS2Object;
import org.osbot.rs07.event.WebWalkEvent;
import org.osbot.rs07.utility.ConditionalSleep;

public class CraftAstralTask extends PrioritizedReactiveTask {

    public CraftAstralTask(Bot bot) {
        super(bot);
        this.priority = Priority.NORMAL;
    }

    @Override
    public void task() throws InterruptedException {
        Area ASTRAL_ALTER = new Area(2152, 3867, 2156, 3859);
        WebWalkEvent astralAltarWalkEvent = new WebWalkEvent(ASTRAL_ALTER);
        astralAltarWalkEvent.setMoveCameraDuringWalking(false);
        execute(astralAltarWalkEvent);

        if(astralAltarWalkEvent.hasFinished()) {
            RS2Object astralAltar = objects.closest(new Area(2155, 3867, 2161, 3861), "Altar");
            if(astralAltar != null && astralAltar.exists()) {
                if(astralAltar.interact("Craft-rune")) {
                    new ConditionalSleep(5000) {
                        @Override
                        public boolean condition() throws InterruptedException {
                            return !inventory.contains("Pure essence");
                        }
                    }.sleep();
                }
            }
        }
    }

    @Override
    boolean shouldTaskActivate() {
        return inventory.contains("Pure essence");
    }

}
