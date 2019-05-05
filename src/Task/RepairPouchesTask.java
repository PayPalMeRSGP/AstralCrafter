package Task;

import org.osbot.rs07.Bot;
import org.osbot.rs07.api.ui.Message;
import org.osbot.rs07.api.ui.RS2Widget;
import org.osbot.rs07.api.ui.Spells;
import org.osbot.rs07.listener.MessageListener;
import org.osbot.rs07.utility.ConditionalSleep;

import java.util.Arrays;

public class RepairPouchesTask extends ReactiveTask implements MessageListener {
    private boolean shouldEnqueue = false;

    public RepairPouchesTask(Bot bot) {
        super(bot);
    }

    @Override
    public void task() throws InterruptedException {
        if(magic.open()) {
            RS2Widget npcContact = widgets.get(218, 103);
            if(npcContact != null && npcContact.getSpriteIndex1() == 568) {
                boolean hasDarkMageContactOption = Arrays.asList(npcContact.getInteractActions()).contains("Dark Mage");
                if(hasDarkMageContactOption && npcContact.interact("Dark Mage")) {
                    if(handleRepairPouchDialogue()) {
                        log("pouch repair successful");
                    }
                } else {
                    if(npcContact.interact("Cast")) {
                        final RS2Widget[] darkMage = new RS2Widget[1];
                        boolean widgetExists = new ConditionalSleep(3000) {
                            @Override
                            public boolean condition() throws InterruptedException {
                                darkMage[0] = widgets.get(75, 13);
                                return darkMage[0] != null;
                            }
                        }.sleep();
                        if(widgetExists) {

                        }
                    }
                    if(handleRepairPouchDialogue()) {
                        log("pouch repair successful");
                    }
                }
            }
        }

    }

    @Override
    boolean shouldEnqueue() {
        if(shouldEnqueue || inventory.contains(5513)){
            shouldEnqueue = false;
            return true;
        }
        return false;
    }

    @Override
    public String getClassName() {
        return "RepairPouchesTask";
    }

    private boolean handleRepairPouchDialogue() throws InterruptedException {
        boolean inDialogue = new ConditionalSleep(8000) {
            @Override
            public boolean condition() throws InterruptedException {
                return dialogues.inDialogue();
            }
        }.sleep();
        return inDialogue && dialogues.completeDialogue("Can you repair my pouches?");
    }

    @Override
    public void onMessage(Message message) throws InterruptedException {
        if(message.getType() == Message.MessageType.GAME && message.getMessage().equals("Your pouch has decayed through use.")) {
            shouldEnqueue = true;
        }
    }
}
