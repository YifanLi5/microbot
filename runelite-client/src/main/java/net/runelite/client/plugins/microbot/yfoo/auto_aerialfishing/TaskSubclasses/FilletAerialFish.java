package net.runelite.client.plugins.microbot.yfoo.auto_aerialfishing.TaskSubclasses;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ItemID;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.yfoo.GeneralUtil.RngUtil;
import net.runelite.client.plugins.microbot.yfoo.Task.Task;

import java.util.Arrays;
import java.util.List;

import static net.runelite.client.plugins.microbot.yfoo.auto_aerialfishing.Util.Constants.AERIAL_FISH;

@Slf4j
public class FilletAerialFish extends Task {
    public enum FilletStyle {
        SLOW(RngUtil.randomInclusive(1, 10)), FAST(RngUtil.randomInclusive(1, 10));

        public final int executionWeight;

        FilletStyle(int executionWeight) {
            this.executionWeight = executionWeight;

        }
    }
    private static List<FilletStyle> filletStyles;
    private int numEmptySlotsToFillet;

    private static FilletAerialFish instance;

    public static FilletAerialFish getInstance() {
        if(instance == null) {
            throw new NullPointerException("FilletAerialFish is null");
        }
        return instance;
    }

    public static FilletAerialFish initInstance(Script script) {
        instance = new FilletAerialFish(script);
        return instance;
    }

    private FilletAerialFish(Script script) {
        super(script);
        Microbot.log(String.format("Next fillet will occur at %d empty slots", numEmptySlotsToFillet));
        numEmptySlotsToFillet = RngUtil.gaussian(5, 2, 0, 10);
        filletStyles = Arrays.asList(FilletStyle.SLOW, FilletStyle.FAST);

        StringBuilder builder = new StringBuilder();
        filletStyles.forEach(filletStyle -> builder.append(String.format("%s :: %d, ", filletStyle.name(), filletStyle.executionWeight)));
        Microbot.log(builder.toString());
    }

    @Override
    public boolean shouldRun() throws InterruptedException {
        return Rs2Inventory.contains(rs2Item -> AERIAL_FISH.contains(rs2Item.getId()))
                && Rs2Inventory.hasItem(ItemID.KNIFE)
                && (Rs2Inventory.getEmptySlots() <= numEmptySlotsToFillet || Rs2Inventory.isFull());
    }

    @Override
    public boolean runTask() throws InterruptedException {
        numEmptySlotsToFillet = RngUtil.gaussian(5, 2, 0, 10);
        Microbot.log(String.format("Next fillet will occur at %d empty slots", numEmptySlotsToFillet));
        return rollAndExecuteFilletStyle();
    }

    private boolean rollAndExecuteFilletStyle() {
        FilletStyle selectedStyle = null;
        int weightSum = filletStyles.stream()
                .mapToInt(action -> action.executionWeight)
                .sum();
        int roll = RngUtil.randomInclusive(1, weightSum-1);
        for (FilletStyle style : filletStyles) {
            roll -= style.executionWeight;
            if (roll <= 0) {
                selectedStyle = style;
                break;
            }
        }
        if(selectedStyle == null) {
            Microbot.log("selectedStyle resolved to null");
            selectedStyle = FilletStyle.SLOW;
        }

        boolean result = false;
        switch (selectedStyle) {
            case SLOW:
                result = slowFillet();
                break;
            case FAST:
                result = fastFillet();
                break;
        }

        return result;
    }

    private boolean slowFillet() {
        Microbot.log("Rolled slow fillet");
        boolean combined = Rs2Inventory.combineClosest(rs2Item -> rs2Item.getId() == ItemID.KNIFE, rs2Item -> AERIAL_FISH.contains(rs2Item.getId()));
        if(!combined) {
            Microbot.log("Combination failed");
            return false;
        }
        boolean gainedXp = script.sleepUntil(() -> Microbot.isGainingExp);
        if(!gainedXp) {
            Microbot.log("Did not gain xp after combining knife -> AerialFish");
            return false;
        }

        script.sleepUntil(() -> !Microbot.isGainingExp, 40000);

        int extraIdleTime = RngUtil.gaussian(5000, 1000, 1000, 10000);
        Microbot.log(String.format("Simulating AFK for an additional %dms", extraIdleTime));
        script.sleep(RngUtil.gaussian(5000, 1000, 1000, 10000));
        return true;
    }

    private boolean fastFillet() {
        Microbot.log("Rolled fast fillet");
        int numFish = Rs2Inventory.count(rs2Item -> AERIAL_FISH.contains(rs2Item.getId()));
        int lowBound = RngUtil.randomInclusive(0, numFish-1);
        if(lowBound < 0) lowBound = 5;

        int loopIterations = RngUtil.randomInclusive(lowBound, numFish);
        Microbot.log(String.format("Running %d combinations", loopIterations));
        for(int i = 0; i < loopIterations; i++) {
            Rs2Inventory.combineClosest(rs2Item -> rs2Item.getId() == ItemID.KNIFE, rs2Item -> AERIAL_FISH.contains(rs2Item.getId()));
            script.sleep(RngUtil.gaussian(200, 50, 0, 300));
        }
        return true;
    }
}
