package Task;

import org.osbot.rs07.Bot;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.model.RS2Object;
import org.osbot.rs07.event.WebWalkEvent;
import org.osbot.rs07.utility.Condition;
import org.osbot.rs07.utility.ConditionalSleep;

import java.util.Arrays;

public class CraftAstralTask extends PrioritizedReactiveTask {

    public CraftAstralTask(Bot bot) {
        super(bot);
        this.priority = Priority.NORMAL;
    }

    @Override
    public void task() throws InterruptedException {
        final RS2Object[] astralAltar = new RS2Object[1];
        final int[] distanceToAltar = {-1};
        Area ALTAR_AREA = new Area(2152, 3867, 2156, 3859);
        WebWalkEvent astralAltarWalkEvent = new WebWalkEvent(ALTAR_AREA);
        astralAltarWalkEvent.setMoveCameraDuringWalking(false);
        astralAltarWalkEvent.setBreakCondition(new Condition() {
            @Override
            public boolean evaluate() {
                astralAltar[0] = objects.closest(new Area(2155, 3867, 2161, 3861), "Altar");
                if(astralAltar[0] != null) {
                    distanceToAltar[0] = map.distance(astralAltar[0]);
                    return distanceToAltar[0] < 10;
                }
                return false;
            }
        });
        execute(astralAltarWalkEvent);

        if(astralAltarWalkEvent.hasFinished() || (distanceToAltar[0] > -1 && distanceToAltar[0] < 10)) {
            if(astralAltar[0] == null) {
                astralAltar[0] = objects.closest(new Area(2155, 3867, 2161, 3861), "Altar");
            }

            if(astralAltar[0] != null) {
                if(astralAltar[0].interact("Craft-rune")) {
                    new ConditionalSleep(5000) {
                        @Override
                        public boolean condition() throws InterruptedException {
                            return !inventory.contains("Pure essence") && myPlayer().getAnimation() != 791;
                        }
                    }.sleep();
                } else {
                    log("cannot interact");
                }
            }
        }
    }

    @Override
    boolean shouldTaskActivate() {
        return inventory.contains("Pure essence");
    }

    @Override
    String getClassName() {
        return "CraftAstralTask";
    }
}
