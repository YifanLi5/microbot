package net.runelite.client.plugins.microbot.yfoo.yBlastFurnace;

import com.google.inject.Provides;
import net.runelite.api.ChatMessageType;
import net.runelite.api.events.ChatMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.yfoo.StateMachine.StateManager;
import net.runelite.client.plugins.microbot.yfoo.yBlastFurnace.Util.BFUtils;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@PluginDescriptor(
        name = PluginDescriptor.yfoo + "yBlastFurnace",
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

    private Script yBFScript;
    public static AtomicBoolean isShutdown = new AtomicBoolean();

    @Provides
    BFConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(BFConfig.class);
    }

    @Override
    protected void startUp() throws Exception {
        yBFScript = new Script(config);
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
            int numCoal = parseCoalBagMessage(message);
            BFUtils.setNumCoalInBag(numCoal);
        }
    }

    static final Pattern COAL_BAG_PATTERN = Pattern.compile(
            "contains (?:(\\d+)|one)"
    );

    public static int parseCoalBagMessage(String message) {
        if (message.contains("empty")) {
            Microbot.log("Coal bag -> 0");
            return 0;
        }

        Matcher matcher = COAL_BAG_PATTERN.matcher(message);
        if (matcher.find()) {
            if (matcher.group(1) != null) {
                int numCoal = Integer.parseInt(matcher.group(1));
                Microbot.log("Coal bag -> " + numCoal);
                return numCoal;
            }
            return 1;
        }

        if(message.equals("The coal bag can be filled only with coal. You haven't got any.")) {
            StateManager.stopScript();
            return 0;
        }

        throw new IllegalArgumentException("Invalid coal bag message: " + message);
    }
}


