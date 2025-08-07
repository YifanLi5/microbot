package net.runelite.client.plugins.microbot.yfoo.yBlastFurnace.Util;

import lombok.Getter;
import lombok.Setter;
import net.runelite.api.ItemID;
import net.runelite.api.Varbits;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.inventory.Rs2ItemModel;
import net.runelite.client.plugins.microbot.yfoo.GeneralUtil.ExtendableConditionalSleep;
import net.runelite.client.plugins.microbot.yfoo.GeneralUtil.HoverBoundsUtil;
import net.runelite.client.plugins.microbot.yfoo.GeneralUtil.RngUtil;
import net.runelite.client.plugins.microbot.yfoo.StateMachine.StateManager;

public class BFUtils {

    // 0 : filled, 32: not filled
    public static int COAL_BAG_IS_FILLED_VARBIT = 261;

    @Getter
    @Setter
    static int numCoalInBag = 0;


    public static boolean fillCoalBag() {
        boolean waitForCoalBagVarbit = ExtendableConditionalSleep.sleep(1000,
                () -> BFUtils.coalBagHasEmptyInBank(),
                null,
                null
        );
        if(!waitForCoalBagVarbit) {
            Microbot.log("Coal bag doesn't have fill option. Empty then retry.");
            Rs2Inventory.interact(ItemID.COAL_BAG_12019, "Empty");

            waitForCoalBagVarbit = ExtendableConditionalSleep.sleep(1000,
                    () -> !BFUtils.coalBagHasEmptyInBank(),
                    null,
                    null
            );
            if (!waitForCoalBagVarbit) {
                Microbot.log("coal bag varbit did not change to empty state.");
                return false;
            }

            return fillHelper();

        }
        return fillHelper();
    }

    private static boolean fillHelper() {
        if(!Rs2Bank.isOpen()) {
            Microbot.log("Bank is not open.");
            return false;
        }

        if(!Rs2Inventory.contains(ItemID.COAL_BAG_12019)) {
            Microbot.log("Coal bag not in inventory.");
            return false;
        }

        if(Rs2Bank.count(ItemID.COAL) < 27) {
            StateManager.stopScript();
            Microbot.log("Outta coal");
            return false;
        }

        Rs2ItemModel coalBag = Rs2Inventory.get(ItemID.COAL_BAG_12019);
        HoverBoundsUtil.addInventoryItemHoverBounds(coalBag);
        Rs2Inventory.interact(coalBag, "Fill");
        return ExtendableConditionalSleep.sleep(4000,
                () -> !BFUtils.coalBagHasEmptyInBank(),
                null,
                null
        );
    }

    public static boolean emptyCoalBag() {
        if(numCoalInBag <= 0) return true;
        boolean emptied = false;
        if(Rs2Inventory.contains(ItemID.COAL_BAG_12019)) {
            Rs2Inventory.interact(ItemID.COAL_BAG_12019, "Empty");
            emptied = ExtendableConditionalSleep.sleep(2000, () -> Rs2Inventory.contains(ItemID.COAL), null, null);
        }
        return emptied;
    }

    public static boolean canFurnaceProcessOre(BFBarRecipes barType) {
        int availableCoal = Microbot.getVarbitValue(Varbits.BLAST_FURNACE_COAL);
        int coalNeeded = Rs2Inventory.getEmptySlots() * barType.coalNeeded;
        Microbot.log(String.format("availableCoal: %d CoalNeeded: %d", availableCoal, coalNeeded));
        return availableCoal >= coalNeeded;
    }

    public static boolean shouldDoubleCoal(BFBarRecipes barType) {
        if(barType == BFBarRecipes.STEEL_BAR) return false;
        // 54 -> 27 inv slots + 27 coal bag
        int coalAfter = Microbot.getVarbitValue(Varbits.BLAST_FURNACE_COAL) + 54;
        if(coalAfter > 200) return false;
        else if(canFurnaceProcessOre(barType)) return rollForCoal(coalAfter, barType.coalRequired());
        else return true;
    }

    public static boolean rollForCoal(int coalAfter, int minCoalNeeded) {
        int normalizedVal = normalize(coalAfter, minCoalNeeded, 250, 0, 100);
        int roll = RngUtil.randomInclusive(1, 100);
        Microbot.log(String.format("normalizedVal: %d || randomRoll: %d. DoubleCoal: %s", normalizedVal, roll, roll <= normalizedVal));
        return roll <= normalizedVal;
    }

    public static int normalize(double x, double oldMin, double oldMax, double newMin, double newMax) {
        return (int) (((x - oldMin) / (oldMax - oldMin)) * (newMax - newMin) + newMin);
    }


    public static boolean finishedBlastingBars(BFBarRecipes barType) {
        boolean allOreProcessed = barType.getNumOreInFurnace() * Microbot.getVarbitValue(barType.coalNeeded) >= Microbot.getVarbitValue(Varbits.BLAST_FURNACE_COAL);
        boolean canCollect = Microbot.getVarbitValue(Varbits.BAR_DISPENSER) >= 2;
        return allOreProcessed && canCollect;
    }

    // ONLY WORKS WHILE IN BANK
    private static boolean coalBagHasEmptyInBank() {
        if(!Rs2Bank.isOpen()) {
            Microbot.log("Bank must be open for coalbag varbit to change.");
            return false;
        }
        return Microbot.getVarbitPlayerValue(BFUtils.COAL_BAG_IS_FILLED_VARBIT) == 32;
    }

    public static void main(String[] args) {
        rollForCoal(200, 108);
    }
}
