package net.runelite.client.plugins.microbot.yfoo.StateMachine;

import lombok.Getter;

public class StateStepResult<K extends Enum<K>> {
    @Getter
    K nextStepIfSuccess;
    boolean isSuccess;

    public StateStepResult(K nextStep, boolean result) {
        this.nextStepIfSuccess = nextStep;
        this.isSuccess = result;
    }

    public boolean getResult() {
        return isSuccess;
    }
}
