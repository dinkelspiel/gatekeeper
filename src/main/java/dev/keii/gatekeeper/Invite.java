package dev.keii.gatekeeper;

public class Invite {
    private final int userId;
    private final String inviteUUID;
    private final boolean used;

    public Invite(int userId, String inviteUUID, boolean used) {
        this.userId = userId;
        this.inviteUUID = inviteUUID;
        this.used = used;
    }

    public int getUserId() {
        return userId;
    }

    public String getInviteUUID() {
        return inviteUUID;
    }

    public boolean isUsed() {
        return used;
    }
}
