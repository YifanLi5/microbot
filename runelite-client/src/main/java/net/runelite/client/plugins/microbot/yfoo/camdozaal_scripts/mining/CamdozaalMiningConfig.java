package net.runelite.client.plugins.microbot.yfoo.camdozaal_scripts.mining;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("CamdozaalMining")
public interface CamdozaalMiningConfig extends Config {
    @ConfigItem(
            keyName = "guide",
            name = "How to use",
            description = "How to use this plugin",
            position = 0
    )
    default String GUIDE() {
        return (
                "Start script near mining veins in Camdozaal ruins"
        );
    }

    @ConfigItem(
            keyName = "DropDeposits",
            name = "Drop Barronite Deposit",
            description = "Should player drop the barronite chunks",
            position = 1
    )
    default boolean dropBarroniteDeposits() {
        return false;
    }
}
