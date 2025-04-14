package net.runelite.client.plugins.microbot.yfoo.auto_aerialfishing.TaskSubclasses;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ItemID;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.yfoo.GeneralUtil.RngUtil;
import net.runelite.client.plugins.microbot.yfoo.Task.Task;
import net.runelite.client.plugins.microbot.yfoo.auto_aerialfishing.AerialFishingConfig;

import java.text.DecimalFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;

import static net.runelite.client.plugins.microbot.yfoo.auto_aerialfishing.Util.Constants.AERIAL_FISH;

@Slf4j
public class FilletAerialFish extends Task {
    public enum FilletStyle {
        SLOW, FAST, HYBRID;
    }

    private static List<AbstractMap.SimpleEntry<FilletStyle, Integer>> filletStyles;
    private int numEmptySlotsToFillet;

    private AerialFishingConfig config;

    private static FilletAerialFish instance;

    public static FilletAerialFish getInstance() {
        if(instance == null) {
            throw new NullPointerException("FilletAerialFish is null");
        }
        return instance;
    }

    public static FilletAerialFish initInstance(Script script, AerialFishingConfig config) {
        instance = new FilletAerialFish(script, config);
        return instance;
    }

    private FilletAerialFish(Script script, AerialFishingConfig config) {
        super(script);
        Microbot.log(String.format("Next fillet will occur inventory has <= %d empty slots", numEmptySlotsToFillet));
        numEmptySlotsToFillet = RngUtil.gaussian(5, 2, 0, 10);

        filletStyles = new ArrayList<>();
        if(config.allowFastFillet()) {
            filletStyles.add(new AbstractMap.SimpleEntry<>(FilletStyle.FAST, RngUtil.randomInclusive(1, 10)));
            filletStyles.add(new AbstractMap.SimpleEntry<>(FilletStyle.HYBRID, RngUtil.randomInclusive(1, 10)));
        }

        if(config.allowSlowFillet())
            filletStyles.add(new AbstractMap.SimpleEntry<>(FilletStyle.SLOW, RngUtil.randomInclusive(1, 10)));


        StringBuilder builder = new StringBuilder();
        double weightSum = filletStyles.stream()
                .mapToDouble(AbstractMap.SimpleEntry::getValue)
                .sum();
        DecimalFormat df = new DecimalFormat("#0.0%");
        filletStyles.forEach(filletStyle -> {
            double percentage = (filletStyle.getValue() / weightSum);
            builder.append(String.format("%s :: %d (~%s), ", filletStyle.getKey(), filletStyle.getValue(), df.format(percentage)));
        });
        Microbot.log(builder.toString());
    }

    @Override
    public boolean shouldRun() throws InterruptedException {
        boolean haveKnifeAndFish = Rs2Inventory.contains(rs2Item -> AERIAL_FISH.contains(rs2Item.getId()))
                && Rs2Inventory.hasItem(ItemID.KNIFE);
        boolean shouldFillet = Rs2Inventory.getEmptySlots() <= numEmptySlotsToFillet
                || Rs2Inventory.isFull()
                || !Rs2Inventory.contains(ItemID.KING_WORM, ItemID.FISH_CHUNKS);

        Microbot.log("shouldFillet: " + shouldFillet);

        return haveKnifeAndFish && shouldFillet;
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
                .mapToInt(AbstractMap.SimpleEntry::getValue)
                .sum();
        int roll = RngUtil.randomInclusive(1, weightSum-1);
        for (AbstractMap.SimpleEntry<FilletStyle, Integer> mapping : filletStyles) {
            roll -= mapping.getValue();
            if (roll <= 0) {
                selectedStyle = mapping.getKey();
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
            case HYBRID:
                result = hybridFillet();
                break;
        }

        return result;
    }

    private boolean slowFillet() {
        Microbot.log("Rolled slow fillet");
        boolean combined = false;
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
            // Rs2Inventory.combineClosest(rs2Item -> rs2Item.getId() == ItemID.KNIFE, rs2Item -> AERIAL_FISH.contains(rs2Item.getId()));
            script.sleep(RngUtil.gaussian(200, 50, 0, 300));
        }
        return true;
    }

    private boolean hybridFillet() {
        Microbot.log("Rolled hybrid fillet");
        int numFish = Rs2Inventory.count(rs2Item -> AERIAL_FISH.contains(rs2Item.getId()));
        int loopIterations = numFish / RngUtil.randomInclusive(2, 4);
        Microbot.log(String.format("Running %d combinations", loopIterations));
        for(int i = 0; i < loopIterations; i++) {
            // Rs2Inventory.combineClosest(rs2Item -> rs2Item.getId() == ItemID.KNIFE, rs2Item -> AERIAL_FISH.contains(rs2Item.getId()));
            script.sleep(RngUtil.gaussian(200, 50, 0, 300));
        }

        if(!script.sleepUntil(() -> !Microbot.isGainingExp, 40000)) {
            return false;
        }

        int extraIdleTime = RngUtil.gaussian(5000, 1000, 1000, 10000);
        Microbot.log(String.format("Simulating AFK for an additional %dms", extraIdleTime));
        script.sleep(RngUtil.gaussian(5000, 1000, 1000, 10000));
        return true;
    }
}
