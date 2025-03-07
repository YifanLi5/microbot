package net.runelite.client.plugins.microbot.yfoo.FishNFight;

import com.google.inject.Provides;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.microbot.Microbot;

import javax.inject.Inject;

@PluginDescriptor(
        name = PluginDescriptor.yfoo + "Fish N' Fight",
        description = "asdf",
        tags = {"fishing"},
        enabledByDefault = false
)
public class FishNFightPlugin extends Plugin {

    @Inject
    private FishNFightConfig config;
    private FishNFightScript fishNFightScript;

    @Provides
    FishNFightConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(FishNFightConfig.class);
    }

    @Override
    protected void startUp() throws Exception {
        fishNFightScript = new FishNFightScript(config);
        fishNFightScript.run();
        Microbot.log("Startup FishNFight");
    }

    @Override
    protected void shutDown() throws Exception {
        Microbot.log("Shutting down FishNFight");
        fishNFightScript.shutdown();
    }
}
