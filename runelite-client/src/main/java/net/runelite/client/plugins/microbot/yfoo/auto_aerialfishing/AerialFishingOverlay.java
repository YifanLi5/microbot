package net.runelite.client.plugins.microbot.yfoo.auto_aerialfishing;

import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.example.ExamplePlugin;
import net.runelite.client.plugins.microbot.fishing.eel.EelFishingScript;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import javax.inject.Inject;
import java.awt.*;

public class AerialFishingOverlay extends OverlayPanel {

    private final String nameAndVersion = String.format("Auto Aerial v%s", AerialFishingScript.version);
    private static long startTime;
    private static int numCatches;

    @Inject
    AerialFishingOverlay(AerialFishingPlugin plugin)
    {
        super(plugin);;
        setPosition(OverlayPosition.TOP_LEFT);
        setNaughty();
        resetStats();
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        try {
            panelComponent.setPreferredSize(new Dimension(100, 300));
            panelComponent.getChildren().add(TitleComponent.builder()
                    .text("Auto Aerial")
                    .color(Color.CYAN)
                    .build());

            panelComponent.getChildren().add(LineComponent.builder().build());

            panelComponent.getChildren().add(TitleComponent.builder()
                    .text("⏱️ " + formatTime(System.currentTimeMillis() - startTime))
                    .build());

            panelComponent.getChildren().add(TitleComponent.builder()
                    .text(String.format("<>< caught: %d", numCatches))
                    .build());
        } catch(Exception ex) {
            System.out.println(ex.getMessage());
        }
        return super.render(graphics);
    }

    public static void incrementNumCatches() {
        numCatches += 1;
    }

    private String formatTime(final long ms) {
        long s = ms / 1000, m = s / 60, h = m / 60;
        s %= 60;
        m %= 60;
        h %= 24;
        return String.format("%02d:%02d:%02d", h, m, s);
    }

    public static void resetStats() {
        startTime = System.currentTimeMillis();
        numCatches = 0;
    }
}
