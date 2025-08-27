package net.runelite.client.plugins.microbot.yfoo.camdozaal_mining.states;

import net.runelite.api.ItemID;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.yfoo.StateMachine.StateNodeV2;
import net.runelite.client.plugins.microbot.yfoo.StateMachine.StateStepResult;

import java.util.LinkedHashMap;


public class DropState extends StateNodeV2<DropState.DropStateSteps> {

    private static DropState instance;

    public DropState(Script script) {
        super(script);
    }

    public static DropState getInstance() {
        if(instance == null) {
            throw new NullPointerException(DropState.class.getSimpleName() + " is null");
        }
        return instance;
    }

    public static DropState initInstance(Script script) {
        if(instance == null)
            instance = new DropState(script);
        return instance;
    }

    public enum DropStateSteps {
        DROP_DEPOSITS, COMPLETE
    }

    @Override
    public boolean canRun() throws InterruptedException {
        return Rs2Inventory.contains(ItemID.BARRONITE_DEPOSIT);
    }

    @Override
    public void initStateSteps() {
        this.stateSteps = new LinkedHashMap<>();
        this.stateSteps.put(DropStateSteps.DROP_DEPOSITS, () -> {
            if(!Rs2Inventory.dropAll(ItemID.BARRONITE_DEPOSIT)) {
                return new StateStepResult<>(DropStateSteps.DROP_DEPOSITS, true);
            }
            return new StateStepResult<>(DropStateSteps.COMPLETE, true);
        });

        this.stateSteps.put(DropStateSteps.COMPLETE, () -> {
            Microbot.log("COMPLETE");
            return new StateStepResult<>(DropStateSteps.COMPLETE, true);
        });
    }

    @Override
    public StateNodeV2<?> nextState() throws InterruptedException {
        return MiningState.getInstance();
    }

    @Override
    public Enum<DropStateSteps> completionStep() {
        return DropStateSteps.COMPLETE;
    }
}
