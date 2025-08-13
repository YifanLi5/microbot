package net.runelite.client.plugins.microbot.yfoo.camdozaal_mining.states;

import net.runelite.api.WallObject;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.ActorModel;
import net.runelite.client.plugins.microbot.util.Global;
import net.runelite.client.plugins.microbot.util.antiban.Rs2Antiban;
import net.runelite.client.plugins.microbot.util.camera.Rs2Camera;
import net.runelite.client.plugins.microbot.util.coords.Rs2WorldArea;
import net.runelite.client.plugins.microbot.util.equipment.Rs2Equipment;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.math.Rs2Random;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.player.Rs2PlayerModel;
import net.runelite.client.plugins.microbot.yfoo.GeneralUtil.CustomWalker;
import net.runelite.client.plugins.microbot.yfoo.GeneralUtil.ExtendableConditionalSleep;
import net.runelite.client.plugins.microbot.yfoo.GeneralUtil.RngUtil;
import net.runelite.client.plugins.microbot.yfoo.StateMachine.StateNodeV2;
import net.runelite.client.plugins.microbot.yfoo.StateMachine.StateStepResult;
import net.runelite.client.plugins.microbot.yfoo.camdozaal_mining.Constants;

import java.util.*;
import java.util.stream.Collectors;

import static net.runelite.client.plugins.microbot.yfoo.camdozaal_mining.Constants.*;

public class MiningState extends StateNodeV2<MiningState.MiningStateSteps> {


    private WorldPoint selectedBarroniteVein;
    private static MiningState instance;

    public static MiningState getInstance() {
        if(instance == null) {
            throw new NullPointerException(MiningState.class.getSimpleName() + " is null");
        }
        return instance;
    }

    public static MiningState initInstance(Script script) {
        if(instance == null)
            instance = new MiningState(script);
        return instance;
    }

    public enum MiningStateSteps {
        WALK_TO_VEINS, INTERACT_VEIN, SLEEP_UNTIL_COMPLETE, COMPLETE
    }

    public MiningState(Script script) {
        super(script);
    }

    @Override
    public boolean canRun() throws InterruptedException {
        return Rs2Equipment.isWearing(item -> item.getName().contains("pickaxe"))
                || Rs2Inventory.contains(item -> item.getName().contains("pickaxe"));
    }

    @Override
    public void initStateSteps() {
        this.stateSteps = new LinkedHashMap<>();
        this.stateSteps.put(MiningStateSteps.WALK_TO_VEINS, () -> {
            if(!miningArea.contains(Rs2Player.getRs2WorldPoint().getWorldPoint())) {
                Microbot.log("Not in mining area. Walking into it.");
                this.selectedBarroniteVein = miningClusters.get(Rs2Random.between(0, miningClusters.size()));
            } else {

                Microbot.log("In mining area, getting unpopulated cluster");
                this.selectedBarroniteVein = getRandomUnpopulatedCluster();



            }
            if(this.selectedBarroniteVein.distanceTo(Rs2Player.getRs2WorldPoint().getWorldPoint()) >= 3) {
                Microbot.log("Moving to new cluster @ %s", this.selectedBarroniteVein);
                CustomWalker.walkTo(this.selectedBarroniteVein, 3);
            } else {
                Microbot.log("Staying with current cluster");
            }

            boolean inArea = miningArea.contains(Rs2Player.getRs2WorldPoint().getWorldPoint());
            if(!inArea) {
                Microbot.log("Player not in area.");
            }
            return new StateStepResult<>(MiningStateSteps.INTERACT_VEIN, inArea);
        });
        this.stateSteps.put(MiningStateSteps.INTERACT_VEIN, () -> {
            WallObject barroniteRocks;
            if(this.selectedBarroniteVein != null) {
                barroniteRocks = Rs2GameObject.getWallObject(BARRONITE_ROCKS, this.selectedBarroniteVein);
            } else {
                barroniteRocks = Rs2GameObject.getWallObject(BARRONITE_ROCKS);
            }

            if(!Rs2Camera.isTileCenteredOnScreen(barroniteRocks.getLocalLocation(), 35.0)) {
                Rs2Camera.centerTileOnScreen(barroniteRocks.getLocalLocation(), 35.0);
            }
            Rs2GameObject.interact(barroniteRocks, MINE);
            boolean result = ExtendableConditionalSleep.sleep(3000, Rs2Player::isAnimating, null, Rs2Player::isMoving);
            if(!result) {
                Microbot.log("Player did not start mining anim.");
            }
            return new StateStepResult<>(MiningStateSteps.SLEEP_UNTIL_COMPLETE, result);
        });
        this.stateSteps.put(MiningStateSteps.SLEEP_UNTIL_COMPLETE, () -> {
            if(RngUtil.boolD100Roll(75)) {
                Rs2Antiban.moveMouseOffScreen();
            }
            long start = System.currentTimeMillis();
            ExtendableConditionalSleep.sleepUntilAnimStops();
            long end = System.currentTimeMillis();
            long animTime = end - start;


            if(animTime >= 10000) {
                Global.sleepGaussian(3000, 1000);
            }

            return new StateStepResult<>(MiningStateSteps.COMPLETE, true);
        });
        this.stateSteps.put(MiningStateSteps.COMPLETE, () -> {
            Microbot.log("COMPLETE");
            return new StateStepResult<>(MiningStateSteps.COMPLETE, true);
        });
    }

    @Override
    public StateNodeV2<?> nextState() throws InterruptedException {
        if(Rs2Inventory.isFull())
            return SmashingState.getInstance();
        else return MiningState.getInstance();
    }

    @Override
    public MiningStateSteps completionStep() {
        return MiningStateSteps.COMPLETE;
    }
}
