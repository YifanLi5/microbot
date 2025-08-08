package net.runelite.client.plugins.microbot.yfoo.yBlastFurnace.States.Hybrid;

import net.runelite.api.ItemID;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.inventory.Rs2ItemModel;
import net.runelite.client.plugins.microbot.util.misc.Rs2Potion;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.yfoo.GeneralUtil.HoverBoundsUtil;
import net.runelite.client.plugins.microbot.yfoo.GeneralUtil.RngUtil;
import net.runelite.client.plugins.microbot.yfoo.StateMachine.StateNode;
import net.runelite.client.plugins.microbot.yfoo.yBlastFurnace.BFConfig;
import net.runelite.client.plugins.microbot.yfoo.yBlastFurnace.Script;

import net.runelite.client.plugins.microbot.yfoo.MicroAction.MicroAction;
import net.runelite.client.plugins.microbot.yfoo.yBlastFurnace.States.MicroActions.BankState.FillCoalBag;
import net.runelite.client.plugins.microbot.yfoo.yBlastFurnace.States.MicroActions.BankState.WithdrawOre2;


import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BankState2 extends StateNode {

    enum RestockStates {
        OPEN_BANK, DEPOSIT_BARS, CHECK_RUN_ENERGY, WITHDRAW_ORE, COMPLETE
    }

    private static BankState2 instance;

    WithdrawOre2 withdrawOre;
    FillCoalBag fillCoalBag;

    public static BankState2 getInstance() {
        if(instance == null) {
            throw new NullPointerException(BankState2.class.getSimpleName() + " is null");
        }
        return instance;
    }

    public static BankState2 initInstance(Script script) {
        if (instance == null)
            instance = new BankState2(script);
        return instance;
    }

    public BankState2(Script script) {
        super(script);
        BFConfig config = script.config;
        this.withdrawOre = new WithdrawOre2(config);
        this.fillCoalBag = new FillCoalBag();
    }

    @Override
    public boolean canRun() throws InterruptedException {
        return true;
    }

    @Override
    public void initStateSteps() {
        this.stateSteps = new LinkedHashMap<>();
        this.stateSteps.put(RestockStates.OPEN_BANK, () ->  {
            if(Rs2Bank.isOpen()) {
                Microbot.log("Bank is already open");
                return true;
            }
            boolean result = Rs2Bank.openBank();
            HoverBoundsUtil.hoverRandom();
            script.sleep(RngUtil.gaussian(400, 100, 100, 600));
            return Rs2Bank.isOpen();
        });
        this.stateSteps.put(RestockStates.DEPOSIT_BARS, () -> {
            if(Rs2Inventory.onlyContains(ItemID.COAL_BAG_12019)) return true;
            if(Rs2Inventory.contains(item -> item.getName().contains("bar"))) {
                return Rs2Bank.depositAll(item -> item.getName().contains("bar"));
            }
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
                return MicroAction.runActionsInRandomOrder(Arrays.asList(withdrawOre, fillCoalBag));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        });
    }

    @Override
    public StateNode nextState() {
        return DropOffRocksState2.getInstance();
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
            Microbot.showMessage("Unable to find Stamina Potion but hasItem?");
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
