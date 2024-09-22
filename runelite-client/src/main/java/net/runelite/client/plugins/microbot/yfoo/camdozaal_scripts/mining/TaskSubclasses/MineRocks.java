package net.runelite.client.plugins.microbot.yfoo.camdozaal_scripts.mining.TaskSubclasses;

import net.runelite.api.ItemID;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.antiban.Rs2Antiban;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.keyboard.Rs2Keyboard;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.yfoo.GeneralUtil.AnimationUtil;
import net.runelite.client.plugins.microbot.yfoo.GeneralUtil.RngUtil;
import net.runelite.client.plugins.microbot.yfoo.Task.Task;
import net.runelite.client.plugins.microbot.yfoo.camdozaal_scripts.fishing.TaskSubclasses.CatchCamdozaalFish;

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
            Microbot.log("Player is already fishing... Sleeping until player stops fishing...");
            if(script.sleepUntil(() -> Rs2Player.getAnimation() == -1, 30_000)) {
                int additionalSleepTime = RngUtil.gaussian(4000, 2000, 0, 7000);
                Microbot.log(String.format("Sleeping for an additional %dms", additionalSleepTime));
                script.sleep(additionalSleepTime);
            }
            return true;
        }
        if(!Rs2GameObject.interact(new int[] {41547, 41548}, "Mine")) {
            Microbot.log("Failed interaction with Barronite Rocks");
            return false;
        }
        AnimationUtil.waitUntilPlayerStopsAnimating(4000);
        Rs2Antiban.moveMouseOffScreen();
        return true;
    }
}
