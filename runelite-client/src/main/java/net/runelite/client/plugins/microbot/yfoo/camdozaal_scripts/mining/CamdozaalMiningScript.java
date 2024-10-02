package net.runelite.client.plugins.microbot.yfoo.camdozaal_scripts.mining;

import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.yfoo.Task.Task;
import net.runelite.client.plugins.microbot.yfoo.camdozaal_scripts.mining.TaskSubclasses.BankAndReturn;
import net.runelite.client.plugins.microbot.yfoo.camdozaal_scripts.mining.TaskSubclasses.MineRocks;
import net.runelite.client.plugins.microbot.yfoo.camdozaal_scripts.mining.TaskSubclasses.SmashDeposits;

import java.util.concurrent.TimeUnit;

public class CamdozaalMiningScript extends Script {
    public CamdozaalMiningScript(CamdozaalMiningConfig config) {

    }

    public boolean run() {
        Task.resetStatics();

        SmashDeposits.initInstance(this);
        MineRocks.initInstance(this);
        BankAndReturn.initInstance(this);

        this.mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            Microbot.log(String.format("(%d) Is script running? %s", this.hashCode(), this.isRunning()));
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
