package net.runelite.client.plugins.microbot.yfoo.GeneralUtil;

import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.util.Global;

import java.util.concurrent.ThreadLocalRandom;

public class IdleSleep {
    public static double idleChance;
    public static int sessionMean;
    public static int sessionStdDev;

    public static void randomizeFields() {
        idleChance = ThreadLocalRandom.current().nextDouble(0.01, 0.05);
        sessionMean = ThreadLocalRandom.current().nextInt(4000, 8000);
        sessionStdDev = sessionMean / ThreadLocalRandom.current().nextInt(8, 12);
    }

    public static boolean chanceIdleSleep() {
        boolean slept = Math.random() < idleChance;
        if(slept) {
            idleSleep(sessionMean, sessionStdDev);
        }
        return slept;
    }

    public static void idleSleep(int mean, int stddev) {
        int sleepTime = RngUtil.gaussian(mean, stddev, 0, 12000);
        Microbot.log("Idle for %dms", sleepTime);
        Global.sleep(sleepTime);
    }
}
