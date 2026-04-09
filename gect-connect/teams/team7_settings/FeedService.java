package teams.team7_settings;

import shared.EventBus;
import shared.DebugLogger;
import shared.SessionManager;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for Settings & Feed module logic.
 * Refactored to use SessionManager for current user context.
 */
public class FeedService {
    private final FeedDAO feedDAO;

    public FeedService() {
        DebugLogger.info("SETTINGS ? Initializing service - constructor() - FeedService.java");
        this.feedDAO = new FeedDAO();
    }

    /**
     * Post to feed for current user and notify.
     */
    public boolean post(String content) {
        return post(SessionManager.getInstance().getCurrentUserId(), content);
    }

    /**
     * Post to feed and notify.
     */
    public boolean post(int userId, String content) {
        DebugLogger.info("SETTINGS ? Posting to feed for user " + userId + " - post() - FeedService.java");
        boolean success = feedDAO.postToFeed(userId, content, null);
        if (success) {
            DebugLogger.info("SETTINGS ? Feed post SUCCESS - post() - FeedService.java");
            EventBus.getInstance().publish("FEED_POSTED", content);
        } else {
            DebugLogger.error("SETTINGS ? Feed post FAILED - post() - FeedService.java");
        }
        return success;
    }

    /**
     * Get feed items for current user.
     */
    public List<String> getFeed() {
        if (!SessionManager.getInstance().isLoggedIn()) return new ArrayList<>();
        return feedDAO.getFeed();
    }

    /**
     * Get feed items for specified user.
     */
    public List<String> getFeed(int userId) {
        DebugLogger.info("SETTINGS ? Fetching feed for user " + userId + " - getFeed() - FeedService.java");
        return feedDAO.getFeed(userId);
    }
}
