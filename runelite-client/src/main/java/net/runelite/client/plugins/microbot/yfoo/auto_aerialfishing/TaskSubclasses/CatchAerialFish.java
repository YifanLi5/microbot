package net.runelite.client.plugins.microbot.yfoo.auto_aerialfishing.TaskSubclasses;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.equipment.Rs2Equipment;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Item;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.yfoo.GeneralUtil.RngUtil;
import net.runelite.client.plugins.microbot.yfoo.Task.Task;

import java.util.concurrent.atomic.AtomicBoolean;

import static net.runelite.client.plugins.microbot.yfoo.auto_aerialfishing.Util.Constants.AERIAL_FISH;


@Slf4j
public class CatchAerialFish extends Task {

    private enum TaskState {
        FIND_FISHING_SPOT, INTERACT_FISHING_SPOT, WAIT_FOR_CATCH, SUCCESS
    }
    private NPC fishingSpot;

    private static CatchAerialFish instance;

    public static CatchAerialFish getInstance() {
        if(instance == null) {
            throw new NullPointerException("CatchAerialFish is null");
        }
        return instance;
    }

    public static CatchAerialFish initInstance(Script script) {
        instance = new CatchAerialFish(script);
        return instance;
    }

    private CatchAerialFish(Script script) {
        super(script);
    }

    @Override
    public boolean shouldRun() throws InterruptedException {
        Rs2Item equippedWeapon = Rs2Equipment.get(EquipmentInventorySlot.WEAPON);
        int weaponId = equippedWeapon != null ? equippedWeapon.getId() : -1;
        return !Rs2Inventory.isFull() && Rs2Inventory.contains(ItemID.FISH_CHUNKS, ItemID.KING_WORM) && weaponId == 22817;
    }

    @Override
    public boolean runTask() throws InterruptedException {
        fishingSpot = null;
        return runTaskIterative();
    }

    private boolean runTaskIterative() {
        if(Rs2Inventory.isFull()) return true;

        TaskState state = TaskState.FIND_FISHING_SPOT;
        int failCount = 0;
        boolean result = false;
        while (!result && failCount < 3) {
            if(failCount > 0) {
                Microbot.log(String.format("Debug: %s, %s", result, failCount));
            }
            boolean deselected = !Rs2Inventory.isItemSelected() || Rs2Inventory.deselect();
            if(!deselected) {
                Microbot.log("Failed to deselect");
                script.sleep(600);
                failCount++;
                continue;
            }
            switch (state) {
                case FIND_FISHING_SPOT:
                    //Microbot.log("Finding fishing spot...");
                    fishingSpot = Rs2Npc.getNpc("Fishing spot");
                    if (fishingSpot == null) {
                        Microbot.log("Fishing spot is null");
                        failCount++;
                        continue;
                    }
                    state = TaskState.INTERACT_FISHING_SPOT;
                    break;

                case INTERACT_FISHING_SPOT:
                    //Microbot.log("Interacting with fishing spot...");
                    if (!Rs2Npc.interact(fishingSpot, "Catch")) {
                        Microbot.log("Failed interaction with fishing spot");
                        failCount++;
                        state = TaskState.FIND_FISHING_SPOT;
                        continue;
                    }

                    boolean sentBird = script.sleepUntil(() -> !isBirdOnGlove(), 1200);

                    if (!sentBird) {
                        Microbot.log("Did detect bird in flight.");
                        failCount++;
                        state = TaskState.FIND_FISHING_SPOT;
                        continue;
                    }
                    state = TaskState.WAIT_FOR_CATCH;
                    break;

                case WAIT_FOR_CATCH:
                    //Microbot.log("Waiting for catch...");
                    boolean birdIsBack = script.sleepUntil(this::isBirdOnGlove, 4000);
                    if (!birdIsBack) {
                        Microbot.log("Bird did not come back???");
                        failCount++;
                        state = TaskState.FIND_FISHING_SPOT;
                    } else state = TaskState.SUCCESS;
                    break;

                case SUCCESS:
                    //Microbot.log("Got fish!");
                    result = true;
                    break;
            }
        }
        return result;
    }

    // When the bird is sent out. Glove's Id -> 22816. When it returns, Glove's Id -> 22817.
    private boolean isBirdOnGlove() {
        Rs2Item equippedWeapon = Rs2Equipment.get(EquipmentInventorySlot.WEAPON);
        int weaponId = equippedWeapon != null ? equippedWeapon.getId() : -1;
        if(weaponId == ItemID.CORMORANTS_GLOVE_22817) return true;
        else if (weaponId == ItemID.CORMORANTS_GLOVE) return false;
        else {
            script.shutdown();
            throw new RuntimeException("Error: Equipped weapon is not the Cormorant Glove (weaponId != 22817 or 22816).");
        }
    }

}
