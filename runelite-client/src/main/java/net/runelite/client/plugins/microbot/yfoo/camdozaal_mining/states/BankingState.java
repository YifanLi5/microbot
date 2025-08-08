package net.runelite.client.plugins.microbot.yfoo.camdozaal_mining.states;

import net.runelite.api.ItemID;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.coords.Rs2WorldArea;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.inventory.Rs2ItemModel;
import net.runelite.client.plugins.microbot.yfoo.GeneralUtil.CustomWalker;
import net.runelite.client.plugins.microbot.yfoo.StateMachine.StateNodeV2;
import net.runelite.client.plugins.microbot.yfoo.StateMachine.StateStepResult;

import java.util.LinkedHashMap;
import java.util.function.Predicate;

public class BankingState extends StateNodeV2<BankingState.BankingStateSteps> {

    private static int[] depositItems = {
            ItemID.BARRONITE_HEAD,
            ItemID.IMCANDO_HAMMER_BROKEN
    };
    private static int[] ancientJunk = {
            ItemID.ANCIENT_GLOBE,
            ItemID.ANCIENT_ASTROSCOPE,
            ItemID.ANCIENT_CARCANET,
            ItemID.ANCIENT_LEDGER,
            ItemID.ANCIENT_TREATISE
    };

    private static Predicate<Rs2ItemModel> keepPredicate = item -> item.getName().contains("pickaxe") || item.getId() == ItemID.HAMMER || item.getId() == ItemID.BARRONITE_SHARDS;

    private static WorldPoint bankLocation = new WorldPoint(2957, 5807, 0);

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
            if(!Rs2Inventory.contains(depositItems)) {
                Microbot.log("No items to deposit");
                return new StateStepResult<>(BankingStateSteps.COMPLETE, true);
            }
            return new StateStepResult<>(BankingStateSteps.OPEN_BANK, true);
        });
        this.stateSteps.put(BankingStateSteps.OPEN_BANK, () -> {
            CustomWalker.walkTo(bankLocation, 7);
            boolean openedBank = Rs2Bank.openBank();
            return new StateStepResult<>(BankingStateSteps.DEPOSIT_CRAP, openedBank);
        });
        this.stateSteps.put(BankingStateSteps.DEPOSIT_CRAP, () -> {
            Rs2Bank.depositAllExcept(keepPredicate);
            boolean depositedAll = Rs2Inventory.onlyContains(keepPredicate);
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
