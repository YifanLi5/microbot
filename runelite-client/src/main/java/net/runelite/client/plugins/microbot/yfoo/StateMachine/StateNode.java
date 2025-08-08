package net.runelite.client.plugins.microbot.yfoo.StateMachine;
import net.runelite.client.config.Config;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.yfoo.yBlastFurnace.BFConfig;
import net.runelite.client.plugins.microbot.yfoo.yBlastFurnace.BFPlugin;
import net.runelite.client.plugins.microbot.yfoo.yBlastFurnace.Script;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BooleanSupplier;

public abstract class StateNode {

    protected Script script;
    protected LinkedHashMap<Enum<?>, BooleanSupplier> stateSteps;

    public StateNode(Script script) {
        this.script = script;
        initStateSteps();
        assert this.stateSteps != null;
        Microbot.log("Initialized state: " + this.getClass().getSimpleName());
    }

    public abstract boolean canRun() throws InterruptedException;
    public abstract void initStateSteps();
    public abstract StateNode nextState() throws InterruptedException;

    public int retries() {
        return 1;
    }

    public boolean handleState() throws InterruptedException {
        if(runStateSteps()) {
            StateManager.queueState(nextState());
            return true;
        }
        return false;
    }

    public boolean runStateSteps() throws InterruptedException {
        boolean allSucceeded = true;
        for (Map.Entry<Enum<?>, BooleanSupplier> entry : stateSteps.entrySet()) {

            Enum<?> stateEnum = entry.getKey();
            BooleanSupplier step = entry.getValue();
            String stepId = stateEnum.getClass().getSimpleName() + " :: " + stateEnum.name();
            Microbot.log("Processing state: " + stepId);

            boolean success = false;
            for (int attempt = 0; attempt < retries(); attempt++) {
                if(BFPlugin.isShutdown.get()) {
                    Microbot.log("Got shutdown boolean");
                    return true;
                }
                success = step.getAsBoolean();
                if(success) break;
                Microbot.log("Failed: " + stepId);
                Thread.sleep(ThreadLocalRandom.current().nextInt(300, 801));
            }

            if (!success) {
                allSucceeded = false;
                break;
            }
        }

        return allSucceeded;
    }
}
