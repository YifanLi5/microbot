package net.runelite.client.plugins.microbot.yfoo.GeneralUtil;

import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.shortestpath.ShortestPathPlugin;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.player.Rs2PlayerModel;
import net.runelite.client.plugins.microbot.util.tile.Rs2Tile;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
import net.runelite.client.plugins.microbot.util.walker.WalkerState;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static java.lang.Thread.sleep;
import static net.runelite.client.plugins.microbot.util.Global.sleepUntil;
import static net.runelite.client.plugins.microbot.util.Global.sleepUntilTrue;
import static net.runelite.client.plugins.microbot.util.walker.Rs2Walker.*;

public class CustomWalker {

    static WorldPoint currentTarget;

    public static boolean walkTo(WorldPoint target, int distance) {
        return walkWithState(target, distance) == WalkerState.ARRIVED;
    }

    public static WalkerState walkWithState(WorldPoint target, int distance) {
        if (Rs2Tile.getReachableTilesFromTile(Rs2Player.getWorldLocation(), distance).containsKey(target)
                || !Rs2Tile.isWalkable(LocalPoint.fromWorld(Microbot.getClient().getTopLevelWorldView(), target)) && Rs2Player.getWorldLocation().distanceTo(target) <= distance) {
            return WalkerState.ARRIVED;
        }
        if (ShortestPathPlugin.getPathfinder() != null && !ShortestPathPlugin.getPathfinder().isDone())
            return WalkerState.MOVING;
        if ((currentTarget != null && currentTarget.equals(target)) && ShortestPathPlugin.getMarker() != null)
            return WalkerState.MOVING;
        setTarget(target);
        ShortestPathPlugin.setReachedDistance(distance);

        if (Microbot.getClient().isClientThread()) {
            Microbot.log("Please do not call the walker from the main thread");
            return WalkerState.EXIT;
        }
        /**
         * When running the walkTo method from scripts
         * the code will run on the script thread
         * If you really like to run this on a seperate thread because you want to do
         * other actions while walking you can wrap the walkTo from within the script
         * on a seperate thread
         */
        return processShortWalk(target, distance);
    }

    private static WalkerState processShortWalk(WorldPoint target, int distance) {
        try {

            if (ShortestPathPlugin.getPathfinder() == null) {
                if (ShortestPathPlugin.getMarker() == null) {
                    setTarget(null);
                }
                boolean isInit = sleepUntilTrue(() -> ShortestPathPlugin.getPathfinder() != null, 100, 2000);
                if (!isInit) {
                    Microbot.log("Pathfinder took to long to initialize, exiting walker: 140");
                    setTarget(null);
                    return WalkerState.EXIT;
                }
            }
            if (!ShortestPathPlugin.getPathfinder().isDone()) {
                boolean isDone = sleepUntilTrue(() -> ShortestPathPlugin.getPathfinder().isDone(), 100, 10000);
                if (!isDone) {
                    System.out.println("Pathfinder took to long to calculate path, exiting: 149");
                    setTarget(null);
                    return WalkerState.EXIT;
                }
            }

            if (ShortestPathPlugin.getMarker() == null) {
                Microbot.log("marker is null, exiting: 156");
                setTarget(null);
                return WalkerState.EXIT;
            }

            if (ShortestPathPlugin.getPathfinder() == null) {
                Microbot.log("pathfinder is null, exiting: 162");
                setTarget(null);
                return WalkerState.EXIT;
            }

            List<WorldPoint> path = ShortestPathPlugin.getPathfinder().getPath();
            int pathSize = path.size();
            Microbot.log("Path size:" + pathSize);

            if (path.get(pathSize - 1).distanceTo(target) > config.reachedDistance()) {
                Microbot.log("Location impossible to reach");
                setTarget(null);
                return WalkerState.UNREACHABLE;
            }

            // Abort the walker if maxWalkAttempts is exceeded, likely the player got stuck somehow.
            // Each walk should be around 5 tiles, double it as an extra buffer.
            int walkAttempts = 0;
            int maxWalkAttempts = (path.size() / 5) * 2;
            for(; walkAttempts < maxWalkAttempts; walkAttempts++) {
                // Are we there yet?
                if(Rs2Player.getWorldLocation().distanceTo(target) <= distance) {
                    if(Rs2Tile.isTileReachable(target)) {
                        Microbot.log("Simple walker finished at destination");
                        break;
                    } else {
                        // try the door handler
                        Microbot.log("Close to destination but final tile is not reachable. ex: door needs to be opened");
                        int pathIdx = getClosestTileIndex(path);
                        handleNextDoorHelper(path, pathIdx, path.size()-1);
                        continue;
                    }
                }

                // Each loop looks ahead a random number of tiles in the path... (aka 'next-walk chunk')
                int pathIdx = getClosestTileIndex(path);
                int nextWalkIdx = Math.min(path.size() - 1, pathIdx + ThreadLocalRandom.current().nextInt(10, 15));
                int nextWalkingDistance = ThreadLocalRandom.current().nextInt(3, 8);

                // and checks if each of those tiles has a door that needs to be handled
                // if handleNextDoorHelper fails, retry
                if(handleNextDoorHelper(path, pathIdx, nextWalkIdx)) continue;

                // if there are no doors, minimap walk to the end of the 'next-walk chunk'
                WorldPoint nextPoint = path.get(nextWalkIdx);
                Microbot.log("Minimap Walk to: " + nextPoint);
                if(Rs2Walker.walkMiniMap(nextPoint)) {
                    sleepUntil(() -> nextPoint.distanceTo(Rs2Player.getWorldLocation()) < nextWalkingDistance);
                } else {
                    Microbot.log(String.format("Minimap walk to %s failed...", nextPoint));
                }
            }

            if(walkAttempts >= maxWalkAttempts) {
                return WalkerState.UNREACHABLE;
            }

            if (Rs2Player.getWorldLocation().distanceTo(target) <= distance) {
                setTarget(null);
                Microbot.log("Arrived");
                // invoking an interaction immediately after a previous one seems to cause the player to halt.
                // A small sleep here prevents that from happening.
                sleep(300);
                return WalkerState.ARRIVED;
            }
        } catch (Exception ex) {
            if (ex instanceof InterruptedException) {
                setTarget(null);
                return WalkerState.EXIT;
            }
            ex.printStackTrace(System.out);
            Microbot.log("Microbot Walker Exception " + ex.getMessage());
            System.out.println(ex.getMessage());
        }
        return WalkerState.EXIT;
    }

    private static boolean handleNextDoorHelper(List<WorldPoint> path, int startIdx, int endIdx) {
        endIdx = Math.min(path.size() - 1, endIdx);
        for(int idx = startIdx; idx < endIdx; idx++) {
            if(publicHandleDoors(path, idx)) {
                return true;
            }
        }
        return false;
    }
}
