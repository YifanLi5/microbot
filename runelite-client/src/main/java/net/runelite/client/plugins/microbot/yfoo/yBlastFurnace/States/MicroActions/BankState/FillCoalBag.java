package net.runelite.client.plugins.microbot.yfoo.yBlastFurnace.States.MicroActions.BankState;

import net.runelite.client.plugins.microbot.yfoo.MicroAction.MicroAction;
import net.runelite.client.plugins.microbot.yfoo.yBlastFurnace.Util.BFUtils;

public class FillCoalBag extends MicroAction {

    public FillCoalBag() {
        super(1, 1);
    }

    @Override
    public boolean doAction() {
        return BFUtils.fillCoalBag();
    }
}
