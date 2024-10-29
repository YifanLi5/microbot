package net.runelite.client.plugins.microbot.yfoo.camdozaal_scripts.fishing;

import net.runelite.client.plugins.microbot.yfoo.auto_aerialfishing.AerialFishingPlugin;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import javax.inject.Inject;
import java.awt.*;

public class CamdozaalFishingOverlay extends OverlayPanel {

    private static long startTime;
    private final String version = "0.02";
    private final String nameAndVersion = String.format("Camdozaal Fishing v%s", version);

    @Inject
    CamdozaalFishingOverlay(CamdozaalFishingPlugin plugin)
    {
        super(plugin);;
        setPosition(OverlayPosition.TOP_LEFT);
        setNaughty();
        resetStats();
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        panelComponent.setPreferredSize(new Dimension(300, 300));
        panelComponent.getChildren().add(TitleComponent.builder()
                .text(nameAndVersion)
                .color(Color.CYAN)
                .build());

        panelComponent.getChildren().add(LineComponent.builder().build());

        panelComponent.getChildren().add(TitleComponent.builder()
                .text("⏱️ " + formatTime(System.currentTimeMillis() - startTime))
                .build());

        return super.render(graphics);
    }

    public static void resetStats() {
        startTime = System.currentTimeMillis();
    }

    private String formatTime(final long ms) {
        long s = ms / 1000, m = s / 60, h = m / 60;
        s %= 60;
        m %= 60;
        h %= 24;
        return String.format("%02d:%02d:%02d", h, m, s);
    }
}
