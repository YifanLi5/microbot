package net.runelite.client.plugins.microbot.yfoo.GeneralUtil;

import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.util.antiban.Rs2Antiban;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;

public class AnimationUtil {
    public static void waitUntilPlayerStopsAnimating(int timeSinceLastAnimation) throws InterruptedException {
        // assume player is currently animating, if they never animate during the timeout window, then the script simple sleeps for the timeout
        long lastAnimTimestamp = System.currentTimeMillis();
        do {
            if(Rs2Player.isAnimating() || Rs2Player.isMoving()) {
                lastAnimTimestamp = System.currentTimeMillis();
            }
            Thread.sleep(300);
        }
        while(System.currentTimeMillis() - lastAnimTimestamp <= timeSinceLastAnimation);
    }
}
