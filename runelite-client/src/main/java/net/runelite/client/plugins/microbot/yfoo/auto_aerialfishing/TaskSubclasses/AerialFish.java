package net.runelite.client.plugins.microbot.yfoo.auto_aerialfishing.TaskSubclasses;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.InventoryID;
import net.runelite.api.NPC;
import net.runelite.api.Projectile;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.yfoo.Task.Task;

import java.util.concurrent.atomic.AtomicBoolean;


@Slf4j
public class AerialFish extends Task {

    private enum TaskState {
        FIND_FISHING_SPOT, INTERACT_FISHING_SPOT, WAIT_FOR_CATCH, SUCCESS
    }
    private NPC fishingSpot;

    private static AerialFish instance;

    public static AerialFish getInstance() {
        if(instance == null) {
            throw new NullPointerException("ExcavatePlantIgnite is null");
        }
        return instance;
    }

    public static AerialFish initInstance(Script script) {
        instance = new AerialFish(script);
        return instance;
    }

    private AerialFish(Script script) {
        super(script);
    }

    @Override
    public boolean shouldRun() throws InterruptedException {
        return !Rs2Inventory.isFull() && Rs2Inventory.contains("Fish chunks", "King worm");
    }

    @Override
    public boolean runTask() throws InterruptedException {
        fishingSpot = null;
        return runTaskRecursive(0, TaskState.FIND_FISHING_SPOT);
    }


    private boolean runTaskRecursive(int failCount, TaskState state) {
        if (failCount >= 3) return false;

        switch (state) {
            case FIND_FISHING_SPOT:
                fishingSpot = Rs2Npc.getNpc("Fishing spot");
                if (fishingSpot == null) {
                    log.warn("Fishing spot is null");
                    return runTaskRecursive(++failCount, state);
                }
                return runTaskRecursive(failCount, TaskState.INTERACT_FISHING_SPOT);

            case INTERACT_FISHING_SPOT:
                if (!Rs2Npc.interact(fishingSpot, "Catch")) {
                    log.warn("Failed interaction with fishing spot");
                    return runTaskRecursive(++failCount, state);
                }
                return runTaskRecursive(failCount, TaskState.WAIT_FOR_CATCH);

            case WAIT_FOR_CATCH:
                boolean sentBird = script.sleepUntil(() -> getProjectileByIdAndTarget(1632, fishingSpot.getLocalLocation()), 2000);
                if(!sentBird) {
                    log.warn("Bird was not detected");
                    return runTaskRecursive(++failCount, TaskState.FIND_FISHING_SPOT);
                }

                invChanged.set(false);
                boolean receivedCatch = script.sleepUntil(() -> invChanged.get(), 300, 2000);
                if(receivedCatch) {
                    return runTaskRecursive(failCount, TaskState.SUCCESS);
                }
                else return runTaskRecursive(++failCount, TaskState.FIND_FISHING_SPOT);

            case SUCCESS:
                return true;

            default:
                return false;
        }
    }

    AtomicBoolean invChanged = new AtomicBoolean();


    public void relayItemContainerChangeEvent(ItemContainerChanged event) {
        if (event.getContainerId() == InventoryID.INVENTORY.getId()) {
            invChanged.set(true);
        }
    }

    private boolean getProjectileByIdAndTarget(int id, LocalPoint target) {
        for (Projectile projectile : Microbot.getClient().getProjectiles()) {
            if (projectile.getId() == id && projectile.getTarget().equals(target)) {
                return true;
            }
        }
        return false;
    }


}
