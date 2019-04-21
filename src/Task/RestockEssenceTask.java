package Task;

import org.osbot.rs07.Bot;
import org.osbot.rs07.api.Bank;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.ui.Spells;
import org.osbot.rs07.event.WalkingEvent;
import org.osbot.rs07.utility.ConditionalSleep;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.Condition;

public class RestockEssenceTask extends PrioritizedReactiveTask {

    private static final Area MOONCLAN_ISLAND = new Area(2088, 3927, 2118, 3907);
    private static final Area MOONCLAN_BANK = new Area(2097, 3919, 2100, 3918);
    private static final List<Position> BANK_PATH = Arrays.asList(
            new Position(2109, 3915, 0),
            new Position(2108, 3915, 0),
            new Position(2107, 3915, 0),
            new Position(2106, 3915, 0),
            new Position(2105, 3915, 0),
            new Position(2104, 3915, 0),
            new Position(2103, 3915, 0),
            new Position(2102, 3915, 0),
            new Position(2101, 3915, 0),
            new Position(2100, 3915, 0),
            new Position(2099, 3915, 0),
            new Position(2099, 3916, 0),
            new Position(2099, 3917, 0),
            new Position(2099, 3918, 0)
    );

    public RestockEssenceTask(Bot bot) {
        super(bot);
        this.priority = Priority.NORMAL;
    }

    @Override
    public void task() throws InterruptedException {
        boolean canWalkToBank = false;
        if(MOONCLAN_ISLAND.contains(myPosition())) {
            canWalkToBank = true;
        }
        else if(magic.canCast(Spells.LunarSpells.TELE_GROUP_MOONCLAN)) {
            if(magic.castSpell(Spells.LunarSpells.TELE_GROUP_MOONCLAN)) {
                canWalkToBank = new ConditionalSleep(3000) {
                    @Override
                    public boolean condition() throws InterruptedException {
                        return MOONCLAN_ISLAND.contains(myPosition());
                    }
                }.sleep();
            }
        }

        if(canWalkToBank) {
            if(bank.open()) {
                if(bank.withdraw("Pure essence", Bank.WITHDRAW_ALL)) {
                    new ConditionalSleep(5000) {
                        @Override
                        public boolean condition() throws InterruptedException {
                            return inventory.contains("Pure essence");
                        }
                    }.sleep();
                }
            }
             else {
                log("walking event failed");
            }
        }
    }

    @Override
    boolean shouldTaskActivate() {
        return !inventory.contains("Pure essence");
    }

    @Override
    String getClassName() {
        return "RestockEssenceTask";
    }
}
