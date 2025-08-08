package net.runelite.client.plugins.microbot.yfoo.camdozaal_mining;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.awt.*;

@PluginDescriptor(
        name = PluginDescriptor.yfoo + "yCamdozaalMining",
        description = "Microbot example plugin",
        tags = {"example", "microbot"},
        enabledByDefault = false
)
@Slf4j
public class CamdozaalMiningPlugin extends Plugin {
    @Inject
    private CamdozaalMiningConfig config;
    @Provides
    CamdozaalMiningConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(CamdozaalMiningConfig.class);
    }

    @Inject
    private OverlayManager overlayManager;
    @Inject
    private CamdozaalMiningOverlay exampleOverlay;

    @Inject
    CamdozaalMiningScript exampleScript;


    @Override
    protected void startUp() throws AWTException {
        if (overlayManager != null) {
            overlayManager.add(exampleOverlay);
            exampleOverlay.myButton.hookMouseListener();
        }
        exampleScript.run(config);
    }

    protected void shutDown() {
        exampleScript.shutdown();
        overlayManager.remove(exampleOverlay);
        exampleOverlay.myButton.unhookMouseListener();
    }
}
