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

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static net.runelite.client.plugins.microbot.yfoo.camdozaal_mining.Constants.*;

public class MiningState extends StateNodeV2<MiningState.MiningStateSteps> {



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
                WorldPoint p = miningClusters[Rs2Random.between(0, miningClusters.length)];
                CustomWalker.walkTo(p, 5);
            } else {
                Microbot.log("In mining area, checking players");
                List<WorldPoint> playerPositions = Rs2Player.getPlayers(p -> miningArea
                        .contains(p.getWorldLocation()))
                        .map(ActorModel::getWorldLocation)
                        .collect(Collectors.toList());

                WorldPoint openPosition = Arrays.stream(miningClusters).filter(worldPoint -> {
                    boolean noNearbyPlayers = true;
                    for(WorldPoint position: playerPositions) {
                        if(position.distanceTo(worldPoint) < 2) {
                            noNearbyPlayers = false;
                            break;
                        }
                    }
                    return noNearbyPlayers;
                }).findFirst().orElse(miningClusters[0]);

                if(openPosition.distanceTo(Rs2Player.getRs2WorldPoint().getWorldPoint()) >= 5) {
                    Microbot.log("Moving to new cluster @ %s", openPosition);
                    CustomWalker.walkTo(openPosition, 3);
                } else {
                    Microbot.log("Staying with current cluster");
                }

            }
            boolean inArea = miningArea.contains(Rs2Player.getRs2WorldPoint().getWorldPoint());
            if(!inArea) {
                Microbot.log("Player not in area.");
            }
            return new StateStepResult<>(MiningStateSteps.INTERACT_VEIN, inArea);
        });
        this.stateSteps.put(MiningStateSteps.INTERACT_VEIN, () -> {
            WallObject barroniteRocks = Rs2GameObject.getWallObject(BARRONITE_ROCKS);

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
            long start = System.currentTimeMillis();
            ExtendableConditionalSleep.sleepUntilAnimStops();
            long end = System.currentTimeMillis();
            long animTime = end - start;
            if(RngUtil.boolD100Roll(75)) {
                Rs2Antiban.moveMouseOffScreen();
            }

            if(animTime >= 10000) {
                Global.sleepGaussian(5000, 1000);
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
