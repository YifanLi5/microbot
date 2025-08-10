package net.runelite.client.plugins.microbot.yfoo.StateMachine;

public interface StateEventSubscriber {
    void onEvent(String stateName, String stepName);
}
