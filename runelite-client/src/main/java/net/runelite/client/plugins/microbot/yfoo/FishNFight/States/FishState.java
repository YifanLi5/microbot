package net.runelite.client.plugins.microbot.yfoo.FishNFight.States;

import net.runelite.api.ItemID;
import net.runelite.api.NPC;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.yfoo.GeneralUtil.AnimationUtil;
import net.runelite.client.plugins.microbot.yfoo.StateMachine.State;

public class FishState extends State {

    public FishState(Script script) {
        super(script);
    }

    @Override
    public boolean checkRequirements() throws InterruptedException {
        return Rs2Inventory.contains(ItemID.SMALL_FISHING_NET)
                && Rs2Inventory.emptySlotCount() > 0
                && Rs2Npc.getNpc(1530) != null;
    }

    @Override
    public boolean handleState() throws InterruptedException {
        NPC fishingSpot = Rs2Npc.getNpc(1530);
        if(fishingSpot == null) {
            Microbot.log("Fishing spot not found.");
            return false;
        }
        Rs2Npc.interact(fishingSpot);
        script.sleepUntil(Rs2Player::isAnimating);
        AnimationUtil.waitUntilPlayerStopsAnimating(2000);
        return false;
    }
}
