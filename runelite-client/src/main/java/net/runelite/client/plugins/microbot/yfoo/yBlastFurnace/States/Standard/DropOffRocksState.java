package net.runelite.client.plugins.microbot.yfoo.yBlastFurnace.States.Standard;

import net.runelite.api.ItemID;
import net.runelite.api.ObjectID;
import net.runelite.api.Varbits;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.keyboard.Rs2Keyboard;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.yfoo.GeneralUtil.ExtendableConditionalSleep;
import net.runelite.client.plugins.microbot.yfoo.GeneralUtil.HoverBoundsUtil;
import net.runelite.client.plugins.microbot.yfoo.GeneralUtil.IdleSleep;
import net.runelite.client.plugins.microbot.yfoo.StateMachine.StateNode;
import net.runelite.client.plugins.microbot.yfoo.yBlastFurnace.BFScript;
import net.runelite.client.plugins.microbot.yfoo.yBlastFurnace.Util.BFUtils;

import java.awt.event.KeyEvent;
import java.util.LinkedHashMap;
import java.util.concurrent.Callable;

public class DropOffRocksState extends StateNode {

    enum States {
        DROP_OFF_MAIN, DROP_OFF_COAL_BAG
    }

    private static boolean isDoubleCoal;
    private static DropOffRocksState instance;
    Callable<Boolean> successCondition = () -> !Rs2Inventory.isFull();
    Callable<Boolean> extendCondition = () -> Rs2Player.isMoving();

    public static DropOffRocksState getInstance() {
        if(instance == null) {
            throw new NullPointerException(DropOffRocksState.class.getSimpleName() + " is null");
        }
        return instance;
    }

    public static DropOffRocksState initInstance(BFScript script) {
        if (instance == null)
            instance = new DropOffRocksState(script);
        return instance;
    }

    public DropOffRocksState(BFScript script) {
        super(script);
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
        this.stateSteps.put(States.DROP_OFF_MAIN, () -> {
            isDoubleCoal = Rs2Inventory.contains(ItemID.COAL);
            if(Rs2Bank.isOpen()) Rs2Keyboard.keyPress(KeyEvent.VK_ESCAPE);
            if(Rs2Inventory.contains(item -> {
                String name = item.getName();
                return name.equals("Coal") || name.endsWith("ore");
            })) {
                Rs2GameObject.interact(ObjectID.CONVEYOR_BELT, "Put-ore-on");
                IdleSleep.idleSleep(1500, 500);
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
            if(!BFUtils.emptyCoalBag()) {
                Microbot.log("Failed empty coalbag 1");
                return false;
            }
            Rs2GameObject.interact(ObjectID.CONVEYOR_BELT, "Put-ore-on");
            boolean droppedCoal = ExtendableConditionalSleep.sleep(4000,
                    successCondition,
                    null,
                    extendCondition
            );
            if(!droppedCoal) {
                Microbot.log("Failed drop of coal 1");
                return false;
            }
            if(BFUtils.getNumCoalInBag() <= 0) {
                return true;
            }

            // For smithing cape
            if(!BFUtils.emptyCoalBag()) {
                Microbot.log("Failed empty coalbag 2");
                return false;
            }
            Rs2GameObject.interact(ObjectID.CONVEYOR_BELT, "Put-ore-on");
            droppedCoal = ExtendableConditionalSleep.sleep(4000,
                    successCondition,
                    null,
                    extendCondition
            );
            if(!droppedCoal) {
                Microbot.log("Failed drop of coal 2");
                return false;
            }
            return true;
        });
    }

    @Override
    public StateNode nextState() {
        if(Microbot.getVarbitValue(Varbits.BAR_DISPENSER) >= 2) return RetrieveBarsState.getInstance();
        return isDoubleCoal ? BankState.getInstance() : RetrieveBarsState.getInstance();
    }
}
