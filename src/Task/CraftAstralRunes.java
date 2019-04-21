package Task;

import org.osbot.rs07.Bot;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.model.InteractableObject;

public class CraftAstralRunes extends PrioritizedReactiveTask {

    CraftAstralRunes(Bot bot) {
        super(bot);
        this.priority = Priority.NORMAL;
    }

    @Override
    void task() throws InterruptedException {

    }

    @Override
    boolean shouldTaskActivate() {
        InteractableObject astralAltar = (InteractableObject) objects.closest(new Area(1, 1, 1, 1), "Astral altar");
        return false;
    }
}
