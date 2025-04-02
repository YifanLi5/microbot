package net.runelite.client.plugins.microbot.yfoo.yBlastFurnace.States.MicroActions.BankState;

import net.runelite.api.ItemID;
import net.runelite.api.Varbits;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.inventory.Rs2ItemModel;
import net.runelite.client.plugins.microbot.yfoo.GeneralUtil.ExtendableConditionalSleep;
import net.runelite.client.plugins.microbot.yfoo.GeneralUtil.HoverBoundsUtil;
import net.runelite.client.plugins.microbot.yfoo.MicroAction.MicroAction;
import net.runelite.client.plugins.microbot.yfoo.StateMachine.StateManager;
import net.runelite.client.plugins.microbot.yfoo.yBlastFurnace.BFConfig;
import net.runelite.client.plugins.microbot.yfoo.yBlastFurnace.Util.BFUtils;

public class WithdrawOre2 extends MicroAction {
    BFConfig config;

    public WithdrawOre2(BFConfig config) {
        super(1, 2);
        this.config = config;
    }

    @Override
    public boolean doAction() {
        boolean assertNotFull = ExtendableConditionalSleep.sleep(1000, () -> !Rs2Inventory.isFull(), null, null);
        if(!assertNotFull) {
            Microbot.log("Inv Is full?");
            return true;
        }
        int oreId = BFUtils.canFurnaceProcessOre(config.barType())
                && BFUtils.rollForCoal(Microbot.getVarbitValue(Varbits.BLAST_FURNACE_COAL) + 26, config.barType().coalNeeded)
                ? config.barType().oreId : ItemID.GOLD_ORE;
        if(Rs2Bank.count(oreId) < 26) {
            Microbot.log("Outta ore for " + config.barType());
            StateManager.stopScript();
            return false;
        }
        Rs2ItemModel withdrawItem = Rs2Bank.getBankItem(oreId);
        HoverBoundsUtil.addBankItemHoverHounds(withdrawItem);

        boolean withdrew = Rs2Bank.withdrawAll(oreId);
        Microbot.log(String.format("withdrew %d? (%s)", oreId, withdrew));
        return withdrew;
    }
}
