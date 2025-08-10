package net.runelite.client.plugins.microbot.yfoo.camdozaal_mining.states;

import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.equipment.Rs2Equipment;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.yfoo.StateMachine.StateNodeV2;
import net.runelite.client.plugins.microbot.yfoo.camdozaal_mining.Constants;

import java.util.LinkedHashMap;

public class StartingState extends StateNodeV2<StartingState.Steps> {

    private static StartingState instance;
    public static StartingState getInstance() {
        if(instance == null) {
            throw new NullPointerException(StartingState.class.getSimpleName() + " is null");
        }
        return instance;
    }

    public static StartingState initInstance(Script script) {
        if(instance == null)
            instance = new StartingState(script);
        return instance;
    }

    public enum Steps {
        COMPLETE
    }

    public StartingState(Script script) {
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
    public boolean runStateSteps() throws InterruptedException {
        return true;
    }

    @Override
    public StateNodeV2<?> nextState() throws InterruptedException {
        boolean hasPickaxe = Rs2Equipment.isWearing(item -> item.getName().contains("pickaxe"))
                || Rs2Inventory.contains(item -> item.getName().contains("pickaxe"));
        if(!Rs2Inventory.onlyContains(Constants.keepPredicate) && Rs2Inventory.isFull()) {
            return BankingState.getInstance();
        }
        else if(Rs2Inventory.isFull()) {
            return SmashingState.getInstance();
        } else {
            return MiningState.getInstance();
        }
    }

    @Override
    public Steps completionStep() {
        return Steps.COMPLETE;
    }
}
