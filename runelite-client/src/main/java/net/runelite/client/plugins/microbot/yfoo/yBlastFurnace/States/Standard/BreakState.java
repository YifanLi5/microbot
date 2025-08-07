package net.runelite.client.plugins.microbot.yfoo.yBlastFurnace.States.Standard;

import net.runelite.api.ObjectID;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.util.Global;
import net.runelite.client.plugins.microbot.util.antiban.Rs2Antiban;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.yfoo.GeneralUtil.RngUtil;
import net.runelite.client.plugins.microbot.yfoo.StateMachine.StateNode;
import net.runelite.client.plugins.microbot.yfoo.yBlastFurnace.BFScript;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;

public class BreakState extends StateNode {

    Instant lastBreakTs;
    static BreakState instance;

    enum StateSteps {
        CHECK_CONDITIONS, BREAK
    }

    public static BreakState getInstance() {
        if(instance == null) {
            throw new NullPointerException(RetrieveBarsState.class.getSimpleName() + " is null");
        }
        return instance;
    }

    public static BreakState initInstance(BFScript script) {
        if (instance == null)
            instance = new BreakState(script);
        return instance;
    }

    private BreakState(BFScript script) {
        super(script);
        this.lastBreakTs = Instant.now();
    }

    @Override
    public boolean canRun() throws InterruptedException {
        return true;
    }

    @Override
    public void initStateSteps() {
        this.stateSteps = new LinkedHashMap<>();
        this.stateSteps.put(StateSteps.BREAK, () -> {
            Duration elapsed = Duration.between(lastBreakTs, Instant.now());
            Microbot.log("seconds: " + elapsed.getSeconds());
            if(elapsed.getSeconds() <= 1800) {
                Microbot.log("No break needed");
                return true;
            }
            boolean takeBreak = RngUtil.boolD100Roll(10);
            if(takeBreak) {
                this.lastBreakTs = Instant.now();
                if(!Rs2GameObject.interact(9138, "Climb-up")) {
                    Microbot.log("Failed to climb up");
                    return false;
                }
                Global.sleepUntil(() -> Rs2GameObject.exists(9084));
                int sleepMs = RngUtil.randomInclusive(15_000, 100_000);
                Microbot.log("Taking a break for " + sleepMs);
                Rs2Antiban.moveMouseOffScreen();
                Global.sleep(sleepMs);
                Microbot.log("break is done");
                if(!Rs2GameObject.interact(9084, "Climb-down")) {
                    Microbot.log("Failed to climb back down");
                    return false;
                }
                return Global.sleepUntil(() -> Rs2GameObject.exists(ObjectID.CONVEYOR_BELT));
            }
            return true;
        });
    }

    @Override
    public StateNode nextState() throws InterruptedException {
        return BankState.getInstance();
    }
}
