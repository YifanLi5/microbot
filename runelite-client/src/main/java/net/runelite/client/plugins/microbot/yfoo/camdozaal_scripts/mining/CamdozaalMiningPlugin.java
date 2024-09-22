package net.runelite.client.plugins.microbot.yfoo.camdozaal_scripts.mining;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.yfoo.Task.Task;
import net.runelite.client.plugins.microbot.yfoo.camdozaal_scripts.fishing.CamdozaalFishingConfig;
import net.runelite.client.plugins.microbot.yfoo.camdozaal_scripts.fishing.CamdozaalFishingOverlay;
import net.runelite.client.plugins.microbot.yfoo.camdozaal_scripts.fishing.CamdozaalFishingScript;
import net.runelite.client.plugins.microbot.yfoo.camdozaal_scripts.fishing.TaskSubclasses.CatchCamdozaalFish;
import net.runelite.client.plugins.microbot.yfoo.camdozaal_scripts.fishing.TaskSubclasses.PrepareCamdozaalFish;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.util.concurrent.TimeUnit;
@PluginDescriptor(
        name = PluginDescriptor.yfoo + "Camdozaal_Mining",
        description = "Mines Barronite rocks",
        tags = {"fishing", "aerial", "microbot"},
        enabledByDefault = false
)

@Slf4j
public class CamdozaalMiningPlugin extends Plugin {
    @Inject
    private CamdozaalMiningConfig config;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private CamdozaalMiningOverlay camdozaalFishingOverlay;

    @Provides
    CamdozaalMiningConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(CamdozaalMiningConfig.class);
    }

    private CamdozaalMiningScript camdozaalMiningScript;

    @Override
    protected void startUp() throws Exception {
        log.info("Camdozaal Mining Plugin started!");
        CamdozaalFishingOverlay.resetStats();
        overlayManager.add(camdozaalFishingOverlay);

        this.camdozaalMiningScript = new CamdozaalMiningScript(config);
        camdozaalMiningScript.run();
    }

    @Override
    protected void shutDown() throws Exception {
        log.info("Camdozaal Mining Plugin stopped!");
        if(camdozaalMiningScript != null)
            camdozaalMiningScript.shutdown();
        overlayManager.remove(camdozaalFishingOverlay);
        Task.cleanupTasks();
    }
}
