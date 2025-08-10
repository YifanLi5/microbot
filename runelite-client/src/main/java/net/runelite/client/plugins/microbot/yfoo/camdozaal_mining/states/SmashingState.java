package net.runelite.client.plugins.microbot.yfoo.camdozaal_mining.states;

import net.runelite.api.ItemID;
import net.runelite.api.ObjectID;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.Global;
import net.runelite.client.plugins.microbot.util.coords.Rs2WorldArea;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.grounditem.Rs2GroundItem;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.yfoo.GeneralUtil.CustomWalker;
import net.runelite.client.plugins.microbot.yfoo.GeneralUtil.ExtendableConditionalSleep;
import net.runelite.client.plugins.microbot.yfoo.StateMachine.StateNodeV2;
import net.runelite.client.plugins.microbot.yfoo.StateMachine.StateStepResult;

import java.util.LinkedHashMap;

import static net.runelite.api.ItemID.BARRONITE_DEPOSIT;
import static net.runelite.client.plugins.microbot.yfoo.camdozaal_mining.Constants.*;

public class SmashingState extends StateNodeV2<SmashingState.SmashingStateSteps> {



    private static SmashingState instance;
    public static SmashingState getInstance() {
        if(instance == null) {
            throw new NullPointerException(SmashingState.class.getSimpleName() + " is null");
        }
        return instance;
    }

    public static SmashingState initInstance(Script script) {
        if(instance == null)
            instance = new SmashingState(script);
        return instance;
    }

    public enum SmashingStateSteps {
        WALK_TO_ANVIL, GRAB_HAMMER, INTERACT_ANVIL, SLEEP_UNTIL_COMPLETE, COMPLETE
    }

    public SmashingState(Script script) {
        super(script);
    }

    @Override
    public boolean canRun() throws InterruptedException {
        return Rs2Inventory.contains(ItemID.BARRONITE_DEPOSIT);
    }

    @Override
    public void initStateSteps() {
        this.stateSteps = new LinkedHashMap<>();
        this.stateSteps.put(SmashingStateSteps.WALK_TO_ANVIL, () -> {
            CustomWalker.walkTo(anvilLocation, 5);
            boolean inArea = smashArea.contains(Rs2Player.getRs2WorldPoint().getWorldPoint());
            if(!inArea) {
                Microbot.log("Player not in area.");
            }
            return new StateStepResult<>(SmashingStateSteps.GRAB_HAMMER, inArea);
        });
        this.stateSteps.put(SmashingStateSteps.GRAB_HAMMER, () -> {
            if(!Rs2Inventory.contains(ItemID.HAMMER)) {
                Microbot.log("Picking up hammer.");
                if(Rs2Inventory.isFull()) {
                    if(!Rs2Inventory.drop(BARRONITE_DEPOSIT)) {
                        Microbot.log("Failed to drop barronite.");
                        return new StateStepResult<>(SmashingStateSteps.GRAB_HAMMER, false);
                    }
                }
                boolean getHammer = Rs2GroundItem.exists(ItemID.HAMMER, 15) && Rs2GroundItem.pickup(ItemID.HAMMER);
                if(!getHammer) {
                    Microbot.log("Failed to pick up the hammer.");
                    return new StateStepResult<>(SmashingStateSteps.GRAB_HAMMER, false);
                }
                boolean gotHammer = Global.sleepUntil(() -> Rs2Inventory.contains(ItemID.HAMMER), 2000);
                if(!gotHammer) {
                    Microbot.log("Hammer not in inventory.");
                    return new StateStepResult<>(SmashingStateSteps.GRAB_HAMMER, false);
                }
            }
            return new StateStepResult<>(SmashingStateSteps.INTERACT_ANVIL, true);
        });
        this.stateSteps.put(SmashingStateSteps.INTERACT_ANVIL, () -> {
            Rs2GameObject.interact(ObjectID.BARRONITE_CRUSHER, SMITH);
            boolean result = ExtendableConditionalSleep.sleep(3000, Rs2Player::isAnimating, null, Rs2Player::isMoving);
            if(!result) {
                Microbot.log("Player did not start smashing.");
            }
            return new StateStepResult<>(SmashingStateSteps.SLEEP_UNTIL_COMPLETE, result);
        });
        this.stateSteps.put(SmashingStateSteps.SLEEP_UNTIL_COMPLETE, () -> {
            ExtendableConditionalSleep.sleepUntilAnimStops();
            Global.sleepGaussian(5000, 1000);
            boolean smashedAll = !Rs2Inventory.contains(BARRONITE_DEPOSIT);
            return new StateStepResult<>(SmashingStateSteps.COMPLETE, smashedAll);
        });
        this.stateSteps.put(SmashingStateSteps.COMPLETE, () -> {
            Microbot.log("COMPLETE");
            return new StateStepResult<>(SmashingStateSteps.COMPLETE, true);
        });
    }

    @Override
    public StateNodeV2<?> nextState() throws InterruptedException {
        return BankingState.getInstance();
    }

    @Override
    public SmashingStateSteps completionStep() {
        return SmashingStateSteps.COMPLETE;
    }
}
