package Task;

import org.osbot.rs07.Bot;
import org.osbot.rs07.api.Bank;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.model.RS2Object;
import org.osbot.rs07.api.ui.Spells;
import org.osbot.rs07.event.InteractionEvent;
import org.osbot.rs07.event.WalkingEvent;
import org.osbot.rs07.utility.ConditionalSleep;

import java.util.Arrays;
import java.util.List;
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
            if(openBankNoCamera()) {
                log("bank has been opened");
                final String PURE_ESS = "Pure essence";
                if(bank.getAmount(PURE_ESS) > 28) {
                    if(bank.withdraw(PURE_ESS, Bank.WITHDRAW_ALL)) {
                        new ConditionalSleep(5000) {
                            @Override
                            public boolean condition() throws InterruptedException {
                                return inventory.contains("Pure essence");
                            }
                        }.sleep();
                    }
                } else {
                    log("Stop Condition -> Out of essence");
                }
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

    private boolean openBankNoCamera() throws InterruptedException {
        List<RS2Object> rs2ObjList = objects.get(2099, 3920);
        RS2Object bankBoothObj = rs2ObjList.stream()
                .filter(rs2Object -> rs2Object.getId() == 16700)
                .findFirst().orElse(null);
        InteractionEvent bankInteraction = null;
        if(bankBoothObj != null) {
            log("found bank, setting up interaction event");
            bankInteraction = new InteractionEvent(bankBoothObj);
            bankInteraction.setOperateCamera(false);

        }
        if(bankInteraction != null) {
            log("executing interaction event");
            execute(bankInteraction);
            boolean opened = new ConditionalSleep(2000) {
                @Override
                public boolean condition() throws InterruptedException {
                    return bank.isOpen();
                }
            }.sleep();

            return opened || bank.open();

        }
        return bank.open();
    }
}
