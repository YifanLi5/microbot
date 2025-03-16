package net.runelite.client.plugins.microbot.yfoo.yBlastFurnace.Util;

import lombok.Getter;
import lombok.Setter;
import net.runelite.api.ChatMessageType;
import net.runelite.api.ItemID;
import net.runelite.api.Varbits;
import net.runelite.api.events.ChatMessage;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.yfoo.GeneralUtil.ExtendableConditionalSleep;
import net.runelite.client.plugins.microbot.yfoo.GeneralUtil.RngUtil;

public class BFUtils {

    // 0 : filled, 32: not filled
    public static int COAL_BAG_IS_FILLED_VARBIT = 261;
    @Getter
    @Setter
    static boolean isCoalBagFilled = false;

    static int numCoal;
    static int numOre;

    public static boolean fillCoalBag() {
        if(!Rs2Inventory.contains(ItemID.COAL_BAG_12019)) {
            Microbot.log("Coal bag not in inventory.");
            return false;
        }

        boolean waitForCoalBagVarbit = ExtendableConditionalSleep.sleep(1000,
                () -> BFUtils.coalBagHasEmptyInBank(),
                null,
                null
        );
        if(!waitForCoalBagVarbit) {
            Microbot.log("Coal bag already full");
            isCoalBagFilled = true;
            return true;
        }
        if(Rs2Inventory.contains(ItemID.COAL_BAG_12019)) {
            Rs2Inventory.interact(ItemID.COAL_BAG_12019, "Fill");
            isCoalBagFilled = ExtendableConditionalSleep.sleep(4000,
                    () -> !BFUtils.coalBagHasEmptyInBank(),
                    null,
                    null
            );
        }
        return isCoalBagFilled;
    }

    public static boolean emptyCoalBag() {
        boolean emptied = false;
        if(Rs2Inventory.contains(ItemID.COAL_BAG_12019) && isCoalBagFilled) {
            Rs2Inventory.interact(ItemID.COAL_BAG_12019, "Empty");
            emptied = ExtendableConditionalSleep.sleep(2000, () -> Rs2Inventory.contains(ItemID.COAL), null, null);
            if(emptied) isCoalBagFilled = false;
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
        else if(canFurnaceProcessOre(barType)) return RngUtil.randomInclusive(0, 100) <= 30;
        else return true;
    }


    public static boolean finishedBlastingBars(BFBarRecipes barType) {
        boolean allOreProcessed = barType.getNumOreInFurnace() * Microbot.getVarbitValue(barType.coalNeeded) >= Microbot.getVarbitValue(Varbits.BLAST_FURNACE_COAL);
        boolean canCollect = Microbot.getVarbitValue(Varbits.BAR_DISPENSER) >= 2;
        return allOreProcessed && canCollect;
    }

    // ONLY WORKS WHILE IN BANK
    public static boolean coalBagHasEmptyInBank() {
        return Microbot.getVarbitPlayerValue(BFUtils.COAL_BAG_IS_FILLED_VARBIT) == 32;
    }
}
