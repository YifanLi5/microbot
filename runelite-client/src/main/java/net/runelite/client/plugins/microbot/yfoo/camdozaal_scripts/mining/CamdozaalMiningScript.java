package net.runelite.client.plugins.microbot.yfoo.camdozaal_scripts.mining;

import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.yfoo.Task.Task;
import net.runelite.client.plugins.microbot.yfoo.camdozaal_scripts.mining.TaskSubclasses.MineRocks;

import java.util.concurrent.TimeUnit;

public class CamdozaalMiningScript extends Script {
    public CamdozaalMiningScript(CamdozaalMiningConfig config) {
        MineRocks.initInstance(this);
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
