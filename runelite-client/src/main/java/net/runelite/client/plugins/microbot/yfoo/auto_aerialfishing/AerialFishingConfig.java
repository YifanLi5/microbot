package net.runelite.client.plugins.microbot.yfoo.auto_aerialfishing;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("aerialFishing")
public interface AerialFishingConfig extends Config {
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
