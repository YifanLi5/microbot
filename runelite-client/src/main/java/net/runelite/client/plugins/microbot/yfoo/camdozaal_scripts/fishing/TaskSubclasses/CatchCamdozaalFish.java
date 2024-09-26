package net.runelite.client.plugins.microbot.yfoo.camdozaal_scripts.fishing.TaskSubclasses;

import net.runelite.api.ItemID;
import net.runelite.api.NPC;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.yfoo.GeneralUtil.GetInteractableUtil;
import net.runelite.client.plugins.microbot.yfoo.GeneralUtil.RngUtil;
import net.runelite.client.plugins.microbot.yfoo.Task.Task;

public class CatchCamdozaalFish extends Task {

    private static CatchCamdozaalFish instance;

    public static CatchCamdozaalFish getInstance() {
        if(instance == null) {
            throw new NullPointerException("CatchAerialFish is null");
        }
        return instance;
    }

    public static CatchCamdozaalFish initInstance(Script script) {
        instance = new CatchCamdozaalFish(script);
        return instance;
    }

    private CatchCamdozaalFish(Script script) {
        super(script);
    }

    @Override
    public boolean shouldRun() throws InterruptedException {
        return !Rs2Inventory.isFull() && Rs2Inventory.contains(rs2Item ->
                rs2Item.id == ItemID.SMALL_FISHING_NET || rs2Item.id == ItemID.BIG_FISHING_NET
        );
    }

    @Override
    public boolean runTask() throws InterruptedException { // 10686 fishing spot
        if(Rs2Player.getAnimation() != -1) {
            Microbot.log("Player is already fishing... Sleeping until player stops fishing...");
            if(script.sleepUntil(() -> Rs2Player.getAnimation() == -1, 30_000)) {
                int additionalSleepTime = RngUtil.gaussian(4000, 2000, 0, 7000);
                Microbot.log(String.format("Sleeping for an additional %dms", additionalSleepTime));
                script.sleep(additionalSleepTime);
            }
            return true;
        }
        NPC fishingSpot = GetInteractableUtil.getRandomNPC(10686);
        if(!Rs2Npc.interact(fishingSpot)) {
            Microbot.log("Failed to interact with fishing spot.");
            return false;
        }
        if(!script.sleepUntil(() -> Rs2Player.getAnimation() != -1)) {
            Microbot.log("Did not start animation after interacting with fishing spot");
            return false;
        }
        return true;
    }
}
