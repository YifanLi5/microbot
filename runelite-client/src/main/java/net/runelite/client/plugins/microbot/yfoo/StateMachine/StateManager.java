package net.runelite.client.plugins.microbot.yfoo.StateMachine;

import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.util.Global;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.yfoo.yBlastFurnace.States.StartingState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class StateManager {

    public static boolean stopScript = false;
    public final static boolean LOGOUT_ON_SCRIPT_STOP = true;

    static List<StateNode> initializedStates;
    static List<StateNode> nextStates;
    static Iterator<StateNode> stateIterator;

    public static void init() {
        initializedStates = new ArrayList<>();
        stopScript = false;
    }

    public static StateNode findFirstRunnableState() throws InterruptedException {
        for(StateNode state: initializedStates) {
            if(state instanceof StartingState) {
                continue;
            }
            boolean canRun = state.canRun();
            Microbot.log(String.format("%s canRun: %s", state.getClass().getSimpleName(), canRun));
            if(canRun) return state;
        }
        return null;
    }

    public static void addState(StateNode state) {
        initializedStates.add(state);
    }

    public static void queueStates(List<StateNode> states) {
        nextStates = states;
        stateIterator = nextStates.listIterator();
    }

    public static void queueState(StateNode state) {
        queueStates(Collections.singletonList(state));
    }


    public static void runOnLoopCycle() throws InterruptedException {
        if(!stateIterator.hasNext()) {
            stopScript();
            return;
        }
        StateNode nextState = stateIterator.next();
        if(!nextState.canRun()) {
            StateManager.stopScript = true;
            Microbot.log("Cannot do state: " + nextState.getClass().getSimpleName());
            return;
        }
        boolean succeededState = false;
        for(int i = 0; i < nextState.retries(); i++) {
            if (StateManager.stopScript) {
                stopScript = true;
                if(LOGOUT_ON_SCRIPT_STOP) {
                    Rs2Player.logout();
                }
                return;
            }

            if(nextState.handleState()) {
                succeededState = true;
                break;
            }
            Microbot.log(String.format("Failed %s. Attempt (%d / %d)",
                    nextState.getClass().getSimpleName(),
                    i+1,
                    nextState.retries())
            );
            Global.sleep(1000);
        }
        if(!succeededState) stopScript();
    }

    public static void stopScript() {
        Microbot.log("stopScript -> true");
        stopScript = true;
    }
}
