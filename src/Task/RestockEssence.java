package Task;

import org.osbot.rs07.Bot;
import org.osbot.rs07.api.Bank;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.ui.Spells;

public class RestockEssence extends PrioritizedReactiveTask {

    private Area MOONCLAN_ISLAND = new Area(1,1,1,1); //TODO: set
    private Area MOONCLAN_BANK = new Area(1,1,1,1);

    RestockEssence(Bot bot) {
        super(bot);
        this.priority = Priority.NORMAL;
    }

    @Override
    void task() throws InterruptedException {
        boolean canWalkToBank = false;
        if(MOONCLAN_ISLAND.contains(myPosition())) {
            canWalkToBank = true;
        } else if(magic.canCast(Spells.LunarSpells.TELE_GROUP_MOONCLAN)) {
            canWalkToBank = magic.castSpell(Spells.LunarSpells.TELE_GROUP_MOONCLAN);
        }

        if(canWalkToBank) {
            if(walking.walk(MOONCLAN_BANK)) {
                if(bank.open()) {
                    bank.withdraw("Pure essence", Bank.WITHDRAW_ALL);
                }
            }
        }
    }

    @Override
    boolean shouldTaskActivate() {
        return !inventory.contains("Pure essence");
    }
}
