package net.runelite.client.plugins.microbot.yfoo.camdozaal_mining.states;

import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.Global;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.yfoo.GeneralUtil.CustomWalker;
import net.runelite.client.plugins.microbot.yfoo.StateMachine.StateNodeV2;
import net.runelite.client.plugins.microbot.yfoo.StateMachine.StateStepResult;
import net.runelite.client.plugins.microbot.yfoo.camdozaal_mining.Constants;

import java.util.LinkedHashMap;

public class BankingState extends StateNodeV2<BankingState.BankingStateSteps> {
    private static BankingState instance;

    public static BankingState getInstance() {
        if(instance == null) {
            throw new NullPointerException(BankingState.class.getSimpleName() + " is null");
        }
        return instance;
    }

    public static BankingState initInstance(Script script) {
        if(instance == null)
            instance = new BankingState(script);
        return instance;
    }

    public enum BankingStateSteps {
        CHECK_INV, OPEN_BANK, DEPOSIT_CRAP, COMPLETE
    }

    public BankingState(Script script) {
        super(script);
    }

    @Override
    public boolean canRun() throws InterruptedException {
        return true;
    }

    @Override
    public void initStateSteps() {
        this.stateSteps = new LinkedHashMap<>();
        this.stateSteps.put(BankingStateSteps.CHECK_INV, () -> {
            if(Rs2Inventory.onlyContains(Constants.keepPredicate)) {
                Microbot.log("No items to deposit");
                return new StateStepResult<>(BankingStateSteps.COMPLETE, true);
            }
            return new StateStepResult<>(BankingStateSteps.OPEN_BANK, true);
        });
        this.stateSteps.put(BankingStateSteps.OPEN_BANK, () -> {
            CustomWalker.walkTo(Constants.bankLocation, 7);
            boolean openedBank = Rs2Bank.openBank();
            return new StateStepResult<>(BankingStateSteps.DEPOSIT_CRAP, openedBank);
        });
        this.stateSteps.put(BankingStateSteps.DEPOSIT_CRAP, () -> {
            Rs2Bank.depositAllExcept(Constants.keepPredicate);
            boolean depositedAll = Global.sleepUntil(() -> Rs2Inventory.onlyContains(Constants.keepPredicate));
            return new StateStepResult<>(BankingStateSteps.COMPLETE, depositedAll);
        });
        this.stateSteps.put(BankingStateSteps.COMPLETE, () -> {
            Microbot.log("COMPLETE");
            return new StateStepResult<>(BankingStateSteps.COMPLETE, true);
        });
    }

    @Override
    public StateNodeV2<?> nextState() throws InterruptedException {
        return MiningState.getInstance();
    }

    @Override
    public BankingStateSteps completionStep() {
        return BankingStateSteps.COMPLETE;
    }
}
