package net.runelite.client.plugins.microbot.yfoo.yBlastFurnace.States.Standard;

import net.runelite.api.Varbits;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.util.dialogues.Rs2Dialogue;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.keyboard.Rs2Keyboard;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
import net.runelite.client.plugins.microbot.yfoo.GeneralUtil.IdleSleep;
import net.runelite.client.plugins.microbot.yfoo.GeneralUtil.RngUtil;
import net.runelite.client.plugins.microbot.yfoo.StateMachine.StateNode;
import net.runelite.client.plugins.microbot.yfoo.yBlastFurnace.BFConfig;
import net.runelite.client.plugins.microbot.yfoo.yBlastFurnace.Script;

import java.awt.event.KeyEvent;
import java.util.LinkedHashMap;
import java.util.Map;

public class RetrieveBarsState extends StateNode {

    enum States {
        MOVE_TO_DISPENSER, RETRIEVE_BARS, COMPLETE
    }

    private static RetrieveBarsState instance;

    final static Map<WorldPoint, Integer> worldPointWeightings = Map.of(
            new WorldPoint(1940, 4962, 0), RngUtil.randomInclusive(0, 10),
            new WorldPoint(1939, 4963, 0), RngUtil.randomInclusive(0, 10),
            new WorldPoint(1939, 4962, 0), RngUtil.randomInclusive(0, 10),
            new WorldPoint(1939, 4964, 0), RngUtil.randomInclusive(0, 10),
            new WorldPoint(1940, 4964, 0), RngUtil.randomInclusive(0, 10)
    );


    public static RetrieveBarsState getInstance() {
        if(instance == null) {
            throw new NullPointerException(RetrieveBarsState.class.getSimpleName() + " is null");
        }
        return instance;
    }

    public static RetrieveBarsState initInstance(Script script) {
        if (instance == null)
            instance = new RetrieveBarsState(script);
        return instance;
    }

    BFConfig bfConfig;

    public RetrieveBarsState(Script script) {
        super(script);
        this.bfConfig = script.config;
    }

    @Override
    public int retries() {
        return 2;
    }

    @Override
    public boolean canRun() throws InterruptedException {
        return !Rs2Inventory.isFull();
    }

    @Override
    public void initStateSteps() {
        this.stateSteps = new LinkedHashMap<>();
        this.stateSteps.put(States.MOVE_TO_DISPENSER, () -> {
            script.sleep(RngUtil.gaussian(400, 200, 0, 1500));
            if(bfConfig.barType().getNumBarsInDispenser() > 0) {
                return true;
            }
            if(Rs2Dialogue.isInDialogue() || (Microbot.getVarbitValue(Varbits.BAR_DISPENSER) >= 2
                    && RngUtil.boolD100Roll(35))
            ) {
                WorldPoint randomWP = RngUtil.rollForWeightedAction(worldPointWeightings);
                Rs2Walker.walkFastCanvas(randomWP);
                script.sleep(RngUtil.gaussian(800, 300, 0, 2500));
            }
            return true;
        });
        this.stateSteps.put(States.RETRIEVE_BARS, () -> {
            IdleSleep.chanceIdleSleep();
            boolean allOreProcessed = script.sleepUntil(() -> bfConfig.barType().getNumOreInFurnace() <= 0);
            if(!allOreProcessed && bfConfig.barType().furnaceRequiresMoreCoal()) {
                if(bfConfig.barType().furnaceRequiresMoreCoal()) {
                    Microbot.log("Require more coal to finish all ore.");
                    return true;
                }
                Microbot.log("debug1");
                return false;
            }

            boolean canCollect = script.sleepUntil(() -> Microbot.getVarbitValue(Varbits.BAR_DISPENSER) >= 2, 3000);
            if(!canCollect) {
                Microbot.log("cannot collect");
                return false;
            }
            if(!Rs2GameObject.interact(9092, "Take")) {
                Microbot.log("Failed interaction");
                return false;
            }

            boolean gotBars = script.sleepUntil(() -> {
                Rs2Keyboard.keyHold(KeyEvent.VK_SPACE);
                return Rs2Inventory.contains(item -> item.getName().contains("bar"));
            });
            Rs2Keyboard.keyRelease(KeyEvent.VK_SPACE);
            return gotBars;
        });
    }


    @Override
    public StateNode nextState() {
        return BreakState.getInstance();
    }
}
