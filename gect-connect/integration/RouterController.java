package integration;

import shared.EventBus;
import javax.swing.*;
import java.awt.*;
import shared.AnimationUtils;
import shared.DebugLogger;

/**
 * Controller responsible for screen routing and navigation.
 */
public class RouterController {
    private final JPanel mainContentPanel;
    private final CardLayout cardLayout;

    public RouterController(JPanel mainContentPanel, CardLayout cardLayout) {
        this.mainContentPanel = mainContentPanel;
        this.cardLayout = cardLayout;
    }

    /**
     * Navigate to a specific screen with a slide-in animation.
     * @param screenName The name of the card to show.
     */
    public void navigateTo(String screenName) {
        DebugLogger.info("UI ? Navigated to " + screenName + " - navigateTo() - RouterController.java");
        cardLayout.show(mainContentPanel, screenName);
        EventBus.getInstance().publish("ROUTE_CHANGED", screenName);
        
        // Apply Slide-in animation to the newly visible component
        for (Component comp : mainContentPanel.getComponents()) {
            if (comp.isVisible()) {
                AnimationUtils.slideIn(comp, 100, 0, 300);
                break;
            }
        }
    }

    /**
     * Show a screen without animation (e.g., initial load).
     */
    public void showImmediately(String screenName) {
        cardLayout.show(mainContentPanel, screenName);
        EventBus.getInstance().publish("ROUTE_CHANGED", screenName);
    }
}
