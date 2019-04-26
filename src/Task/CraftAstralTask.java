package Task;

import org.osbot.rs07.Bot;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.model.RS2Object;
import org.osbot.rs07.api.ui.Spells;
import org.osbot.rs07.event.WalkingEvent;
import org.osbot.rs07.event.WebWalkEvent;
import org.osbot.rs07.utility.Condition;
import org.osbot.rs07.utility.ConditionalSleep;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.concurrent.ThreadLocalRandom;

public class CraftAstralTask extends PrioritizedReactiveTask {

    private static final LinkedList<Position> PATH1 = new LinkedList<>(Arrays.asList(
            new Position(2104, 3915, 0),
            new Position(2110, 3915, 0),
            new Position(2115, 3910, 0),
            new Position(2115, 3904, 0),
            new Position(2115, 3898, 0),
            new Position(2113, 3892, 0),
            new Position(2114, 3886, 0),
            new Position(2116, 3880, 0),
            new Position(2119, 3874, 0),
            new Position(2126, 3870, 0),
            new Position(2133, 3867, 0),
            new Position(2135, 3861, 0),
            new Position(2142, 3863, 0),
            new Position(2149, 3863, 0),
            new Position(2156, 3862, 0)
    ));

    private static final LinkedList<Position> PATH2 = new LinkedList<>(Arrays.asList(
            new Position(2102, 3915, 0),
            new Position(2109, 3915, 0),
            new Position(2112, 3905, 0),
            new Position(2112, 3897, 0),
            new Position(2114, 3889, 0),
            new Position(2118, 3883, 0),
            new Position(2123, 3878, 0),
            new Position(2128, 3873, 0),
            new Position(2134, 3868, 0),
            new Position(2135, 3862, 0),
            new Position(2140, 3860, 0),
            new Position(2147, 3862, 0),
            new Position(2153, 3863, 0)
    ));

    public CraftAstralTask(Bot bot) {
        super(bot);
        this.priority = Priority.NORMAL;
    }

    @Override
    public void task() throws InterruptedException {
        final RS2Object[] astralAltar = new RS2Object[1];
        WalkingEvent astralAltarWalkEvent = new WalkingEvent();
        if(ThreadLocalRandom.current().nextBoolean()) {
            astralAltarWalkEvent.setPath(PATH1);
        } else {
            astralAltarWalkEvent.setPath(PATH2);
        }

        astralAltarWalkEvent.setMinDistanceThreshold(5);
        execute(astralAltarWalkEvent);

        if(astralAltarWalkEvent.hasFinished()) {
            if(astralAltar[0] == null) {
                astralAltar[0] = objects.closest(new Area(2155, 3867, 2161, 3861), "Altar");
            }

            if(astralAltar[0] != null) {
                boolean alterInteraction = new ConditionalSleep(2000) {
                    @Override
                    public boolean condition() {
                        return astralAltar[0].interact("Craft-rune");
                    }
                }.sleep();
                if(alterInteraction) {
                    if(settings.getRunEnergy() >= 20) {
                        magic.hoverSpell(Spells.LunarSpells.TELE_GROUP_MOONCLAN);
                    }
                    new ConditionalSleep(5000) {
                        @Override
                        public boolean condition() {
                            return !inventory.contains("Pure essence") && myPlayer().getAnimation() != 791;
                        }
                    }.sleep();
                } else {
                    log("cannot interact with alter will retry");
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
