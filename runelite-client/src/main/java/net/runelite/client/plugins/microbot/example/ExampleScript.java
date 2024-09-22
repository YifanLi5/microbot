package net.runelite.client.plugins.microbot.example;

import net.runelite.api.ItemID;
import net.runelite.api.Skill;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.grounditem.LootingParameters;
import net.runelite.client.plugins.microbot.util.grounditem.Rs2GroundItem;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


public class ExampleScript extends Script {

    public static boolean test = false;

    private static final HashMap<Integer, Integer> offeringRequirements = new HashMap<>();

    static {
        offeringRequirements.put(ItemID.RAW_GUPPY, 7);
        offeringRequirements.put(ItemID.RAW_CAVEFISH, 20);
        offeringRequirements.put(ItemID.RAW_TETRA, 33);
        offeringRequirements.put(ItemID.RAW_CATFISH, 46);
    }

    public boolean run(ExampleConfig config) {
        Microbot.enableAutoRunOn = false;
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn()) return;
                if (!super.run()) return;
                List<Integer> test = calculatePreparableFishAtCurrentLvl();
                Microbot.log(test.stream().map(String::valueOf)
                        .collect(Collectors.joining(",")));

            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }, 0, 1000, TimeUnit.MILLISECONDS);
        return true;
    }

    private List<Integer> calculatePreparableFishAtCurrentLvl() {
        int cookingLvl = Rs2Player.getRealSkillLevel(Skill.COOKING);

        // Filter the map and extract the keys (fish IDs) whose value (required level) is <= cookingLvl
        return offeringRequirements.entrySet().stream()
                .filter(entry -> entry.getValue() <= cookingLvl)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    @Override
    public void shutdown() {
        super.shutdown();
    }
}
