package net.runelite.client.plugins.microbot.yfoo.camdozaal_mining;

import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.antiban.Rs2Antiban;
import net.runelite.client.plugins.microbot.util.antiban.Rs2AntibanSettings;
import net.runelite.client.plugins.microbot.util.antiban.enums.ActivityIntensity;
import net.runelite.client.plugins.microbot.yfoo.StateMachine.StateEventDispatcher;
import net.runelite.client.plugins.microbot.yfoo.StateMachine.StateManagerV2;
import net.runelite.client.plugins.microbot.yfoo.camdozaal_mining.states.BankingState;
import net.runelite.client.plugins.microbot.yfoo.camdozaal_mining.states.MiningState;
import net.runelite.client.plugins.microbot.yfoo.camdozaal_mining.states.SmashingState;
import net.runelite.client.plugins.microbot.yfoo.camdozaal_mining.states.StartingState;

import java.util.concurrent.TimeUnit;

public class CamdozaalMiningScript extends Script {

    public static boolean test = false;

    public boolean run(CamdozaalMiningConfig config) {
        Microbot.log("*************** Camdozaal Mining Script ***************");
        StateManagerV2.init();
        StateManagerV2.addState(StartingState.initInstance(this));
        StateManagerV2.addState(MiningState.initInstance(this));
        StateManagerV2.addState(SmashingState.initInstance(this));
        StateManagerV2.addState(BankingState.initInstance(this));
        StateManagerV2.setNextState(StartingState.getInstance());

        Rs2Antiban.setActivityIntensity(ActivityIntensity.VERY_LOW);
        Rs2AntibanSettings.naturalMouse = true;

        Microbot.enableAutoRunOn = false;
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn()) return;
                if (!super.run()) return;

                StateManagerV2.runLoop();

            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }, 0, 1000, TimeUnit.MILLISECONDS);
        return true;
    }
    
    @Override
    public void shutdown() {
        StateEventDispatcher.cleanup();
        super.shutdown();
    }
}