package net.runelite.client.plugins.microbot.yfoo.camdozaal_scripts.mining.TaskSubclasses;

import net.runelite.api.WallObject;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.antiban.Rs2Antiban;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.yfoo.GeneralUtil.AnimationUtil;
import net.runelite.client.plugins.microbot.yfoo.GeneralUtil.GetInteractableUtil;
import net.runelite.client.plugins.microbot.yfoo.GeneralUtil.InteractionUtil;
import net.runelite.client.plugins.microbot.yfoo.GeneralUtil.RngUtil;
import net.runelite.client.plugins.microbot.yfoo.Task.Task;

import java.util.List;
import java.util.stream.Collectors;

public class MineRocks extends Task {

    private static MineRocks instance;

    public static MineRocks getInstance() {
        if(instance == null) {
            throw new NullPointerException("CatchAerialFish is null");
        }
        return instance;
    }

    public static MineRocks initInstance(Script script) {
        instance = new MineRocks(script);
        return instance;
    }

    private MineRocks(Script script) {
        super(script);
    }


    @Override
    public boolean shouldRun() throws InterruptedException {
        return !Rs2Inventory.isFull();
    }

    @Override
    public boolean runTask() throws InterruptedException {
        if(Rs2Player.getAnimation() != -1) {
            Microbot.log("Player is already mining... Sleeping until player stops mining...");
            AnimationUtil.waitUntilPlayerStopsAnimating(3000);
            if(!Rs2Player.isAnimating()) {
                int additionalSleepTime = RngUtil.gaussian(1500, 750, 0, 3500);
                Microbot.log(String.format("Sleeping for an additional %dms", additionalSleepTime));
                script.sleep(additionalSleepTime);
            }
            return true;
        }
        List<WallObject> barroniteRocks = GetInteractableUtil.getWallObjects(
                object -> (object.getId() == 41547 || object.getId() == 41548)
                        && object.getWorldLocation().distanceTo(Rs2Player.getWorldLocation()) <= 7
        );
        if(barroniteRocks.isEmpty()) {
            Microbot.log("Error did not find any Barronite Rocks within 7 tiles.");
            barroniteRocks = GetInteractableUtil.getWallObjects(
                    object -> (object.getId() == 41547 || object.getId() == 41548)
            );
            if(barroniteRocks.isEmpty()) {
                Microbot.log("Error did not find any Barronite Rocks whatsoever");
                return false;
            }
        }

        if(!Rs2GameObject.interact(barroniteRocks.get(RngUtil.randomInclusive(0, barroniteRocks.size()-1)))){
            Microbot.log("Failed interaction with Barronite Rocks");
            return false;
        }
        script.sleepUntil(Rs2Player::isAnimating);
        return true;
    }
}
