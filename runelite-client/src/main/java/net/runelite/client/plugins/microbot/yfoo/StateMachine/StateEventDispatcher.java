package net.runelite.client.plugins.microbot.yfoo.StateMachine;

import java.util.ArrayList;
import java.util.List;

public class StateEventDispatcher {
    static List<StateEventSubscriber> subscribers;

    public static void init() {
        subscribers = new ArrayList<>();
    }

    public static void cleanup() {
        if(subscribers != null)
            subscribers.clear();
    }

    public static void addSubscriber(StateEventSubscriber subscriber) {
        subscribers.add(subscriber);
    }

    protected static void dispatchEvent(StateNodeV2<?> state, Enum<?> step) {
        String stateName = state.getClass().getSimpleName();
        String stepName = step.name();

        subscribers.forEach(subscriber -> subscriber.onEvent(stateName, stepName));
    }
}
