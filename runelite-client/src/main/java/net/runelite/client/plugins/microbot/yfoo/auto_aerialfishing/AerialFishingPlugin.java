package net.runelite.client.plugins.microbot.yfoo.auto_aerialfishing;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.util.equipment.Rs2Equipment;
import net.runelite.client.plugins.microbot.util.inventory.Rs2ItemModel;
import net.runelite.client.plugins.microbot.yfoo.Task.Task;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;


@PluginDescriptor(
        name = PluginDescriptor.yfoo + "Auto Aerial Fishing",
        description = "Automates aerial fishing on molch island",
        tags = {"fishing", "aerial", "microbot"},
        enabledByDefault = false
)

@Slf4j
public class AerialFishingPlugin extends Plugin {
    @Inject
    private AerialFishingConfig config;

    @Inject
    private OverlayManager overlayManager;

    private AerialFishingScript aerialFishingScript;

    @Inject
    private AerialFishingOverlay aerialFishingOverlay;

    @Provides
    AerialFishingConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(AerialFishingConfig.class);
    }

    @Override
    protected void startUp() throws Exception {
        if(!config.allowFastFillet() && !config.allowSlowFillet()) {
            Microbot.log("One of Fast/Slow fillet must be checked!. Reset then restart the script.");
            return;
        }
        Rs2ItemModel equippedWeapon = Rs2Equipment.get(EquipmentInventorySlot.WEAPON);
        int weaponId = equippedWeapon != null ? equippedWeapon.getId() : -1;
        if(weaponId != 22817) {
            Microbot.log("Player is not wielding the bird. Get one from the Alry the Angler then restart the script.");
            return;
        }

        log.info("Aerial Fishing Plugin started!");
        AerialFishingOverlay.resetStats();
        overlayManager.add(aerialFishingOverlay);
        this.aerialFishingScript = new AerialFishingScript(config);
        aerialFishingScript.run();
    }

    @Override
    protected void shutDown() throws Exception {
        log.info("Aerial Fishing Plugin stopped!");
        if(aerialFishingScript != null)
            aerialFishingScript.shutdown();
        overlayManager.remove(aerialFishingOverlay);
        Task.cleanupTasks();
    }
}
