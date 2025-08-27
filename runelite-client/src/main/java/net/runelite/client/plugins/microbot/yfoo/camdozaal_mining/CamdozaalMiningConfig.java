package net.runelite.client.plugins.microbot.yfoo.camdozaal_mining;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("camdozaal_mining")
public interface CamdozaalMiningConfig extends Config {
    @ConfigItem(
            keyName = "dropDeposits",
            name = "Drop Barronite Deposits",
            description = "Whether to drop barronite deposits. Sacrifices smithing xp and items for faster mining xp."
    )

    default boolean dropDeposits()
    {
        return false;
    }
}
