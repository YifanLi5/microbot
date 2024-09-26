package net.runelite.client.plugins.microbot.yfoo.camdozaal_scripts.mining.TaskSubclasses;

import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.yfoo.Task.Task;

public class BankAndReturn extends Task {

    private static BankAndReturn instance;

    public static BankAndReturn getInstance() {
        if(instance == null) {
            throw new NullPointerException("CatchAerialFish is null");
        }
        return instance;
    }

    public static BankAndReturn initInstance(Script script) {
        instance = new BankAndReturn(script);
        return instance;
    }

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
        return false;
    }
}
