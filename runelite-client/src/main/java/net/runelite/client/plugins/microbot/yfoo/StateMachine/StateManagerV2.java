package net.runelite.client.plugins.microbot.yfoo.StateMachine;

import lombok.Setter;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.util.Global;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;

import java.util.ArrayList;
import java.util.List;

public class StateManagerV2 {

    public static boolean stopScript = false;
    public final static boolean LOGOUT_ON_SCRIPT_STOP = true;

    static List<StateNodeV2<?>> initializedStates;
    @Setter
    static StateNodeV2<?> nextState;


    public static void init() {
        initializedStates = new ArrayList<>();
        stopScript = false;
    }

    public static void addState(StateNodeV2<?> state) {
        initializedStates.add(state);
    }

    public static void runLoop() throws InterruptedException {
        if(nextState == null) {
            Microbot.log("No next state");
            stopScript();
            return;
        }
        if(!nextState.canRun()) {
            stopScript = true;
            Microbot.log("Cannot do state: " + nextState.getClass().getSimpleName());
            return;
        }
        boolean succeededState = false;
        for(int i = 0; i < nextState.retries(); i++) {
            if (stopScript) {
                if(LOGOUT_ON_SCRIPT_STOP) {
                    Rs2Player.logout();
                }
                return;
            }
            if(!nextState.canRun()) {
                Microbot.log("%s failed canRun() check.", nextState.getClass().getSimpleName());
                Global.sleep(1000);
                continue;
            }
            Microbot.log("Running State: %s", nextState.getClass().getSimpleName());
            if(nextState.handleState()) {
                succeededState = true;
                break;
            } else {
                Microbot.log(String.format("Failed %s. Attempt (%d / %d)",
                        nextState.getClass().getSimpleName(),
                        i+1,
                        nextState.retries())
                );
                Global.sleep(1000);
            }

        }
        if(!succeededState) stopScript();
    }

    public static void stopScript() {
        Microbot.log("stopScript -> true");
        stopScript = true;
    }
}
