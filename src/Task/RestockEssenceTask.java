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
import java.util.LinkedList;
import java.util.List;

public class RestockEssenceTask extends ReactiveTask {

    private static final Area MOONCLAN_ISLAND = new Area(2088, 3927, 2118, 3907);
    private static final LinkedList<Position> BANK_PATH = new LinkedList<>(Arrays.asList(
            new Position(2107, 3915, 0),
            new Position(2103, 3915, 0),
            new Position(2100, 3917, 0),
            new Position(2099, 3919, 0)
    ));

    public RestockEssenceTask(Bot bot) {
        super(bot);
    }

    @Override
    public void task() throws InterruptedException {
        boolean atMoonclanIsland = false;
        if(npcs.closest("Rimae Sirsalis") != null) {
            atMoonclanIsland = true;
        }
        else if(magic.castSpell(Spells.LunarSpells.TELE_GROUP_MOONCLAN)) {
            atMoonclanIsland = new ConditionalSleep(5000, 500) {
                @Override
                public boolean condition() {
                    return npcs.closest("Rimae Sirsalis") != null;
                }
            }.sleep();
        }

        if(atMoonclanIsland && openBankAlternative() && withdrawEssence() && fillPouches() && withdrawEssence()) {
            log("completed restocking task successfully");
        }
    }

    @Override
    boolean shouldEnqueue() throws InterruptedException {
        Thread.sleep(ENQUEUE_POLLING_INTERVAL_MS);
        return !inventory.contains("Pure essence") && settings.getRunEnergy() >= 20;
    }

    @Override
    public String getClassName() {
        return "RestockEssenceTask";
    }

    private boolean openBankAlternative() throws InterruptedException {
        WalkingEvent bankWalk = new WalkingEvent();
        bankWalk.setPath(BANK_PATH);
        bankWalk.setOperateCamera(false);
        execute(bankWalk);

        if(bankWalk.hasFinished()) {
            List<RS2Object> rs2ObjList = objects.get(2099, 3920);
            RS2Object bankBoothObj = rs2ObjList.stream()
                    .filter(rs2Object -> rs2Object.getId() == 16700)
                    .findFirst().orElse(null);
            InteractionEvent bankInteraction = null;
            if(bankBoothObj != null) {
                bankInteraction = new InteractionEvent(bankBoothObj);
                bankInteraction.setOperateCamera(false);

            }
            if(bankInteraction != null) {
                execute(bankInteraction);
                boolean opened = new ConditionalSleep(2000) {
                    @Override
                    public boolean condition() {
                        return bank.isOpen();
                    }
                }.sleep();
                return opened || bank.open();
            }
        }
        return bank.open();
    }

    private boolean withdrawEssence() throws InterruptedException {
        if(bank.isOpen() || bank.open()) {
            final String PURE_ESS = "Pure essence";
            if(bank.getAmount(PURE_ESS) > 50) {
                if (bank.withdraw(PURE_ESS, Bank.WITHDRAW_ALL)) {
                    return new ConditionalSleep(5000) {
                        @Override
                        public boolean condition() {
                            return inventory.contains("Pure essence");
                        }
                    }.sleep();
                }
            } else {
                log("Stop Condition -> Out of essence");
                bot.getScriptExecutor().stop(false);
            }
        }
        return false;
    }

    private boolean fillPouches() {
        //return true only if all pouches in the inventory have been filled.
        //if a pouch does not exist in the inventory it is counted as "handled"
        boolean handledSmall = !inventory.contains("Small pouch") || inventory.interact("Fill", "Small pouch");
        boolean handledMed = !inventory.contains("Medium pouch") || inventory.interact("Fill", "Medium pouch");
        boolean handledLarge = !inventory.contains("Large pouch") || inventory.interact("Fill", "Large pouch");
        return handledSmall && handledMed && handledLarge;
    }


}
