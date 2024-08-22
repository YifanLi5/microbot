package net.runelite.client.plugins.microbot.yfoo.auto_aerialfishing;

import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.yfoo.Task.Task;
import net.runelite.client.plugins.microbot.yfoo.auto_aerialfishing.TaskSubclasses.CatchAerialFish;
import net.runelite.client.plugins.microbot.yfoo.auto_aerialfishing.TaskSubclasses.FilletAerialFish;

import java.util.concurrent.TimeUnit;

public class AerialFishingScript extends Script {

    public static final String version = "0.18";

    public AerialFishingScript() {
        FilletAerialFish.initInstance(this);
        CatchAerialFish.initInstance(this);
    }

    public boolean run(AerialFishingConfig config) {
        Task.stopScriptNow = false;
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            if (!super.run() || !Microbot.isLoggedIn()) {
                Microbot.log("Short circuit");
                return;
            }

            try {
                Task.runLoopIteration(this);
            } catch (Exception e) {
                Microbot.log(e.getMessage());
                throw new RuntimeException(e);
            }
        }, 0, 600, TimeUnit.MILLISECONDS);
        return true;
    }



}
