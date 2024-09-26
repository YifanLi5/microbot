package net.runelite.client.plugins.microbot.yfoo.GeneralUtil;

import net.runelite.api.GameObject;
import net.runelite.api.NPC;
import net.runelite.api.WallObject;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class GetInteractableUtil {
    public static NPC getRandomNPC(Predicate<NPC> npcPredicate) {
        List<NPC> npcs = Rs2Npc.getNpcs().filter(npcPredicate).collect(Collectors.toList());
        if(npcs.isEmpty()) return null;
        return npcs.get(RngUtil.randomInclusive(0, npcs.size() - 1));
    }

    public static NPC getRandomNPC(int id) {
        List<NPC> npcs = Rs2Npc.getNpcs(id).collect(Collectors.toList());
        if(npcs.isEmpty()) return null;
        return npcs.get(RngUtil.randomInclusive(0, npcs.size() - 1));
    }

    public static List<GameObject> getGameObjects(Predicate<GameObject> gameObjectPredicate) {
        return Rs2GameObject.getGameObjects().stream().filter(gameObjectPredicate).collect(Collectors.toList());
    }

    public static GameObject getRandomGameObject(Predicate<GameObject> gameObjectPredicate) {
        List<GameObject> objects = getGameObjects(gameObjectPredicate);
        if(objects.isEmpty()) return null;
        return objects.get(RngUtil.randomInclusive(0, objects.size() - 1));
    }

    public static GameObject getRandomGameObject(int id) {
        List<GameObject> objects = getGameObjects(gameObject -> gameObject.getId() == id);
        if(objects.isEmpty()) return null;
        return objects.get(RngUtil.randomInclusive(0, objects.size() - 1));
    }

    public static List<WallObject> getWallObjects(Predicate<WallObject> wallObjectPredicate) {
        return Rs2GameObject.getWallObjects().stream().filter(wallObjectPredicate).collect(Collectors.toList());
    }

    public static WallObject getRandomWallObject(Predicate<WallObject> wallObjectPredicate) {
        List<WallObject> objects = getWallObjects(wallObjectPredicate);
        if(objects.isEmpty()) return null;
        return objects.get(RngUtil.randomInclusive(0, objects.size() - 1));
    }
}
