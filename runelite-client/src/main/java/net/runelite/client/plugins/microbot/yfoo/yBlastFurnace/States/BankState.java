package net.runelite.client.plugins.microbot.yfoo.yBlastFurnace.States;

import net.runelite.api.ItemID;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.util.antiban.Rs2Antiban;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.yfoo.StateMachine.StateNode;
import net.runelite.client.plugins.microbot.yfoo.yBlastFurnace.BFScript;
import net.runelite.client.plugins.microbot.yfoo.yBlastFurnace.States.MicroActions.BankState.FillCoalBag;
import net.runelite.client.plugins.microbot.yfoo.MicroAction.MicroAction;
import net.runelite.client.plugins.microbot.yfoo.yBlastFurnace.States.MicroActions.BankState.WithdrawOre;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class BankState extends StateNode {

    enum RestockStates {
        OPEN_BANK, DEPOSIT_BARS, CHECK_RUN_ENERGY, WITHDRAW_ORE, COMPLETE
    }

    private static BankState instance;

    WithdrawOre withdrawOre = new WithdrawOre(config);
    FillCoalBag fillCoalBag = new FillCoalBag();

    public static BankState getInstance() {
        if(instance == null) {
            throw new NullPointerException(BankState.class.getSimpleName() + " is null");
        }
        return instance;
    }

    public static BankState initInstance(BFScript script) {
        if (instance == null)
            instance = new BankState(script);
        return instance;
    }

    public BankState(BFScript script) {
        super(script);
    }

    @Override
    public boolean canRun() throws InterruptedException {
        return true;
    }

    @Override
    public void initStateSteps() {
        this.stateSteps = new LinkedHashMap<>();
        this.stateSteps.put(RestockStates.OPEN_BANK, () ->  Rs2Bank.isOpen() || Rs2Bank.openBank());
        this.stateSteps.put(RestockStates.DEPOSIT_BARS, () -> {
            if(Rs2Inventory.onlyContains(ItemID.COAL_BAG_12019)) return true;
            return Rs2Bank.depositAllExcept(ItemID.COAL_BAG_12019);
        });
        this.stateSteps.put(RestockStates.CHECK_RUN_ENERGY, () -> {
            if(!rollForRunRestore(20)) {
                Microbot.log("Skip stamina");
                return true;
            }
            return withdrawAndUseStamina();
        });
        this.stateSteps.put(RestockStates.WITHDRAW_ORE, () -> {
            try {
                return MicroAction.runActionsInRandomOrder(Arrays.asList(withdrawOre, fillCoalBag));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        });
    }

    @Override
    public StateNode nextState() {
        return DropOffRocksState.getInstance();
    }

    private static boolean rollForRunRestore(int minEnergy) {
        if (Rs2Player.hasStaminaActive()) {
            return false;
        }
        int currentEnergy = Rs2Player.getRunEnergy();
        if(currentEnergy >= 75) return false;
        else if (currentEnergy <= minEnergy) return true;

        // 3. Calculate probability based on how close currentEnergy is to minEnergy
        int energyDiff = currentEnergy - minEnergy;
        int probability = Math.max(10, 100 - (energyDiff * 2)); // Scale probability (min 10%, max 100%)

        return ThreadLocalRandom.current().nextInt(101) < probability;
    }

    private boolean withdrawAndUseStamina() {
        Rs2Bank.withdrawOne("Stamina potion");
        boolean gotStamina = script.sleepUntil(() -> Rs2Inventory.contains(item -> item.getName().contains("Stamina")));
        if(!gotStamina) {
            Microbot.log("didn't get stamina");
            return false;
        }
        Rs2Inventory.interact("Stamina potion", "Drink");
        boolean drankStamina = script.sleepUntil(() -> Rs2Player.hasStaminaActive());
        if(!drankStamina) Microbot.log("didn't drink stamina");

        return Rs2Bank.depositAll(item -> item.getName().contains("Stamina") || item.getId() == ItemID.VIAL);
    }
}
