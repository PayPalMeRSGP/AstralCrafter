package Task;

import org.osbot.rs07.Bot;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.event.WebWalkEvent;

public class GoToAstralAltar extends PrioritizedReactiveTask {

    private WebWalkEvent astralAltarWalkEvent;

    GoToAstralAltar(Bot bot) {
        super(bot);
        this.priority = Priority.NORMAL;
        Area ASTRAL_ALTER = new Area(1, 1, 1, 1); //TODO set me
        this.astralAltarWalkEvent = new WebWalkEvent(ASTRAL_ALTER);
        this.astralAltarWalkEvent.setMoveCameraDuringWalking(false);
    }

    @Override
    void task() throws InterruptedException {
        execute(astralAltarWalkEvent);
    }

    @Override
    boolean shouldTaskActivate() {
        return inventory.contains("Pure essence");
    }
}
