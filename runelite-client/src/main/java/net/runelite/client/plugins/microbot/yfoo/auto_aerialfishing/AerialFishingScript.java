package net.runelite.client.plugins.microbot.yfoo.auto_aerialfishing;

import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.yfoo.Task.Task;
import net.runelite.client.plugins.microbot.yfoo.auto_aerialfishing.TaskSubclasses.AerialFish;
import net.runelite.client.plugins.microbot.yfoo.auto_aerialfishing.TaskSubclasses.CutAerialFish;

import java.util.concurrent.TimeUnit;

public class AerialFishingScript extends Script {

    public static final String version = "0.01";

    public AerialFishingScript() {
        AerialFish.initInstance(this);
        CutAerialFish.initInstance(this);
    }

    public boolean run(AerialFishingConfig config) {
        Task.stopScriptNow = false;
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            if (!super.run() || !Microbot.isLoggedIn()) {

                return;
            }
            try {
                Task.runLoopIteration(this);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }, 0, 600, TimeUnit.MILLISECONDS);
        return true;
    }



}
