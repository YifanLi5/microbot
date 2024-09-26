package net.runelite.client.plugins.microbot.yfoo.camdozaal_scripts.mining.TaskSubclasses;

import net.runelite.api.GameObject;
import net.runelite.api.ItemID;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.dialogues.Rs2Dialogue;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
import net.runelite.client.plugins.microbot.yfoo.GeneralUtil.AnimationUtil;
import net.runelite.client.plugins.microbot.yfoo.GeneralUtil.InteractionUtil;
import net.runelite.client.plugins.microbot.yfoo.GeneralUtil.RngUtil;
import net.runelite.client.plugins.microbot.yfoo.Task.Task;

public class SmashDeposits extends Task {

    private enum SmashState {
        WALK_TO_ANVIL, SMASH_DEPOSITS, WAIT_FOR_SMASH
    }

    private static SmashDeposits instance;

    public static SmashDeposits getInstance() {
        if(instance == null) {
            throw new NullPointerException("CatchAerialFish is null");
        }
        return instance;
    }

    public static SmashDeposits initInstance(Script script) {
        instance = new SmashDeposits(script);
        return instance;
    }

    private int minEmptySlotsToTrigger;

    private SmashDeposits(Script script) {
        super(script);
        minEmptySlotsToTrigger = RngUtil.randomInclusive(4, 8);
    }

    @Override
    public boolean shouldRun() throws InterruptedException {
        return Rs2Inventory.getEmptySlots() <= minEmptySlotsToTrigger
                && Rs2Player.getAnimation() == -1
                && Rs2Inventory.contains(ItemID.BARRONITE_DEPOSIT);
    }

    @Override
    public boolean runTask() throws InterruptedException {
        SmashState state = SmashState.WALK_TO_ANVIL;
        int numFails = 0;
        while(numFails < 3 && script.isRunning()) {
            Microbot.log("SmashState: " + state);
            switch(state) {
                case WALK_TO_ANVIL:
                    WorldPoint crusherPosition = new WorldPoint(2957, 5807, 0);
                    boolean nearCrusher = Rs2Player.getWorldLocation().distanceTo(crusherPosition) <= 7;
                    if(!nearCrusher) {
                        Rs2Walker.walkTo(crusherPosition, 7);
                        script.sleep(1000);
                        continue;
                    } else {
                        state = SmashState.SMASH_DEPOSITS;
                    }
                    break;
                case SMASH_DEPOSITS:
                    GameObject barroniteCrusher = Rs2GameObject.findObject(41551, new WorldPoint(2956, 5807, 0));
                    if(barroniteCrusher == null) {
                        Microbot.log("Barronite crusher is null");
                        numFails++;
                        script.sleep(600);
                        continue;
                    }
                    boolean startedAnimationAfterInteraction = InteractionUtil.interactObjectAssertAnimation(script, barroniteCrusher);
                    if(!startedAnimationAfterInteraction) {
                        Microbot.log("Did not start animation after interacting with barronite crusher");
                        numFails++;
                        script.sleep(600);
                        continue;
                    }
                    AnimationUtil.waitUntilPlayerStopsAnimating(4000);
                    if(Rs2Inventory.contains(ItemID.BARRONITE_DEPOSIT)) {
                        continue;
                    }

                    state = SmashState.WAIT_FOR_SMASH;
                    break;
                case WAIT_FOR_SMASH:
                    AnimationUtil.waitUntilPlayerStopsAnimating(3000);
                    if(Rs2Inventory.contains(ItemID.BARRONITE_DEPOSIT)) {
                        Microbot.log("Inventory still contains Barronite deposits despite smashing them, likely an error has occured");
                        numFails++;
                        script.sleep(600);
                        state = SmashState.SMASH_DEPOSITS;
                        continue;
                    }

                    // Success!
                    minEmptySlotsToTrigger = RngUtil.randomInclusive(4, 8);
                    //setNextTask(BankAndReturn.getInstance());
                    return true;
            }
            if(!script.isRunning()) {
                Microbot.log("Script has been stopped!");
                return true;
            }
        }
        return false;
    }
}
