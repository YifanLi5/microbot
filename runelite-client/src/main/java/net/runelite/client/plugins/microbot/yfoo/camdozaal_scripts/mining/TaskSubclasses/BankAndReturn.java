package net.runelite.client.plugins.microbot.yfoo.camdozaal_scripts.mining.TaskSubclasses;

import net.runelite.api.GameObject;
import net.runelite.api.Item;
import net.runelite.api.ItemID;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Item;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;
import net.runelite.client.plugins.microbot.yfoo.GeneralUtil.InteractionUtil;
import net.runelite.client.plugins.microbot.yfoo.GeneralUtil.RngUtil;
import net.runelite.client.plugins.microbot.yfoo.Task.Task;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

public class BankAndReturn extends Task {

    private enum BankingState {
        WALK_TO_CHEST, OPEN_BANK, DEPOSIT, RETURN_TO_MINE
    }

    private static BankAndReturn instance;

    public static BankAndReturn getInstance() {
        if(instance == null) {
            throw new NullPointerException("CatchAerialFish is null");
        }
        return instance;
    }

    public static BankAndReturn initInstance(Script script) {
        instance = new BankAndReturn(script);
        Microbot.log("passed in script: " + script.hashCode());
        return instance;
    }

    private static final WorldPoint chestPosition = new WorldPoint(2975, 5797, 0);

    private static final List<WorldPoint> randomMinePositions = Arrays.asList(
            new WorldPoint(2929,5818,0),
            new WorldPoint(2938,5809,0),
            new WorldPoint(2911,5810,0),
            new WorldPoint(2916,5815,0)
    );

    private static final Predicate<Rs2Item> depositPredicate = item -> item.getName().startsWith("Ancient") || item.getName().startsWith("Uncut") || item.getId() == ItemID.BARRONITE_HEAD;

    public BankAndReturn(Script script) {
        super(script);
    }

    @Override
    public boolean shouldRun() throws InterruptedException {
        // manually triggered after smash
        return false;
    }

    @Override
    public boolean runTask() throws InterruptedException {
        BankingState state = Rs2Inventory.contains(depositPredicate) ? BankingState.WALK_TO_CHEST : BankingState.RETURN_TO_MINE;
        WorldPoint returnPoint = randomMinePositions.get(RngUtil.randomInclusive(0, randomMinePositions.size()-1));
        int numFails = 0;
        Microbot.log(String.format("Inside class. (%d) Is script running? %s", script.hashCode(), script.isRunning()));

        while(numFails < 3 && script.isRunning()) {
            Microbot.log("BankingState: " + state);
            switch (state) {
                case WALK_TO_CHEST:
                    boolean result = Rs2Walker.walkToAndInteract(chestPosition, object -> object.getId() == 41493, 7);
                    if(result) {
                        Microbot.log("Success!");
                        state = BankingState.OPEN_BANK;
                    } else {
                        Microbot.log("Fail");
                        numFails++;
                        script.sleep(600);
                        continue;
                    }

                    break;

                case OPEN_BANK:
                    if(Rs2Bank.isOpen()) {
                        state = BankingState.DEPOSIT;
                        continue;
                    }
                    GameObject chest = Rs2GameObject.findChest();
                    if(chest == null) {
                        Microbot.log("Bank Chest is null");
                        numFails++;
                        script.sleep(600);
                        continue;
                    }

                    if(!Rs2GameObject.interact(chest)) {
                        Microbot.log("Failed interact with GameObject: " + chest.getId());
                        return false;
                    }

                    boolean foundWidget = false;
                    long timeStamp = System.currentTimeMillis();
                    while(System.currentTimeMillis() - timeStamp <= 4000) {
                        if(Rs2Player.isMoving()) {
                            timeStamp = System.currentTimeMillis();
                        }
                        if(Rs2Bank.isOpen()) {
                            Microbot.log("Bank is open");
                            foundWidget = true;
                            break;
                        }
                        Thread.sleep(300);
                    }

                    if(!foundWidget) {
                        Microbot.log("Failed to interact with bank chest and assert the bank is open");
                        numFails++;
                        script.sleep(600);
                        continue;
                    }

                    state = BankingState.DEPOSIT;
                    break;

                case DEPOSIT:
                    if(!Rs2Bank.depositAllExcept(2347, 25676)) {
                        Microbot.log("Deposit all failed");
                        numFails++;
                        script.sleep(600);
                        continue;
                    }

                    state = BankingState.RETURN_TO_MINE;
                    break;

                case RETURN_TO_MINE:
                    boolean nearReturn = Rs2Player.getWorldLocation().distanceTo(returnPoint) <= 7;
                    if(!nearReturn) {
                        Rs2Walker.walkTo(returnPoint, 7);
                        script.sleep(1000);
                        continue;
                    }

                    Task.setNextTask(MineRocks.getInstance());
                    return true;
            }

        }
        return false;
    }
}
