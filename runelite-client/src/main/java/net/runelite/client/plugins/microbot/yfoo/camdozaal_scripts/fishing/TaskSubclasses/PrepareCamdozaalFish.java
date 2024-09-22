package net.runelite.client.plugins.microbot.yfoo.camdozaal_scripts.fishing.TaskSubclasses;

import net.runelite.api.ItemID;
import net.runelite.api.Skill;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;
import net.runelite.client.plugins.microbot.yfoo.GeneralUtil.AnimationUtil;
import net.runelite.client.plugins.microbot.yfoo.GeneralUtil.RngUtil;
import net.runelite.client.plugins.microbot.yfoo.Task.Task;

import java.util.*;
import java.util.stream.Collectors;

import static net.runelite.client.plugins.microbot.yfoo.camdozaal_scripts.Util.Constants.*;

public class PrepareCamdozaalFish extends Task {

    private enum PrepareState {
        DROP_UNPREPARABLE_FISH, PREPARE, OFFER, CLEAN_INV, ROLL_NEXT_TRIGGER
    }

    private static PrepareCamdozaalFish instance;
    private static final HashMap<Integer, Integer> offeringRequirements = new HashMap<>();

    static {
        offeringRequirements.put(ItemID.RAW_GUPPY, 7);
        offeringRequirements.put(ItemID.RAW_CAVEFISH, 20);
        offeringRequirements.put(ItemID.RAW_TETRA, 33);
        offeringRequirements.put(ItemID.RAW_CATFISH, 46);
    }

    private int minEmptySlotsToTrigger;

    public static PrepareCamdozaalFish getInstance() {
        if(instance == null) {
            throw new NullPointerException("CatchAerialFish is null");
        }
        return instance;
    }

    public static PrepareCamdozaalFish initInstance(Script script) {
        instance = new PrepareCamdozaalFish(script);
        return instance;
    }


    private PrepareCamdozaalFish(Script script) {
        super(script);
        minEmptySlotsToTrigger = RngUtil.randomInclusive(4, 8);
    }

    @Override
    public boolean shouldRun() throws InterruptedException {
        return Rs2Inventory.getEmptySlots() <= minEmptySlotsToTrigger
                && Rs2Player.getAnimation() == -1
                && Rs2Inventory.contains(rs2Item -> CAMDOZAAL_FISH.contains(rs2Item.getId()));
    }

    @Override
    public boolean runTask() throws InterruptedException {
        PrepareState state = PrepareState.DROP_UNPREPARABLE_FISH;
        int numFails = 0;
        while(numFails < 3) {
            Microbot.log("PrepareCamdozaalFish state: " + state.name());
            if(state == PrepareState.DROP_UNPREPARABLE_FISH) {
                List<Integer> preparableFishAtCurrentLvl = calculatePreparableFishAtCurrentLvl();
                boolean droppedAllUnpreparableFish = Rs2Inventory.dropAll(
                    rs2Item -> CAMDOZAAL_FISH.contains(rs2Item.getId()) && !preparableFishAtCurrentLvl.contains(rs2Item.getId())
                );
                if(!droppedAllUnpreparableFish) {
                    Microbot.log("Failed to drop unpreparable fish");
                    numFails++;
                    continue;
                }
                script.sleep(300);
                if(!Rs2Inventory.contains(rs2Item -> CAMDOZAAL_FISH.contains(rs2Item.getId()))) {
                    state = PrepareState.ROLL_NEXT_TRIGGER;
                    Microbot.log("Inventory is empty after dropping unpreparable fish due to low cooking lvl");
                } else {
                    state = PrepareState.PREPARE;
                }
            }
            else if(state == PrepareState.PREPARE) {
                if(!Rs2Inventory.contains(rs2Item -> CAMDOZAAL_FISH.contains(rs2Item.getId()))) {
                    state = PrepareState.ROLL_NEXT_TRIGGER;
                    continue;
                }
                boolean startedPreparing = interactObjectHandleWidget(PREPARATION_TABLE, MAKE_STUFF_WIDGET_ROOT, PREPARE_FISH_CHILD_WIDGET_ID);
                if(!startedPreparing) {
                    numFails++;
                    continue;
                }
                AnimationUtil.waitUntilPlayerStopsAnimating(3000);
//                while(Rs2Player.isAnimating()) {
//                    Rs2Player.waitForAnimation();
//                }

                int additionalSleepTime = RngUtil.gaussian(3000, 1000, 0, 6000);
                Microbot.log(String.format("Sleeping for an additional %dms", additionalSleepTime));
                script.sleep(additionalSleepTime);
                if(!Rs2Inventory.contains(rs2Item -> CAMDOZAAL_FISH.contains(rs2Item.getId()))) {
                    state = PrepareState.OFFER;
                }
            }
            else if(state == PrepareState.OFFER) {
                if(!Rs2Inventory.contains(rs2Item -> PREPARED_CAMDOZAAL_FISH.contains(rs2Item.getId()))) {
                    state = PrepareState.CLEAN_INV;
                    continue;
                }
                boolean startedOffering = interactObjectHandleWidget(OFFERING_TABLE, MAKE_STUFF_WIDGET_ROOT, PREPARE_FISH_CHILD_WIDGET_ID);
                if(!startedOffering) {
                    numFails++;
                    continue;
                }
                AnimationUtil.waitUntilPlayerStopsAnimating(3000);
                int additionalSleepTime = RngUtil.gaussian(3000, 1000, 0, 6000);
                Microbot.log(String.format("Sleeping for an additional %dms", additionalSleepTime));
                script.sleep(additionalSleepTime);
                if(!Rs2Inventory.contains(rs2Item -> PREPARED_CAMDOZAAL_FISH.contains(rs2Item.getId()))) {
                    state = PrepareState.CLEAN_INV;
                }
            }
            else if(state == PrepareState.CLEAN_INV) {
                boolean dropRuinedFish = Rs2Inventory.dropAll(rs2Item -> rs2Item.getName().startsWith("Ruined"));
                if(!dropRuinedFish) {
                    Microbot.log("Failed to drop ruined fish");
                    numFails++;
                    continue;
                }
                state = PrepareState.ROLL_NEXT_TRIGGER;
            }
            else if(state == PrepareState.ROLL_NEXT_TRIGGER) {
                minEmptySlotsToTrigger = RngUtil.randomInclusive(4, 8);
                return true;
            }
        }
        return false;
    }


    private List<Integer> calculatePreparableFishAtCurrentLvl() {
        int cookingLvl = Rs2Player.getRealSkillLevel(Skill.COOKING);

        // Filter the map and extract the keys (fish IDs) whose value (required level) is <= cookingLvl
        return offeringRequirements.entrySet().stream()
                .filter(entry -> entry.getValue() <= cookingLvl)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private boolean interactObjectHandleWidget(int objectId, int widgetRoot, int widgetChild) {
        if(!Rs2GameObject.interact(objectId)) {
            Microbot.log("Failed interact with offering table");
            return false;
        }
        if(!script.sleepUntil(() -> Rs2Widget.isWidgetVisible(widgetRoot, widgetChild))) {
            Microbot.log("offering widget did not become visible");
            return false;
        } else if (Rs2Player.isAnimating(1200)) {
            Microbot.log("Player immediately started animation without a widget interaction.");
            return true;
        }

        if(!Rs2Widget.clickWidget(widgetRoot, widgetChild)) {
            Microbot.log(String.format("Unable to click widget (%d, %d)", MAKE_STUFF_WIDGET_ROOT, PREPARE_FISH_CHILD_WIDGET_ID));
            return false;
        }
        return true;
    }

}
