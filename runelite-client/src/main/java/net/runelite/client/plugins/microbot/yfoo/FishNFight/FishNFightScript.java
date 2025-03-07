package net.runelite.client.plugins.microbot.yfoo.FishNFight;

import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.yfoo.Task.Task;

import java.util.concurrent.TimeUnit;

public class FishNFightScript extends Script {

    public static final String version = "0.01";


    public FishNFightScript(FishNFightConfig config) {
    }

    public boolean run() {
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            if (!super.run() || !Microbot.isLoggedIn()) {
                return;
            }

            Microbot.log("Test!");
        }, 0, 600, TimeUnit.MILLISECONDS);
        return true;
    }



}
