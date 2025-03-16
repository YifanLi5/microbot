package net.runelite.client.plugins.microbot.yfoo.yBlastFurnace;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.plugins.microbot.yfoo.yBlastFurnace.Util.BFBarRecipes;

@ConfigGroup("yBlastFurnace")
public interface BFConfig extends Config {
    @ConfigItem(
            keyName = "barType",
            name = "Bar Type",
            description = "Select the type of bar to smelt at the Blast Furnace",
            position = 0
    )
    default BFBarRecipes barType() {
        return BFBarRecipes.STEEL_BAR;
    }
}
