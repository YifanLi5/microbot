package net.runelite.client.plugins.microbot.yfoo.camdozaal_mining;

import net.runelite.api.ItemID;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.util.ActorModel;
import net.runelite.client.plugins.microbot.util.coords.Rs2WorldArea;
import net.runelite.client.plugins.microbot.util.inventory.Rs2ItemModel;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Constants {
    public final static int[] depositItems = {
            ItemID.BARRONITE_HEAD,
            ItemID.IMCANDO_HAMMER_BROKEN
    };
    public final static int[] ancientJunk = {
            ItemID.ANCIENT_GLOBE,
            ItemID.ANCIENT_ASTROSCOPE,
            ItemID.ANCIENT_CARCANET,
            ItemID.ANCIENT_LEDGER,
            ItemID.ANCIENT_TREATISE
    };

    public final static Predicate<Rs2ItemModel> keepPredicate = item -> item.getName().contains("pickaxe") || item.getId() == ItemID.HAMMER || item.getId() == ItemID.BARRONITE_SHARDS || item.getId() == ItemID.LAMP;

    public final static WorldPoint bankLocation = new WorldPoint(2977, 5798, 0);

    public final static String BARRONITE_ROCKS = "Barronite rocks";
    public final static String MINE = "Mine";
    public final static Rs2WorldArea miningArea = new Rs2WorldArea(2900, 5800, 50, 25, 0);
    public final static List<WorldPoint> miningClusters = Arrays.asList(
            new WorldPoint(2910,5815,0),
            new WorldPoint(2914,5816,0),
            new WorldPoint(2919,5819,0),
            new WorldPoint(2924,5821,0),
            new WorldPoint(2928,5820,0),
            new WorldPoint(2940,5810,0),
            new WorldPoint(2937,5807,0),
            new WorldPoint(2911,5806,0),
            new WorldPoint(2906,5812,0)
    );

    public static WorldPoint getClosestCluster() {
        WorldPoint myPosition = Rs2Player.getWorldLocation();
        return miningClusters.stream()
                .min(Comparator.comparingInt(pp -> pp.distanceTo(myPosition)))
                .orElse(miningClusters.get(0));
    }

    public static WorldPoint getRandomUnpopulatedCluster() {
        List<WorldPoint> playerPositions = Rs2Player.getPlayers(p -> miningArea.contains(p.getWorldLocation()))
            .map(ActorModel::getWorldLocation)
            .collect(Collectors.toList());

        String debug = playerPositions.stream().map(wp -> wp.toString()). collect(Collectors.joining(", "));
        Microbot.log("Player locations: %s", debug);

        boolean nearbyPlayer = false;
        WorldPoint myPosition = Rs2Player.getWorldLocation();
        for(WorldPoint pPosition: playerPositions) {
            if(pPosition.distanceTo(myPosition) <= 3) {
                nearbyPlayer = true;
                break;
            }
        }
        if(!nearbyPlayer) return getClosestCluster();

        Collections.shuffle(miningClusters);
        return miningClusters.stream().filter(worldPoint -> {
            boolean noNearbyPlayers = true;
            for(WorldPoint position: playerPositions) {
                if(position.distanceTo(worldPoint) < 2) {
                    noNearbyPlayers = false;
                    break;
                }
            }
            return noNearbyPlayers;
        }).findFirst().orElse(miningClusters.get(0));
    }

    public final static String BARRONITE_DEPOSIT = "Barronite Deposit";
    public final static String BARRONITE_CRUSHER = "Barronite Crusher";
    public final static String HAMMER = "Hammer";
    public final static String SMITH = "Smith";

    public final static WorldPoint anvilLocation = new WorldPoint(2957, 5807, 0);
    public final static Rs2WorldArea smashArea = new Rs2WorldArea(2954, 5801, 12, 12, 0);
}
