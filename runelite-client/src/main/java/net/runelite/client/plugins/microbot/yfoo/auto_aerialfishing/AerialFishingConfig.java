package net.runelite.client.plugins.microbot.yfoo.auto_aerialfishing;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("aerialFishing")
public interface AerialFishingConfig extends Config {

    @ConfigItem(
            keyName = "guide",
            name = "How to use",
            description = "How to use this plugin",
            position = 0
    )
    default String GUIDE() {
        return (
                "1. Go to Molch island. Grab the cormorant from the NPC.\n" +
                "2. If you don't have bait or a knife, grab them from the island.\n" +
                "3. Start the plugin at the edge of the water."
        );
    }

    @ConfigItem(
            keyName = "allowFastFillet",
            name = "Allow Fast Fillet",
            description = "Enable/Disable use the knife -> fish repeatedly",
            position = 1

    )
    default boolean allowFastFillet() {
        return true;  // Default is unchecked
    }

    @ConfigItem(
            keyName = "allowSlowFillet",
            name = "Allow Slow Fillet",
            description = "Enable/Disable use knife -> fish once, then AFK",
            position = 2
    )
    default boolean allowSlowFillet() {
        return true;  // Default is unchecked
    }
}
