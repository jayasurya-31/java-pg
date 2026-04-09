package shared;

import java.awt.Image;
import java.net.URL;
import javax.swing.ImageIcon;

/**
 * Centralized icon manager for the application. Loads PNG icons from
 * resources/icons folder and scales them to default size, except main_logo
 * which can have separate width and height.
 */
public class Icons {

    private static final int ICON_SIZE = 24;
    private static final int LOGO_WIDTH = 60;  // customize main logo width
    private static final int LOGO_HEIGHT = 32; // customize main logo height
    private static final String RESOURCE_PATH = "/resources/icons/";

    /**
     * Gets a scaled ImageIcon for the specified key.
     *
     * @param key The icon key (case-insensitive)
     * @return A scaled ImageIcon, or an empty ImageIcon if not found
     */
    public static ImageIcon get(String key) {
        return get(key, ICON_SIZE);
    }

    /**
     * Gets a scaled ImageIcon with custom size.
     * Only main_logo can have width and height separately.
     */
    public static ImageIcon get(String key, int customSize) {
        if (key == null) return new ImageIcon();

        String filename;
        int size = customSize;
        switch (key.toLowerCase()) {
            case "main_logo":
                filename = "MainLogo.png";
                return load(filename, LOGO_WIDTH, LOGO_HEIGHT); // separate width & height
            case "watermark":  // new key for background watermark
                filename = "WaterMark.png";
                // Default larger size; you can override later in WatermarkPanel
                return load(filename, 400, 200);
            case "home":
            case "feed":
                filename = "home.png";
                break;
            case "chats":
            case "chat":
            case "media":
            case "emoji":
                filename = "chat.png";
                break;
            case "groups":
                filename = "group.png";
                break;
            case "notifications":
                filename = "bell.png";
                break;
            case "contacts":
                filename = "user.png";
                break;
            case "profile":
            case "user_avatar":
                filename = "user-circle.png";
                break;
            case "settings":
                filename = "settings.png";
                break;
            case "menu":
                filename = "menu.png";
                break;
            case "send":
                filename = "send.png";
                break;
            case "back":
                filename = "back.png";
                break;
            case "search":
                filename = "search.png";
                break;
            case "edit":
            case "add":
                filename = "edit.png";
                break;
            case "close":
                filename = "close.png";
                break;
            case "more":
                filename = "threedot.png";
                break;
            case "welcome":
            case "logo":
                filename = "welcome.png";
                break;
            case "show":
                filename = "show.png";
                break;
            default:
                return new ImageIcon();
        }

        return load(filename, size); // default square scaling
    }

    /**
     * Loads and scales an icon (square) from the resources folder.
     */
    private static ImageIcon load(String filename, int size) {
        return load(filename, size, size); // delegate to width/height version
    }

    /**
     * Loads and scales an icon (custom width & height) from the resources folder.
     */
    private static ImageIcon load(String filename, int width, int height) {
        try {
            URL imgURL = Icons.class.getResource(RESOURCE_PATH + filename);
            if (imgURL == null) {
                imgURL = Icons.class.getClassLoader().getResource("resources/icons/" + filename);
            }

            if (imgURL != null) {
                ImageIcon originalIcon = new ImageIcon(imgURL);
                Image scaledImg = originalIcon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
                return new ImageIcon(scaledImg);
            } else {
                System.err.println("❌ Icon NOT FOUND: " + filename);
            }
        } catch (Exception e) {
            System.err.println("❌ Error loading icon: " + filename);
        }
        return new ImageIcon();
    }
}