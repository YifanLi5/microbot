package net.runelite.client.plugins.microbot.yfoo.StateMachine;

import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;


import java.util.AbstractMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public abstract class StateNodeV2<K extends Enum<K>> {
    protected Script script;
    protected LinkedHashMap<K, Supplier<StateStepResult<K>>> stateSteps;

    public StateNodeV2(Script script) {
        this.script = script;
        initStateSteps();
        assert this.stateSteps != null;
        Microbot.log("Initialized state: " + this.getClass().getSimpleName());
    }

    public abstract boolean canRun() throws InterruptedException;
    public abstract void initStateSteps();
    public abstract StateNodeV2<?> nextState() throws InterruptedException;
    public int retries() {
        return 1;
    }
    public abstract Enum<K> completionStep();

    public boolean handleState() throws InterruptedException {
        if(runStateSteps()) {
            StateManagerV2.setNextState(nextState());
            return true;
        }
        return false;
    }

    public boolean runStateSteps() throws InterruptedException {
        int attempt = 1;
        int loopCount = 0;
        Map.Entry<K, Supplier<StateStepResult<K>>> currentStep = stateSteps.entrySet().iterator().next();
        while(script.isRunning() && currentStep.getKey() != completionStep() && attempt <= 3 && loopCount < 20) {
            loopCount += 1;

            Microbot.log("Step: %s | Attempt: %d", currentStep.getKey(), attempt);
            StateEventDispatcher.dispatchEvent(this, currentStep.getKey());
            StateStepResult<K> stepResult = currentStep.getValue().get();
            if(stepResult.isSuccess) {
                K nextStepEnum = stepResult.getNextStepIfSuccess();
                Supplier<StateStepResult<K>> nextStepAction = stateSteps.get(nextStepEnum);
                if (nextStepAction == null) {
                    Microbot.log("Invalid next step: %s", nextStepEnum);
                    break;
                }

                currentStep = new AbstractMap.SimpleEntry<>(nextStepEnum, nextStepAction);
                attempt = 1;
            } else {
                attempt += 1;
            }
        }

        return currentStep.getKey().equals(completionStep());
    }
}
