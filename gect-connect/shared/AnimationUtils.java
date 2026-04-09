package shared;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

/**
 * Utility for Swing animations (Fade-in, Slide, Shake).
 */
public class AnimationUtils {

    /**
     * Animation: Fade-in effect by gradually increasing opacity (if supported)
     * or by a timer-based visibility transition.
     */
    public static void fadeIn(Component component, int durationMs) {
        if (component instanceof UIUtils.FadePanel) {
            UIUtils.FadePanel panel = (UIUtils.FadePanel) component;
            final int steps = 20;
            final int delay = durationMs / steps;
            final float delta = 1.0f / steps;

            Timer timer = new Timer(delay, null);
            timer.addActionListener(new java.awt.event.ActionListener() {
                float alpha = 0.0f;
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    alpha += delta;
                    if (alpha >= 1.0f) {
                        panel.setAlpha(1.0f);
                        ((Timer)e.getSource()).stop();
                    } else {
                        panel.setAlpha(alpha);
                    }
                }
            });
            timer.start();
        } else {
            // Fallback for non-FadePanel: simple visibility
            component.setVisible(true);
            component.repaint();
        }
    }

    /**
     * Animation: Fade-out effect.
     */
    public static void fadeOut(Component component, int durationMs, Runnable onComplete) {
        if (component instanceof UIUtils.FadePanel) {
            UIUtils.FadePanel panel = (UIUtils.FadePanel) component;
            final int steps = 20;
            final int delay = durationMs / steps;
            final float delta = 1.0f / steps;

            Timer timer = new Timer(delay, null);
            timer.addActionListener(new java.awt.event.ActionListener() {
                float alpha = 1.0f;
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    alpha -= delta;
                    if (alpha <= 0.0f) {
                        panel.setAlpha(0.0f);
                        ((Timer)e.getSource()).stop();
                        if (onComplete != null) onComplete.run();
                    } else {
                        panel.setAlpha(alpha);
                    }
                }
            });
            timer.start();
        } else {
            component.setVisible(false);
            if (onComplete != null) onComplete.run();
        }
    }

    /**
     * Animation: Slide effect for moving a panel into view.
     */
    public static void slideIn(Component component, int fromX, int toX, int durationMs) {
        final int steps = 30;
        final int delay = durationMs / steps;
        final int deltaX = (toX - fromX) / steps;
        
        Point pos = component.getLocation();
        component.setLocation(fromX, pos.y);
        
        Timer timer = new Timer(delay, null);
        timer.addActionListener(new java.awt.event.ActionListener() {
            int currentStep = 0;
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (currentStep < steps) {
                    component.setLocation(component.getX() + deltaX, component.getY());
                    currentStep++;
                } else {
                    component.setLocation(toX, component.getY());
                    ((Timer)e.getSource()).stop();
                }
            }
        });
        timer.start();
    }

    /**
     * Animation: Shake effect for errors.
     */
    public static void shake(Component component) {
        final Point point = component.getLocation();
        final int delay = 20;
        final int distance = 5;
        
        new Thread(() -> {
            try {
                for (int i = 0; i < 5; i++) {
                    component.setLocation(point.x + distance, point.y);
                    Thread.sleep(delay);
                    component.setLocation(point.x - distance, point.y);
                    Thread.sleep(delay);
                }
                component.setLocation(point);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }).start();
    }
}
