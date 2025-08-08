package net.runelite.client.plugins.microbot.yfoo.yBlastFurnace.States.Standard;

import net.runelite.api.GameObject;
import net.runelite.api.ItemID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.keyboard.Rs2Keyboard;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;
import net.runelite.client.plugins.microbot.yfoo.GeneralUtil.ExtendableConditionalSleep;
import net.runelite.client.plugins.microbot.yfoo.GeneralUtil.HoverBoundsUtil;
import net.runelite.client.plugins.microbot.yfoo.GeneralUtil.RngUtil;
import net.runelite.client.plugins.microbot.yfoo.StateMachine.StateNode;
import net.runelite.client.plugins.microbot.yfoo.yBlastFurnace.Script;

import java.util.LinkedHashMap;

import static net.runelite.client.plugins.microbot.util.Global.sleepUntil;

public class RefillCofferState extends StateNode {

    enum States {
        OPEN_BANK, DEPOSIT_BARS, WITHDRAW_CASH, DEPOSIT_INTO_COFFER, COMPLETE
    }

    private static RefillCofferState instance;

    public static RefillCofferState getInstance() {
        if(instance == null) {
            throw new NullPointerException(RefillCofferState.class.getSimpleName() + " is null");
        }
        return instance;
    }

    public static RefillCofferState initInstance(Script script) {
        if (instance == null)
            instance = new RefillCofferState(script);
        return instance;
    }

    public RefillCofferState(Script script) {
        super(script);
    }

    @Override
    public boolean canRun() throws InterruptedException {
        return true;
    }

    @Override
    public void initStateSteps() {
        this.stateSteps = new LinkedHashMap<>();
        this.stateSteps.put(States.OPEN_BANK, () ->  {
            if(Rs2Bank.isOpen()) {
                Microbot.log("Bank is already open");
                return true;
            }
            GameObject chest = Rs2GameObject.findBank();
            boolean result = Rs2GameObject.interact(chest, "use") && ExtendableConditionalSleep.sleep(5000, () -> Rs2Bank.isOpen(), null, () -> Rs2Player.isMoving());
            HoverBoundsUtil.hoverRandom();
            script.sleep(RngUtil.gaussian(400, 100, 100, 600));
            return result;
        });
        this.stateSteps.put(States.DEPOSIT_BARS, () -> {
            if(Rs2Inventory.onlyContains(ItemID.COAL_BAG_12019)) return true;
            if(Rs2Inventory.contains(item -> item.getName().contains("bar"))) {
                return Rs2Bank.depositAll(item -> item.getName().contains("bar"));
            }
            return true;
        });
        this.stateSteps.put(States.WITHDRAW_CASH, () -> {
            if(Rs2Bank.count(ItemID.COINS_995) < 50000) {
                Microbot.log("Insufficent gp in bank");
                return false;
            }
            boolean withdrewCoins = Rs2Bank.withdrawX(ItemID.COINS_995, 50000) && sleepUntil(() -> Rs2Inventory.contains(ItemID.COINS_995));
            if(!withdrewCoins) {
                Microbot.log("Failed to withdraw coins");
                return false;
            }
            return Rs2Bank.closeBank();
        });
        this.stateSteps.put(States.DEPOSIT_INTO_COFFER, () -> {
            if(!Rs2Inventory.use(ItemID.COINS_995)) {
                Microbot.log("Did not use coins");
                return false;
            }
            if(!Rs2GameObject.interact(29330, "Use")) {
                Microbot.log("Did use coins on coffer");
                return false;
            }
            boolean isVisible = sleepUntil(() -> Rs2Widget.isWidgetVisible(162, 42));
            if(!isVisible) {
                Microbot.log("Coffer deposit widget did not become visible");
                return false;
            }
            Rs2Keyboard.typeString("9".repeat(RngUtil.randomInclusive(5, 8)));
            Rs2Keyboard.enter();
            return sleepUntil(() -> !Rs2Inventory.contains(ItemID.COINS_995));
        });
    }

    @Override
    public boolean handleState() throws InterruptedException {
        int coffer = Microbot.getVarbitValue(VarbitID.BLAST_FURNACE_COFFER);
        if(coffer >= 50000) {
            return true;
        }
        Microbot.log("%d < 50000", coffer);
        return super.handleState();
    }

    @Override
    public StateNode nextState() throws InterruptedException {
        return BankState.getInstance();
    }
}
