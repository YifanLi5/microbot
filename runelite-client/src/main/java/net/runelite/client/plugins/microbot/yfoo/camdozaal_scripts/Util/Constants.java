package net.runelite.client.plugins.microbot.yfoo.camdozaal_scripts.Util;

import net.runelite.api.ItemID;

import java.util.Arrays;
import java.util.List;

public class Constants {
    public static final List<Integer> PREPARED_CAMDOZAAL_FISH = Arrays.asList(ItemID.GUPPY, ItemID.CAVEFISH, ItemID.TETRA, ItemID.CATFISH);
    public static final List<Integer> CAMDOZAAL_FISH = Arrays.asList(ItemID.RAW_GUPPY, ItemID.RAW_CAVEFISH, ItemID.RAW_TETRA, ItemID.RAW_CAVEFISH);

    // Widgets
    public static final int MAKE_STUFF_WIDGET_ROOT = 270;
    public static final int PREPARE_FISH_CHILD_WIDGET_ID = 14;

    // Game Objects
    public static final int PREPARATION_TABLE = 41545;
    public static final int OFFERING_TABLE = 41546;
}
