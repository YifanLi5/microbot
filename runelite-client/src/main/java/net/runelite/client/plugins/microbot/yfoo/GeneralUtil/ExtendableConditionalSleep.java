package net.runelite.client.plugins.microbot.yfoo.GeneralUtil;

import java.util.concurrent.Callable;

public class ExtendableConditionalSleep {
    public static boolean sleep(int sleepTime, Callable<Boolean> successCondition, Callable<Boolean> failCondition, Callable<Boolean> extendCondition) {
        sleepTime = sleepTime < 0 ? 1800: sleepTime;
        long startTime = System.currentTimeMillis();
        try {
            while (System.currentTimeMillis() - startTime < sleepTime) {
                if (successCondition.call()) {
                    return true;
                }

                if (failCondition != null && failCondition.call()) {
                    return false;
                }

                if (extendCondition != null && extendCondition.call()) {
                    sleepTime += 600;
                }

                Thread.sleep(600);
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

}
