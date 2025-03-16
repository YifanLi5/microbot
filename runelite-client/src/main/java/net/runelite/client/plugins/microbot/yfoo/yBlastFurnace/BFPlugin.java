package net.runelite.client.plugins.microbot.yfoo.yBlastFurnace;

import com.google.inject.Provides;
import net.runelite.api.ChatMessageType;
import net.runelite.api.events.ChatMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.yfoo.yBlastFurnace.Util.BFUtils;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.util.concurrent.atomic.AtomicBoolean;

@PluginDescriptor(
        name = PluginDescriptor.yfoo + "Blast Furnace",
        description = "my blast furnace plugin",
        tags = {"smithing"},
        enabledByDefault = false
)
public class BFPlugin extends Plugin {

    @Inject
    private BFConfig config;
    @Inject
    private BFOverlay yBFOverlay;
    @Inject
    private OverlayManager overlayManager;

    private BFScript yBFScript;
    public static AtomicBoolean isShutdown = new AtomicBoolean();

    @Provides
    BFConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(BFConfig.class);
    }

    @Override
    protected void startUp() throws Exception {
        yBFScript = new BFScript(config);
        isShutdown.set(false);
        yBFScript.run();
        Microbot.log("Startup yBlastFurnace");
        if (overlayManager != null) {
            overlayManager.add(yBFOverlay);
        }
    }

    @Override
    protected void shutDown() throws Exception {
        Microbot.log("Shutting down yBlastFurnace");
        isShutdown.set(true);
        yBFScript.shutdown();
        overlayManager.remove(yBFOverlay);
    }

    @Subscribe
    public void onChatMessage(ChatMessage chatMessage) {
        if (chatMessage.getType() != ChatMessageType.GAMEMESSAGE) {
            return;
        }
        String message = chatMessage.getMessage();
        if(message.contains("coal bag")) {
            if(message.contains("empty")) {
                Microbot.log("Got Coal bag empty msg");
                BFUtils.setCoalBagFilled(false);
            } else if(message.contains("contains")) {
                Microbot.log("Got Coal bag full msg");
                BFUtils.setCoalBagFilled(true);
            }
        }
    }
}


