package net.runelite.client.plugins.microbot.yfoo.camdozaal_scripts.mining;

import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.yfoo.Task.Task;
import net.runelite.client.plugins.microbot.yfoo.camdozaal_scripts.mining.TaskSubclasses.BankAndReturn;
import net.runelite.client.plugins.microbot.yfoo.camdozaal_scripts.mining.TaskSubclasses.DropBarroniteDeposits;
import net.runelite.client.plugins.microbot.yfoo.camdozaal_scripts.mining.TaskSubclasses.MineRocks;
import net.runelite.client.plugins.microbot.yfoo.camdozaal_scripts.mining.TaskSubclasses.SmashDeposits;

import java.util.concurrent.TimeUnit;

public class CamdozaalMiningScript extends Script {

    public CamdozaalMiningConfig config;

    public CamdozaalMiningScript(CamdozaalMiningConfig config) {
        this.config = config;

    }

    public boolean run() {
        Task.resetStatics();

        DropBarroniteDeposits.initInstance(this, config);
        SmashDeposits.initInstance(this, config);
        MineRocks.initInstance(this);
        BankAndReturn.initInstance(this);

        this.mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
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
