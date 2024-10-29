package net.runelite.client.plugins.microbot.yfoo.GeneralUtil;

import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;

public class AnimationUtil {
    public static void waitUntilPlayerStopsAnimating(int timeout) throws InterruptedException {
        // assume player is currently animating, if they never animate during the timeout window, then the script simple sleeps for the timeout
        long lastAnimTimestamp = System.currentTimeMillis();
        while(System.currentTimeMillis() - lastAnimTimestamp <= timeout) {
            if(Rs2Player.isAnimating()) {
                lastAnimTimestamp = System.currentTimeMillis();
            }
            Thread.sleep(300);
        }
    }
}
