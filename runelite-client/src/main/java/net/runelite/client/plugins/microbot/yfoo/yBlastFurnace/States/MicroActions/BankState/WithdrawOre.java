package net.runelite.client.plugins.microbot.yfoo.yBlastFurnace.States.MicroActions.BankState;

import lombok.Setter;
import net.runelite.api.ItemID;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.inventory.Rs2ItemModel;
import net.runelite.client.plugins.microbot.yfoo.GeneralUtil.ExtendableConditionalSleep;
import net.runelite.client.plugins.microbot.yfoo.GeneralUtil.HoverBoundsUtil;
import net.runelite.client.plugins.microbot.yfoo.MicroAction.MicroAction;
import net.runelite.client.plugins.microbot.yfoo.yBlastFurnace.BFConfig;
import net.runelite.client.plugins.microbot.yfoo.yBlastFurnace.Util.BFUtils;

@Setter
public class WithdrawOre extends MicroAction {

    BFConfig config;

    public WithdrawOre(BFConfig config) {
        super(1, 2);
        this.config = config;
    }

    @Override
    public boolean doAction() {
        boolean isNotFull = ExtendableConditionalSleep.sleep(1000, () -> !Rs2Inventory.isFull(), null, null);
        if(!isNotFull) {
            Microbot.log("Inv Is full?");
            return true;
        }
        int oreId = BFUtils.shouldDoubleCoal(config.barType()) ? ItemID.COAL : config.barType().oreId;
        Rs2ItemModel withdrawItem = Rs2Bank.getBankItem(oreId);
        HoverBoundsUtil.addBankItemHoverHounds(withdrawItem);

        boolean withdrew = Rs2Bank.withdrawAll(oreId);
        Microbot.log(String.format("withdrew %d? (%s)", oreId, withdrew));
        return withdrew;
    }
}
