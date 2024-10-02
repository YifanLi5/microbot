package net.runelite.client.plugins.microbot.yfoo.camdozaal_scripts.fishing;

import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.yfoo.Task.Task;
import net.runelite.client.plugins.microbot.yfoo.camdozaal_scripts.fishing.TaskSubclasses.CatchCamdozaalFish;
import net.runelite.client.plugins.microbot.yfoo.camdozaal_scripts.fishing.TaskSubclasses.PrepareCamdozaalFish;

import java.util.concurrent.TimeUnit;

public class CamdozaalFishingScript extends Script {

    public CamdozaalFishingScript(CamdozaalFishingConfig config) {
    }

    public boolean run() {
        Task.stopScriptNow = false;
        Task.resetStatics();

        PrepareCamdozaalFish.initInstance(this);
        CatchCamdozaalFish.initInstance(this);

        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            if (!super.run() || !Microbot.isLoggedIn()) {
                return;
            }

            try {
                Task.runLoopIteration(this);
            } catch (Exception e) {
                Microbot.log("Encountered Exception: " + e.getMessage());
                this.shutdown();
            }
        }, 0, 600, TimeUnit.MILLISECONDS);
        return true;
    }
}

