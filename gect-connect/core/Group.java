package core;

import java.sql.Timestamp;

/**
 * Model for Group Chat information.
 */
public class Group {
    private final int id;
    private final String name;
    private final String description;
    private final int creatorId;
    private final Timestamp createdAt;

    public Group(int id, String name, String description, int creatorId, Timestamp createdAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.creatorId = creatorId;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getCreatorId() { return creatorId; }
    public Timestamp getCreatedAt() { return createdAt; }
}
