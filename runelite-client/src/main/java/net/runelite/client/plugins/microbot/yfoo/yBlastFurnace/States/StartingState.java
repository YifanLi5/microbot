package net.runelite.client.plugins.microbot.yfoo.yBlastFurnace.States;

import net.runelite.api.gameval.VarbitID;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.yfoo.StateMachine.StateNode;
import net.runelite.client.plugins.microbot.yfoo.StateMachine.StateManager;
import net.runelite.client.plugins.microbot.yfoo.yBlastFurnace.BFScript;
import net.runelite.client.plugins.microbot.yfoo.yBlastFurnace.States.Standard.RefillCofferState;

import java.util.LinkedHashMap;

public class StartingState extends StateNode {
    private static StartingState instance;

    public static StartingState getInstance() {
        if(instance == null) {
            throw new NullPointerException(StartingState.class.getSimpleName() + " is null");
        }
        return instance;
    }

    public static StartingState initInstance(BFScript script) {
        if(instance == null)
            instance = new StartingState(script);
        return instance;
    }

    public StartingState(BFScript script) {
        super(script);
    }

    @Override
    public boolean canRun() throws InterruptedException {
        return true;
    }

    @Override
    public void initStateSteps() {
        this.stateSteps = new LinkedHashMap<>();
    }

    @Override
    public StateNode nextState() throws InterruptedException {
        if(Microbot.getVarbitValue(VarbitID.BLAST_FURNACE_COFFER) < 50000) {
            return RefillCofferState.getInstance();
        }
        StateNode state = StateManager.findFirstRunnableState();
        if(state == null) {
            Microbot.log("Unable to find starting state.");
            return null;
        }
        Microbot.log("Starting with state: " + state.getClass().getSimpleName());
        return state;
    }

}
