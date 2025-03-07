package net.runelite.client.plugins.microbot.yfoo.StateMachine;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;

public abstract class State {
    protected Script script;

    public State(Script script) {
        this.script = script;
        Microbot.log("Initialized state: " + this.getClass().getSimpleName());
    }


    public int retries() {
        return 3;
    }
    public abstract boolean checkRequirements() throws InterruptedException;
    public abstract boolean handleState() throws InterruptedException;
}
