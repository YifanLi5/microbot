package net.runelite.client.plugins.microbot.yfoo.camdozaal_scripts.fishing;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("CamdozaalFishing")
public interface CamdozaalFishingConfig extends Config {
    @ConfigItem(
            keyName = "guide",
            name = "How to use",
            description = "How to use this plugin",
            position = 0
    )
    default String GUIDE() {
        return (
                "Start script near fishing spots in Camdozaal ruins"
        );
    }
}
