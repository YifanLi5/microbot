package net.runelite.client.plugins.microbot.yfoo.GeneralUtil;

import net.runelite.api.GameObject;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.inventory.Rs2ItemModel;
import net.runelite.client.plugins.microbot.util.misc.Rs2UiHelper;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class HoverBoundsUtil {

    private static HashMap<String, Rectangle> hoverBounds;

    public static void init() {
        hoverBounds = new HashMap<>();
    }

    public static void hover(String key)  {
        if (!hoverBounds.containsKey(key)) {
            Microbot.log("WARN: hoverBounds does not contain key: " + key);
            return;
        }
        Microbot.log("Hover: " + key);
        try {
            Thread.sleep(RngUtil.gaussian(500, 200, 100, 1000));
        } catch (InterruptedException e) {
            return;
        }
        Rectangle bounds = hoverBounds.get(key);
        Point randomPoint = getGaussianPointInRectangle(bounds);
        Microbot.naturalMouse.moveTo(randomPoint.x, randomPoint.y);
    }

    public static void hoverRandom() {
        if(ThreadLocalRandom.current().nextInt(100) < 65) return;
        if (hoverBounds.isEmpty()) {
            Microbot.log("WARN: hoverBounds is empty, cannot hover randomly.");
            return;
        }

        // Convert keySet to a List for direct indexing
        List<String> keys = new ArrayList<>(hoverBounds.keySet());
        int randomIndex = ThreadLocalRandom.current().nextInt(keys.size());

        String randomKey = keys.get(randomIndex);
        hover(randomKey);
    }


    public static void addHoverBounds(String name, GameObject gameObject) {
        Rectangle bounds = Rs2UiHelper.getObjectClickbox(gameObject);
        hoverBounds.put(name, bounds);
    }

    public static void addInventoryItemHoverBounds(Rs2ItemModel itemModel) {
        Rectangle bounds = Rs2Inventory.itemBounds(itemModel);
        hoverBounds.put(itemModel.getName(), bounds);
    }

    public static void addBankItemHoverHounds(Rs2ItemModel itemModel) {
        Rectangle bounds = Rs2Bank.itemBounds(itemModel);
        hoverBounds.put(itemModel.getName(), bounds);
    }

    private static Point getGaussianPointInRectangle(Rectangle rect) {
        int meanX = rect.x + rect.width / 2;
        int meanY = rect.y + rect.height / 2;
        int stddevX = rect.width / 6;  // Approx. 99.7% of values within bounds
        int stddevY = rect.height / 6;

        int gaussianX = RngUtil.gaussian(meanX, stddevX, rect.x, rect.x + rect.width);
        int gaussianY = RngUtil.gaussian(meanY, stddevY, rect.y, rect.y + rect.height);

        return new Point(gaussianX, gaussianY);
    }
}
