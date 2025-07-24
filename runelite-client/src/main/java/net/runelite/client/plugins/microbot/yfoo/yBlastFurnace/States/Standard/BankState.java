package net.runelite.client.plugins.microbot.yfoo.yBlastFurnace.States.Standard;

import net.runelite.api.GameObject;
import net.runelite.api.ItemID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.util.antiban.Rs2Antiban;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.inventory.Rs2ItemModel;
import net.runelite.client.plugins.microbot.util.misc.Rs2Potion;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.yfoo.GeneralUtil.ExtendableConditionalSleep;
import net.runelite.client.plugins.microbot.yfoo.GeneralUtil.HoverBoundsUtil;
import net.runelite.client.plugins.microbot.yfoo.GeneralUtil.RngUtil;
import net.runelite.client.plugins.microbot.yfoo.StateMachine.StateNode;
import net.runelite.client.plugins.microbot.yfoo.yBlastFurnace.BFScript;
import net.runelite.client.plugins.microbot.yfoo.yBlastFurnace.States.MicroActions.BankState.FillCoalBag;
import net.runelite.client.plugins.microbot.yfoo.MicroAction.MicroAction;
import net.runelite.client.plugins.microbot.yfoo.yBlastFurnace.States.MicroActions.BankState.WithdrawOre;

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BankState extends StateNode {

    enum RestockStates {
        OPEN_BANK, DEPOSIT_BARS, CHECK_RUN_ENERGY, WITHDRAW_ORE, COMPLETE
    }

    private static BankState instance;
    private static boolean tookMicroBreak = false;

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
        this.stateSteps.put(RestockStates.OPEN_BANK, () ->  {
            tookMicroBreak = false;
            if(Rs2Bank.isOpen()) {
                Microbot.log("Bank is already open");
                return true;
            }
            GameObject chest = Rs2GameObject.findBank();
            Rs2GameObject.interact(chest, "use");
            HoverBoundsUtil.hoverRandom();

            tookMicroBreak = Rs2Antiban.takeMicroBreakByChance();

            return ExtendableConditionalSleep.sleep(5000, () -> Rs2Bank.isOpen(), null, () -> Rs2Player.isMoving());
        });
        this.stateSteps.put(RestockStates.DEPOSIT_BARS, () -> {
            if(Rs2Inventory.onlyContains(ItemID.COAL_BAG_12019)) return true;
            if(Rs2Inventory.contains(item -> item.getName().contains("bar"))) {
                return Rs2Bank.depositAll(item -> item.getName().contains("bar"));
            }

            if(!tookMicroBreak) tookMicroBreak = Rs2Antiban.takeMicroBreakByChance();

            return true;
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
                if(!tookMicroBreak) tookMicroBreak = Rs2Antiban.takeMicroBreakByChance();
                return MicroAction.runActionsInRandomOrder(Arrays.asList(withdrawOre, fillCoalBag));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public boolean runStateSteps() throws InterruptedException {
        int coffer = Microbot.getVarbitValue(VarbitID.BLAST_FURNACE_COFFER);
        if(coffer < 25000) {
            RefillCofferState.getInstance().runStateSteps();
        }
        return super.runStateSteps();
    }

    @Override
    public StateNode nextState() {
        return DropOffRocksState.getInstance();
    }

    @Override
    public int retries() {
        return 3;
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

        Rs2ItemModel staminaPotionItem = Rs2Bank.bankItems().stream()
                .filter(rs2Item -> rs2Item.getName().toLowerCase().contains(Rs2Potion.getStaminaPotion().toLowerCase()))
                .min(Comparator.comparingInt(rs2Item -> getDoseFromName(rs2Item.getName())))
                .orElse(null);

        if (staminaPotionItem == null) {

            return false;
        }

        withdrawAndDrink(staminaPotionItem.getName());
        return script.sleepUntil(Rs2Player::hasStaminaActive);
    }

    private void withdrawAndDrink(String potionItemName) {
        String simplifiedPotionName = potionItemName.replaceAll("\\s*\\(\\d+\\)", "").trim();
        Rs2Bank.withdrawOne(potionItemName);
        Rs2Inventory.waitForInventoryChanges(1800);
        Rs2Inventory.interact(potionItemName, "drink");
        Rs2Inventory.waitForInventoryChanges(1800);
        if (Rs2Inventory.hasItem(simplifiedPotionName)) {
            Rs2Bank.depositOne(simplifiedPotionName);
            Rs2Inventory.waitForInventoryChanges(1800);
        }
        if (Rs2Inventory.hasItem(ItemID.VIAL)) {
            Rs2Bank.depositOne(ItemID.VIAL);
            Rs2Inventory.waitForInventoryChanges(1800);
        }
    }

    private int getDoseFromName(String potionItemName) {
        Pattern pattern = Pattern.compile("\\((\\d+)\\)$");
        Matcher matcher = pattern.matcher(potionItemName);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return 0;
    }
}
