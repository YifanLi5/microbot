package net.runelite.client.plugins.microbot.yfoo.camdozaal_scripts.mining.TaskSubclasses;

import net.runelite.api.ItemID;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.yfoo.GeneralUtil.RngUtil;
import net.runelite.client.plugins.microbot.yfoo.Task.Task;
import net.runelite.client.plugins.microbot.yfoo.camdozaal_scripts.mining.CamdozaalMiningConfig;

public class DropBarroniteDeposits extends Task {

    private static DropBarroniteDeposits instance;

    public static DropBarroniteDeposits getInstance() {
        if(instance == null) {
            throw new NullPointerException("DropBarroniteDeposits is null");
        }
        return instance;
    }

    public static DropBarroniteDeposits initInstance(Script script, CamdozaalMiningConfig config) {
        instance = new DropBarroniteDeposits(script, config);
        return instance;
    }

    private CamdozaalMiningConfig config;
    private int minEmptySlotsToTrigger;

    private DropBarroniteDeposits(Script script, CamdozaalMiningConfig config) {
        super(script);
        this.config = config;
        this.minEmptySlotsToTrigger = RngUtil.randomInclusive(1, 2);
    }

    @Override
    public boolean shouldRun() throws InterruptedException {
        return config.dropBarroniteDeposits()
                && Rs2Inventory.getEmptySlots() <= minEmptySlotsToTrigger
                && Rs2Player.getAnimation() == -1
                && Rs2Inventory.contains(ItemID.BARRONITE_DEPOSIT);
    }

    @Override
    public boolean runTask() throws InterruptedException {
        return Rs2Inventory.dropAll(item -> item.getId() == ItemID.BARRONITE_DEPOSIT);
    }
}
