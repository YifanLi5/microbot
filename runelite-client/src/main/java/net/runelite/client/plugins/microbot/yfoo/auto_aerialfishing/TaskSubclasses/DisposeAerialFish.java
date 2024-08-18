package net.runelite.client.plugins.microbot.yfoo.auto_aerialfishing.TaskSubclasses;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ItemID;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.yfoo.Task.Task;

import java.util.Arrays;
import java.util.List;

@Slf4j
public class DisposeAerialFish extends Task {

    // Todo: Roll between rapid use knife or single use and wait.
    // If single use, afk a bit afterwards

    private static final List<String> aerialFishCatches = Arrays.asList("Bluegill", "Common tench", "Mottled eel", "Greater siren");

    private static DisposeAerialFish instance;

    public static DisposeAerialFish getInstance() {
        if(instance == null) {
            throw new NullPointerException("ExcavatePlantIgnite is null");
        }
        return instance;
    }

    public static DisposeAerialFish initInstance(Script script) {
        instance = new DisposeAerialFish(script);
        return instance;
    }

    private DisposeAerialFish(Script script) {
        super(script);
    }

    @Override
    public boolean shouldRun() throws InterruptedException {
        return Rs2Inventory.contains(aerialFishCatches.toArray(new String[0]))
                && Rs2Inventory.hasItem(ItemID.KNIFE)
                && Rs2Inventory.isFull();
    }

    @Override
    public boolean runTask() throws InterruptedException {

        Rs2Inventory.combineClosest(rs2Item -> aerialFishCatches.contains(rs2Item.getName()), rs2Item -> rs2Item.getName().equals("Knife"));

        boolean gainedXp = script.sleepUntil(() -> Microbot.isGainingExp);
        if(!gainedXp) {
            Microbot.log("Did not gain xp after combining knife -> AerialFish");
            return false;
        }

        script.sleepUntil(() -> !Microbot.isGainingExp, 40000);

        return true;
    }
}
