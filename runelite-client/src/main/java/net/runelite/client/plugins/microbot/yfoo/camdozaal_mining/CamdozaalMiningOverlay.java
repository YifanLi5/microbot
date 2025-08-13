package net.runelite.client.plugins.microbot.yfoo.camdozaal_mining;

import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.yfoo.StateMachine.StateEventDispatcher;
import net.runelite.client.plugins.microbot.yfoo.StateMachine.StateEventSubscriber;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.ButtonComponent;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import javax.inject.Inject;
import java.awt.*;

public class CamdozaalMiningOverlay extends OverlayPanel implements StateEventSubscriber {

    private String stateName;
    private String stepName;

    @Inject
    CamdozaalMiningOverlay(CamdozaalMiningPlugin plugin)
    {
        super(plugin);
        setPosition(OverlayPosition.TOP_LEFT);
        setNaughty();

        this.stateName = "initializing...";
        this.stepName = "initializing...";
    }
    @Override
    public Dimension render(Graphics2D graphics) {
        try {
            panelComponent.setPreferredSize(new Dimension(200, 300));
            panelComponent.getChildren().add(TitleComponent.builder()
                    .text("yCamdozaalMining v0.1")
                    .color(Color.GREEN)
                    .build());

            panelComponent.getChildren().add(LineComponent.builder().build());

            panelComponent.getChildren().add(LineComponent.builder()
                    .left("State").right(stateName)
                    .build());

            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Step").right(stepName)
                    .build());


        } catch(Exception ex) {
            System.out.println(ex.getMessage());
        }
        return super.render(graphics);
    }

    @Override
    public void onEvent(String stateName, String stepName) {
        this.stateName = stateName;
        this.stepName = stepName;
    }
}
