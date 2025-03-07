package net.runelite.client.plugins.microbot.yfoo.StateMachine;

import net.runelite.client.plugins.microbot.Microbot;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class StateManager {

    public static boolean stopScript = false;
    public final static boolean LOGOUT_ON_SCRIPT_STOP = false;

    static List<State> initializedStates;
    static List<State> nextStates;
    static Iterator<State> stateIterator;

    public static void init() {
        initializedStates = new ArrayList<>();
        stopScript = false;
    }

    public static State findFirstRunnableState() throws InterruptedException {
        for(State state: initializedStates) {
            boolean canRun = state.checkRequirements();
            if(canRun) {
                Microbot.log("First runnable state: " + state.getClass().getSimpleName());
                return state;
            }
        }
        return null;
    }

    public static void addState(State state) {
        initializedStates.add(state);
    }

    public static void queueStates(List<State> states) {
        nextStates = states;
        stateIterator = nextStates.listIterator();
    }

    public static void queueState(State state) {
        queueStates(Collections.singletonList(state));
    }


    public static void runOnLoopCycle() throws InterruptedException {
        if (StateManager.stopScript) {
            stopScript = true;
            return;
        }

        if(!stateIterator.hasNext()) {
            stopScript = true;
            return;
        }
        State nextState = stateIterator.next();
        if(!nextState.checkRequirements()) {
            StateManager.stopScript = true;
            Microbot.log("Cannot do state: " + nextState.getClass().getSimpleName());
            return;
        }
        for(int i = 0; i < nextState.retries(); i++) {
            if(nextState.handleState()) break;
            Microbot.log(String.format("Failed %s. Attempt (%d / %d)",
                    nextState.getClass().getSimpleName(),
                    i+1,
                    nextState.retries())
            );
        }
    }


}
