package net.runelite.client.plugins.microbot.yfoo.GeneralUtil;

import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;

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

    public static boolean sleepUntilAnimStops() {
        AtomicLong lastAnimTs = new AtomicLong(System.currentTimeMillis());
        Callable<Boolean> successCondition = () -> {
            long temp = System.currentTimeMillis() - lastAnimTs.get();
            return temp >= 3000;
        };
        Callable<Boolean> extendCondition = () -> {
            if(Rs2Player.isAnimating()) {
                lastAnimTs.set(System.currentTimeMillis());
                return true;
            }
            return false;
        };

        return sleep(5000, successCondition, null, extendCondition);

    }
}
