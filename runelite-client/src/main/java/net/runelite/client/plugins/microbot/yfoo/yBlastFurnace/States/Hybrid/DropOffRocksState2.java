package net.runelite.client.plugins.microbot.yfoo.yBlastFurnace.States.Hybrid;

import net.runelite.api.ItemID;
import net.runelite.api.ObjectID;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.equipment.Rs2Equipment;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.keyboard.Rs2Keyboard;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.yfoo.GeneralUtil.ExtendableConditionalSleep;
import net.runelite.client.plugins.microbot.yfoo.GeneralUtil.HoverBoundsUtil;
import net.runelite.client.plugins.microbot.yfoo.GeneralUtil.RngUtil;
import net.runelite.client.plugins.microbot.yfoo.StateMachine.StateNode;
import net.runelite.client.plugins.microbot.yfoo.yBlastFurnace.BFScript;
import net.runelite.client.plugins.microbot.yfoo.yBlastFurnace.Util.BFUtils;

import java.awt.event.KeyEvent;
import java.util.LinkedHashMap;
import java.util.concurrent.Callable;

public class DropOffRocksState2 extends StateNode {

    enum States {
        EQUIP_GS_GLOVES, DROP_OFF_MAIN, DROP_OFF_COAL_BAG
    }

    private static DropOffRocksState2 instance;
    Callable<Boolean> successCondition = () -> !Rs2Inventory.isFull();
    Callable<Boolean> extendCondition = () -> Rs2Player.isMoving();
    private int doubleEmptyChance;

    public static DropOffRocksState2 getInstance() {
        if(instance == null) {
            throw new NullPointerException(DropOffRocksState2.class.getSimpleName() + " is null");
        }
        return instance;
    }

    public static DropOffRocksState2 initInstance(BFScript script) {
        if (instance == null)
            instance = new DropOffRocksState2(script);
        return instance;
    }

    public DropOffRocksState2(BFScript script) {
        super(script);
        this.doubleEmptyChance = RngUtil.randomInclusive(30, 80);
        Microbot.log("Double Empty Chance: " + doubleEmptyChance);
    }

    @Override
    public boolean canRun() throws InterruptedException {
        return Rs2Inventory.contains(item -> {
            String name = item.getName();
            return name.equals("Coal") || name.endsWith("ore");
        }) && Rs2Inventory.contains(ItemID.COAL_BAG_12019);
    }

    @Override
    public void initStateSteps() {
        this.stateSteps = new LinkedHashMap<>();
        this.stateSteps.put(States.EQUIP_GS_GLOVES, () -> {
            if(!Rs2Inventory.contains(ItemID.GOLD_ORE)) {
                return true;
            }
            if(Rs2Equipment.hasEquipped(ItemID.GOLDSMITH_GAUNTLETS)) {
                Microbot.log("GS gauntlets already equipped");
                return true;
            }
            if(!Rs2Inventory.equip(ItemID.GOLDSMITH_GAUNTLETS))  {
                Microbot.log("Failed to equip GS gauntlets");
                return false;
            }
            return script.sleepUntil(() -> Rs2Equipment.hasEquipped(ItemID.GOLDSMITH_GAUNTLETS), 1000);
        });
        this.stateSteps.put(States.DROP_OFF_MAIN, () -> {
            if(Rs2Bank.isOpen()) Rs2Keyboard.keyPress(KeyEvent.VK_ESCAPE);
            if(Rs2Inventory.contains(item -> item.getName().endsWith("ore"))) {
                Rs2GameObject.interact(ObjectID.CONVEYOR_BELT, "Put-ore-on");
                HoverBoundsUtil.hover("Coal bag");
                boolean droppedOffOre = ExtendableConditionalSleep.sleep(4000,
                        successCondition,
                        null,
                        extendCondition
                );
                if(!droppedOffOre) {
                    Microbot.log("Failed to drop off ore/coal on conveyor belt");
                    return false;
                }
            }
            return true;
        });
        this.stateSteps.put(States.DROP_OFF_COAL_BAG, () -> {
            boolean empty1 = emptyCoalBagOntoConveyorBelt();
            if(!empty1) return false;

            if(rollToCompletelyEmptyCoalBag()) {
                script.sleep(RngUtil.gaussian(500, 100, 200, 900));
                return emptyCoalBagOntoConveyorBelt();
            }
            return true;
        });
    }

    @Override
    public StateNode nextState() {
        return RetrieveBarsState2.getInstance();
    }

    private boolean emptyCoalBagOntoConveyorBelt() {
        if(!BFUtils.emptyCoalBag()) {
            Microbot.log("Failed to empty action on coal bag");
            return false;
        }
        Rs2GameObject.interact(ObjectID.CONVEYOR_BELT, "Put-ore-on");
        return ExtendableConditionalSleep.sleep(4000,
                successCondition,
                null,
                extendCondition
        );
    }

    private boolean rollToCompletelyEmptyCoalBag() {
        if(this.doubleEmptyChance >= 100) return true;
        boolean doubleEmpty = this.doubleEmptyChance >= RngUtil.randomInclusive(0, 100);
        if(doubleEmpty) {
            this.doubleEmptyChance += RngUtil.randomInclusive(1, 5);
            Microbot.log("doubleEmptyChance -> " + doubleEmptyChance);
        }
        return doubleEmpty;
    }
}
