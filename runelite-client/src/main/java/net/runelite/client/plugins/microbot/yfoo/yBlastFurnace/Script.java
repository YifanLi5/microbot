package net.runelite.client.plugins.microbot.yfoo.yBlastFurnace;

import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.util.antiban.Rs2Antiban;
import net.runelite.client.plugins.microbot.util.antiban.Rs2AntibanSettings;
import net.runelite.client.plugins.microbot.util.antiban.enums.ActivityIntensity;
import net.runelite.client.plugins.microbot.yfoo.GeneralUtil.HoverBoundsUtil;
import net.runelite.client.plugins.microbot.yfoo.GeneralUtil.IdleSleep;
import net.runelite.client.plugins.microbot.yfoo.StateMachine.StateManager;
import net.runelite.client.plugins.microbot.yfoo.yBlastFurnace.States.Hybrid.*;
import net.runelite.client.plugins.microbot.yfoo.yBlastFurnace.States.Standard.*;
import net.runelite.client.plugins.microbot.yfoo.yBlastFurnace.States.StartingState;

import java.util.concurrent.TimeUnit;

public class Script extends net.runelite.client.plugins.microbot.Script {

    public static final String version = "0.01";
    public BFConfig config;

    public Script(BFConfig config) {
        this.config = config;
    }

    public boolean run() {
        HoverBoundsUtil.init();
        StateManager.init();
        StateManager.addState(StartingState.initInstance(this));
        if(config.barType().isHybrid) {
            StateManager.addState(BankState2.initInstance(this));
            StateManager.addState(DropOffRocksState2.initInstance(this));
            StateManager.addState(RetrieveBarsState2.initInstance(this));
        } else {
            StateManager.addState(BankState.initInstance(this));
            StateManager.addState(DropOffRocksState.initInstance(this));
            StateManager.addState(RetrieveBarsState.initInstance(this));
            StateManager.addState(RefillCofferState.initInstance(this));
            StateManager.addState(BreakState.initInstance(this));
        }

        StateManager.queueState(StartingState.getInstance());
        IdleSleep.randomizeFields();
        Rs2Antiban.setActivityIntensity(ActivityIntensity.VERY_LOW);
        Rs2AntibanSettings.naturalMouse = true;
        BFOverlay.resetBars();


        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            if (!super.run() || !Microbot.isLoggedIn()) {
                return;
            }
            if (StateManager.stopScript) {
                this.shutdown();
                return;
            }

            try {
                StateManager.runOnLoopCycle();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }, 0, 100, TimeUnit.MILLISECONDS);
        return true;
    }



}
