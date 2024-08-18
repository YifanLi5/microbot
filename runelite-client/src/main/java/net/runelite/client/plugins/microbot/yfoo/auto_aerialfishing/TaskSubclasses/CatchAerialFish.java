package net.runelite.client.plugins.microbot.yfoo.auto_aerialfishing.TaskSubclasses;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.yfoo.Task.Task;

import java.util.concurrent.atomic.AtomicBoolean;


@Slf4j
public class CatchAerialFish extends Task {

    private enum TaskState {
        FIND_FISHING_SPOT, INTERACT_FISHING_SPOT, WAIT_FOR_CATCH, SUCCESS
    }
    private NPC fishingSpot;

    private static CatchAerialFish instance;

    public static CatchAerialFish getInstance() {
        if(instance == null) {
            throw new NullPointerException("ExcavatePlantIgnite is null");
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
        Microbot.log(String.format("%s, %s", !Rs2Inventory.isFull(), Rs2Inventory.contains("Fish chunks", "King worm")));

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
                    Microbot.log("Fishing spot is null");
                    return runTaskRecursive(++failCount, state);
                }
                return runTaskRecursive(failCount, TaskState.INTERACT_FISHING_SPOT);

            case INTERACT_FISHING_SPOT:
                if (!Rs2Npc.interact(fishingSpot, "Catch")) {
                    Microbot.log("Failed interaction with fishing spot");
                    return runTaskRecursive(++failCount, state);
                }
                return runTaskRecursive(failCount, TaskState.WAIT_FOR_CATCH);

            case WAIT_FOR_CATCH:
                boolean sentBird = script.sleepUntil(() -> getProjectileByIdAndTarget(1632, fishingSpot.getLocalLocation()), 1200);
                if(!sentBird) {
                    Microbot.log("Did detect bird in flight.");
                    return runTaskRecursive(++failCount, TaskState.FIND_FISHING_SPOT);
                }

                Client rlClient = Microbot.getClient();
                int fishXpStart = rlClient.getSkillExperience(Skill.FISHING);
                boolean gotXp = script.sleepUntil(() -> rlClient.getSkillExperience(Skill.FISHING) > fishXpStart, 3000);
                Microbot.log("gotXp: " + gotXp);
                if(gotXp) {
                    return runTaskRecursive(failCount, TaskState.SUCCESS);
                }
                else return runTaskRecursive(++failCount, TaskState.FIND_FISHING_SPOT);

            case SUCCESS:
                Microbot.log("Got fish!");
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
