package net.runelite.client.plugins.microbot.yfoo.Task;

import lombok.extern.slf4j.Slf4j;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;

import java.util.ArrayList;

import static net.runelite.client.plugins.microbot.Microbot.log;
@Slf4j
public abstract class Task {

    protected Script script;
    public static boolean stopScriptNow = false;
    protected static Task nextTask;

    private static ArrayList<Task> subclassInstances;
    private static int consecutiveFailsCounter;

    public static void resetStatics() {
        stopScriptNow = false;
        subclassInstances = new ArrayList<>();
        consecutiveFailsCounter = 0;
        nextTask = null;
    }

    public Task(Script script) {
        this.script = script;
        subclassInstances.add(this);
        log.info("Initialized task instance of type: {}", this.getClass().getCanonicalName());
        consecutiveFailsCounter = 0;
    }

    public abstract boolean shouldRun() throws InterruptedException;

    public abstract boolean runTask() throws InterruptedException;

    public static void runLoopIteration(Script script) throws InterruptedException {
        if (consecutiveFailsCounter >= 3) {
            Task.stopScriptNow = true;
            Microbot.log("Stopping script, consecutiveFailsCounter >= 3");
            script.sleep(1000);
        }

        if (Task.stopScriptNow) {
            Microbot.log("called script.shutdown()");
            script.shutdown();
            return;
        }

        Task task = Task.nextTask();
        if(task == null) {
            log(String.format("could not resolve a nextTask to run. ConsecutiveFailsCounter -> %s",
                    ++consecutiveFailsCounter)
            );
            script.sleep(1000);

            return;
        }
        Microbot.log("Running: " + task.getClass().getSimpleName());
        boolean result = task.runTask();
        if(result) {
            if(consecutiveFailsCounter > 0) log("Succeeded last task. consecutiveFailsCounter -> 0.");
            consecutiveFailsCounter = 0;
        } else ++consecutiveFailsCounter;



        if(!result) {
            log(String.format("Failed task %s. ConsecutiveFailCounter: %d/5. Rerunning same task",
                    task.getClass().getSimpleName(),
                    consecutiveFailsCounter
            ));
            setNextTask(task);
            script.sleep(1000);
        }
    }

    public static void cleanupTasks() {
        Microbot.log("Clearing " + subclassInstances.size());
        subclassInstances.clear();
        subclassInstances = null;
    }

    private static Task nextTask() throws InterruptedException {
        if(Task.nextTask != null) {
            Task temp = Task.nextTask;
            Task.nextTask = null;
            return temp;
        }

        Task nextTask = null;
        for (Task task : Task.subclassInstances) {
            if (task.shouldRun()) {
                nextTask = task;
                break;
            }
        }
        return nextTask;
    }

    protected static void setNextTask(Task nextTask) {
        Task.nextTask = nextTask;
    }
}
