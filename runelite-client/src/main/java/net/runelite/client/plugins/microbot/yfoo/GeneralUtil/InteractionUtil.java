package net.runelite.client.plugins.microbot.yfoo.GeneralUtil;

import net.runelite.api.GameObject;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;

import javax.annotation.Nonnull;

import static net.runelite.client.plugins.microbot.yfoo.camdozaal_scripts.Util.Constants.MAKE_STUFF_WIDGET_ROOT;
import static net.runelite.client.plugins.microbot.yfoo.camdozaal_scripts.Util.Constants.PREPARE_FISH_CHILD_WIDGET_ID;

public class InteractionUtil {

    public static boolean interactObjectAssertWidget(Script script, @Nonnull GameObject object, int widgetRoot, int widgetChild) throws InterruptedException {
        if(!Rs2GameObject.interact(object)) {
            Microbot.log("Failed interact with GameObject: " + object.getId());
            return false;
        }

        boolean foundWidget = false;
        long timeStamp = System.currentTimeMillis();
        while(System.currentTimeMillis() - timeStamp <= 4000) {
            if(Rs2Player.isMoving()) {
                timeStamp = System.currentTimeMillis();
            }
            if(Rs2Widget.isWidgetVisible(widgetRoot, widgetChild)) {
                Microbot.log("Bank is open");
                foundWidget = true;
                break;
            }
            Thread.sleep(300);
        }

        if(!foundWidget) {
            Microbot.log("offering widget did not become visible");
            return false;
        }
        return true;
    }

    public static boolean interactObjectHandleWidget(Script script, @Nonnull GameObject object, int widgetRoot, int widgetChild) throws InterruptedException {
        if(!interactObjectAssertWidget(script, object, widgetRoot, widgetChild)) {
            return false;
        }
        if (Rs2Player.isAnimating(1200)) {
            Microbot.log("Player immediately started animation without a widget interaction.");
            return true;
        }

        if(!Rs2Widget.clickWidget(widgetRoot, widgetChild)) {
            Microbot.log(String.format("Unable to click widget (%d, %d)", MAKE_STUFF_WIDGET_ROOT, PREPARE_FISH_CHILD_WIDGET_ID));
            return false;
        }
        return true;
    }

    public static boolean interactObjectAssertAnimation(Script script, @Nonnull GameObject object) {
        if(!Rs2GameObject.interact(object)) {
            Microbot.log("Failed interact with offering table");
            return false;
        }
        return script.sleepUntil(() -> Rs2Player.isAnimating(1200));
    }
}
