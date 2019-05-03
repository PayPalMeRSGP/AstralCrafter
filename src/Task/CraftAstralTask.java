package Task;

import org.osbot.rs07.Bot;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.model.Item;
import org.osbot.rs07.api.model.RS2Object;
import org.osbot.rs07.api.ui.Tab;
import org.osbot.rs07.event.InteractionEvent;
import org.osbot.rs07.event.WalkingEvent;
import org.osbot.rs07.input.keyboard.TypeKeyEvent;
import org.osbot.rs07.input.mouse.InventorySlotDestination;
import org.osbot.rs07.utility.ConditionalSleep;

import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.concurrent.ThreadLocalRandom;

public class CraftAstralTask extends ReactiveTask {

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

    private final Runnable asyncPressF2Key = () -> {
        try {
            sleep(randomNormalDist(8000, 3000));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (keyboard.typeFKey(KeyEvent.VK_F2)) {
            log("Thread " + Thread.currentThread().getId() + " pressed F2 Key");
        }
    };

    public CraftAstralTask(Bot bot) {
        super(bot);
    }

    @Override
    public void task() throws InterruptedException {

        WalkingEvent astralAltarWalkEvent = new WalkingEvent();
        if(ThreadLocalRandom.current().nextBoolean()) {
            astralAltarWalkEvent.setPath(PATH1);
        } else {
            astralAltarWalkEvent.setPath(PATH2);
        }

        astralAltarWalkEvent.setMinDistanceThreshold(5);
        astralAltarWalkEvent.setOperateCamera(false);

        new Thread(asyncPressF2Key).start();
        execute(astralAltarWalkEvent);

        if(astralAltarWalkEvent.hasFinished()) {
            RS2Object astralAltar = objects.closest(new Area(2155, 3867, 2161, 3861), "Altar");
            if(astralAltar != null && altarInteraction(astralAltar)) {
                log("altar interaction complete");
                if(tabs.open(Tab.INVENTORY) && emptyPouches()) {
                    altarInteraction(astralAltar);
                }
            }
        }
    }

    private boolean altarInteraction(RS2Object altar) {
        InteractionEvent altarInteraction = new InteractionEvent(altar);
        altarInteraction.setOperateCamera(false);
        execute(altarInteraction);
        if(altarInteraction.hasFinished()) {
            return new ConditionalSleep(5000) {
                @Override
                public boolean condition() {
                    return !inventory.contains("Pure essence") && myPlayer().getAnimation() != 791;
                }
            }.sleep();
        }
        return false;
    }

    private boolean emptyPouches() {
        Item[] invItems = inventory.getItems();
        keyboard.pressKey(KeyEvent.VK_SHIFT);

        if(ThreadLocalRandom.current().nextBoolean()) {
            for(int i = 0; i < invItems.length; i++) {
                if(invItems[i] != null && invItems[i].nameContains("Small pouch", "Medium pouch", "Large pouch")) {
                    int emptySlots = inventory.getEmptySlotCount();
                    if(invItems[i].getName().equals("Small pouch") && emptySlots >= 3) {
                        mouse.click(new InventorySlotDestination(bot, i));
                        log("Emptied Small pouch");
                    }
                    else if(invItems[i].getName().equals("Medium pouch") && emptySlots >= 6) {
                        mouse.click(new InventorySlotDestination(bot, i));
                        log("Emptied Medium pouch");
                    }
                    else if(invItems[i].getName().equals("Large pouch") && emptySlots >= 9) {
                        mouse.click(new InventorySlotDestination(bot, i));
                        log("Emptied Large pouch");
                    }
                }
            }
        } else {
            for(int i = invItems.length - 1; i > -1; i--) {
                if(invItems[i] != null && invItems[i].nameContains("Small pouch", "Medium pouch", "Large pouch")) {
                    int emptySlots = inventory.getEmptySlotCount();
                    if(invItems[i].getName().equals("Small pouch") && emptySlots >= 3) {
                        mouse.click(new InventorySlotDestination(bot, i));
                        log("Emptied Small pouch");
                    }
                    else if(invItems[i].getName().equals("Medium pouch") && emptySlots >= 6) {
                        mouse.click(new InventorySlotDestination(bot, i));
                        log("Emptied Medium pouch");
                    }
                    else if(invItems[i].getName().equals("Large pouch") && emptySlots >= 9) {
                        mouse.click(new InventorySlotDestination(bot, i));
                        log("Emptied Large pouch");
                    }
                }
            }
        }

        keyboard.releaseKey(KeyEvent.VK_SHIFT);
        return inventory.contains("Pure essence");
    }

    @Override
    boolean shouldEnqueue() throws InterruptedException {
        Thread.sleep(ENQUEUE_POLLING_INTERVAL_MS);
        return inventory.contains("Pure essence");
    }

    @Override
    public String getClassName() {
        return "CraftAstralTask";
    }
}
