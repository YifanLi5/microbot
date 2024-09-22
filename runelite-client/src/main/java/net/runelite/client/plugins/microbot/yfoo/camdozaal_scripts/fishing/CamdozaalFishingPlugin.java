package net.runelite.client.plugins.microbot.yfoo.camdozaal_scripts.fishing;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.microbot.yfoo.Task.Task;

import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;

@PluginDescriptor(
        name = PluginDescriptor.yfoo + "Camdozaal_Fishing",
        description = "Fish -> Prepare -> Offers the fish in the Camdozaal cavern",
        tags = {"fishing", "aerial", "microbot"},
        enabledByDefault = false
)
@Slf4j
public class CamdozaalFishingPlugin extends Plugin {

    @Inject
    private CamdozaalFishingConfig config;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private CamdozaalFishingOverlay camdozaalFishingOverlay;

    @Provides
    CamdozaalFishingConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(CamdozaalFishingConfig.class);
    }

    private CamdozaalFishingScript camdozaalFishingScript;

    @Override
    protected void startUp() throws Exception {
        log.info("Camdozaal Fishing Plugin started!");
        CamdozaalFishingOverlay.resetStats();
        overlayManager.add(camdozaalFishingOverlay);

        this.camdozaalFishingScript = new CamdozaalFishingScript(config);
        camdozaalFishingScript.run();
    }

    @Override
    protected void shutDown() throws Exception {
        log.info("Camdozaal Fishing Plugin stopped!");
        if(camdozaalFishingScript != null)
            camdozaalFishingScript.shutdown();
        overlayManager.remove(camdozaalFishingOverlay);
        Task.cleanupTasks();
    }
}
