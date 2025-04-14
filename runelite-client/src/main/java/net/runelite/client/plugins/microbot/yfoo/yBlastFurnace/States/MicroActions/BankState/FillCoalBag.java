package net.runelite.client.plugins.microbot.yfoo.yBlastFurnace.States.MicroActions.BankState;

import net.runelite.api.ItemID;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.yfoo.MicroAction.MicroAction;
import net.runelite.client.plugins.microbot.yfoo.StateMachine.StateManager;
import net.runelite.client.plugins.microbot.yfoo.yBlastFurnace.BFPlugin;
import net.runelite.client.plugins.microbot.yfoo.yBlastFurnace.Util.BFUtils;

public class FillCoalBag extends MicroAction {

    public FillCoalBag() {
        super(1, 1);
    }

    @Override
    public boolean doAction() {
        if(Rs2Bank.count(ItemID.COAL) < 27) {
            Microbot.log("Outta coal");
            StateManager.stopScript();
            return false;
        }

        return BFUtils.fillCoalBag();
    }
}
