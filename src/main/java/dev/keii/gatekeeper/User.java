package dev.keii.gatekeeper;

import javax.annotation.Nullable;

public class User {
    private final int id;
    private final String uuid;

    private final int recommendedBy;

    public User(int id, String uuid, int recommendedBy) {
        this.id = id;
        this.uuid = uuid;
        this.recommendedBy = recommendedBy;
    }

    public int getRecommendedBy() {
        return recommendedBy;
    }

    public String getUuid() {
        return uuid;
    }

    public int getId() {
        return id;
    }
}
