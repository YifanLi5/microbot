package net.runelite.client.plugins.microbot.yfoo.camdozaal_scripts.mining.TaskSubclasses;

import net.runelite.api.GameObject;
import net.runelite.api.ItemID;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
import net.runelite.client.plugins.microbot.yfoo.GeneralUtil.AnimationUtil;
import net.runelite.client.plugins.microbot.yfoo.GeneralUtil.InteractionUtil;
import net.runelite.client.plugins.microbot.yfoo.GeneralUtil.RngUtil;
import net.runelite.client.plugins.microbot.yfoo.Task.Task;
import net.runelite.client.plugins.microbot.yfoo.camdozaal_scripts.mining.CamdozaalMiningConfig;

public class SmashDeposits extends Task {

    private enum SmashState {
        WALK_TO_ANVIL, WAIT_FOR_SMASH
    }

    private static SmashDeposits instance;

    public static SmashDeposits getInstance() {
        if(instance == null) {
            throw new NullPointerException("CatchAerialFish is null");
        }
        return instance;
    }

    public static SmashDeposits initInstance(Script script, CamdozaalMiningConfig config) {
        instance = new SmashDeposits(script, config);
        return instance;
    }

    private CamdozaalMiningConfig config;
    private int minEmptySlotsToTrigger;
    private static final WorldPoint crusherPosition = new WorldPoint(2957, 5807, 0);

    private SmashDeposits(Script script, CamdozaalMiningConfig config) {
        super(script);
        this.config = config;
        minEmptySlotsToTrigger = RngUtil.randomInclusive(1, 2);
    }

    @Override
    public boolean shouldRun() throws InterruptedException {
        return !config.dropBarroniteDeposits() &&
                Rs2Inventory.getEmptySlots() <= minEmptySlotsToTrigger
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
                    boolean interactedWithCrusher = false;
                    if(interactedWithCrusher) {

                        state = SmashState.WAIT_FOR_SMASH;
                        script.sleep(RngUtil.gaussian(600, 150, 0 ,1000));
                        break;
                    } else {
                        numFails++;
                        script.sleep(600);
                        continue;
                    }

                case WAIT_FOR_SMASH:
                    AnimationUtil.waitUntilPlayerStopsAnimating(3000);
                    if(Rs2Inventory.contains(ItemID.BARRONITE_DEPOSIT)) {
                        Microbot.log("Inventory still contains Barronite deposits despite smashing them, likely an error has occured");
                        numFails++;
                        script.sleep(600);
                        state = SmashState.WALK_TO_ANVIL;
                        continue;
                    }

                    // Success!
                    minEmptySlotsToTrigger = RngUtil.randomInclusive(1, 2);
                    setNextTask(BankAndReturn.getInstance());
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
