package net.runelite.client.plugins.microbot.example;

import net.runelite.api.ChatMessageType;
import net.runelite.api.Varbits;
import net.runelite.api.events.ChatMessage;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.yfoo.yBlastFurnace.Util.BFBarRecipes;
import net.runelite.client.plugins.microbot.yfoo.yBlastFurnace.Util.BFUtils;

import java.util.concurrent.TimeUnit;


public class ExampleScript extends Script {

    public static boolean test = false;
    BFUtils utils = new BFUtils();

    public boolean run(ExampleConfig config) {
        Microbot.enableAutoRunOn = false;
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn()) return;
                if (!super.run()) return;


            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }, 0, 1000, TimeUnit.MILLISECONDS);
        return true;
    }

    @Override
    public void shutdown() {
        super.shutdown();
    }


}