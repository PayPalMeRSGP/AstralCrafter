package Task;

import org.osbot.rs07.Bot;
import org.osbot.rs07.api.Bank;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.ui.Spells;
import org.osbot.rs07.event.WalkingEvent;
import org.osbot.rs07.utility.ConditionalSleep;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.Condition;

public class RestockEssenceTask extends PrioritizedReactiveTask {

    private static final Area MOONCLAN_ISLAND = new Area(2088, 3927, 2118, 3907);
    private static final Area MOONCLAN_BANK = new Area(2097, 3919, 2100, 3918);

    public RestockEssenceTask(Bot bot) {
        super(bot);
        this.priority = Priority.NORMAL;
    }

    @Override
    public void task() throws InterruptedException {
        boolean canWalkToBank = false;
        if(MOONCLAN_ISLAND.contains(myPosition())) {
            canWalkToBank = true;
        } else if(magic.canCast(Spells.LunarSpells.TELE_GROUP_MOONCLAN)) {
            canWalkToBank =  magic.castSpell(Spells.LunarSpells.TELE_GROUP_MOONCLAN);
        }

        if(canWalkToBank) {
            log("trying to walk to bank");
            if(walking.walk(MOONCLAN_BANK)) {
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
            } else {
                log("walking event failed");
            }
        } else {
            log("ERROR: Cannot walk back to bank to restock");
            bot.getScriptExecutor().stop(false);
        }
    }

    @Override
    boolean shouldTaskActivate() {
        return !inventory.contains("Pure essence");
    }
}
