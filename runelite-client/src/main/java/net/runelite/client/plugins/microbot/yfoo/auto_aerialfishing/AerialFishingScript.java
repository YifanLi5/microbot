package net.runelite.client.plugins.microbot.yfoo.auto_aerialfishing;

import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.yfoo.Task.Task;
import net.runelite.client.plugins.microbot.yfoo.auto_aerialfishing.TaskSubclasses.CatchAerialFish;
import net.runelite.client.plugins.microbot.yfoo.auto_aerialfishing.TaskSubclasses.FilletAerialFish;

import javax.inject.Inject;
import java.util.concurrent.TimeUnit;

public class AerialFishingScript extends Script {

    public static final String version = "0.20";


    public AerialFishingScript(AerialFishingConfig config) {
        FilletAerialFish.initInstance(this, config);
        CatchAerialFish.initInstance(this);
    }

    public boolean run() {
        Task.stopScriptNow = false;
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
