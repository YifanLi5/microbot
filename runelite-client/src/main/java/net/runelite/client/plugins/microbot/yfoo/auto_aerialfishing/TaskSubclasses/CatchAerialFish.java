package net.runelite.client.plugins.microbot.yfoo.auto_aerialfishing.TaskSubclasses;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.equipment.Rs2Equipment;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.inventory.Rs2ItemModel;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.yfoo.Task.Task;
import net.runelite.client.plugins.microbot.yfoo.auto_aerialfishing.AerialFishingOverlay;

import java.util.concurrent.*;


@Slf4j
public class CatchAerialFish extends Task {

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

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private CatchAerialFish(Script script) {
        super(script);
    }

    @Override
    public boolean shouldRun() throws InterruptedException {
        Rs2ItemModel equippedWeapon = Rs2Equipment.get(EquipmentInventorySlot.WEAPON);
        int weaponId = equippedWeapon != null ? equippedWeapon.getId() : -1;
        return !Rs2Inventory.isFull() && Rs2Inventory.contains(ItemID.FISH_CHUNKS, ItemID.KING_WORM) && weaponId == 22817;
    }

    @Override
    public boolean runTask() throws InterruptedException {
        return runTaskWithTimeout();
    }

    public boolean runTaskWithTimeout() throws InterruptedException {
        Callable<Boolean> task = this::runTask2;
        Future<Boolean> future = executor.submit(task);

        try {
            // Wait for the task to complete with a timeout of 10 seconds
            return future.get(10, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            Microbot.log("Task exceeded timeout of 10s.");
            future.cancel(true); // Cancel the task if timeout occurs
            return false;
        } catch (ExecutionException e) {
            Microbot.log("Task threw ExecutionException.");
            return false;
        }
    }

    private boolean runTask2() {
        boolean deselected = !Rs2Inventory.isItemSelected() || Rs2Inventory.deselect();
        if(!deselected) {
            Microbot.log("Failed to deselect");
            return false;
        }

        NPC fishingSpot = Rs2Npc.getNpc("Fishing spot");
        if (fishingSpot == null) {
            Microbot.log("Fishing spot is null");
            return false;
        }

        if (!Rs2Npc.interact(fishingSpot, "Catch")) {
            Microbot.log("Failed interaction with fishing spot");
            return false;
        }

        boolean sentBird = script.sleepUntil(() -> !isBirdOnGlove(), 1200);
        if (!sentBird) {
            Microbot.log("Did detect bird in flight.");
            return false;
        }
        boolean birdIsBack = script.sleepUntil(this::isBirdOnGlove, 4000);
        if (!birdIsBack) {
            Microbot.log("Bird did not come back???");
            return false;
        }
        AerialFishingOverlay.incrementNumCatches();
        return true;
    }

    // When the bird is sent out, the equipped weapon changes.
    // Glove's Id -> 22816. When it returns, Glove's Id -> 22817.
    private boolean isBirdOnGlove() {
        Rs2ItemModel equippedWeapon = Rs2Equipment.get(EquipmentInventorySlot.WEAPON);
        int weaponId = equippedWeapon != null ? equippedWeapon.getId() : -1;
        if(weaponId == ItemID.CORMORANTS_GLOVE_22817) return true;
        else if (weaponId == ItemID.CORMORANTS_GLOVE) return false;
        else {
            script.shutdown();
            throw new RuntimeException("Error: Equipped weapon is not the Cormorant Glove (weaponId != 22817 or 22816).");
        }
    }

}
