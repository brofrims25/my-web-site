package com.cytril.duel.model;

import java.util.UUID;

public class PartyInvite {
    private final UUID leader;
    private final UUID target;
    private final long timestamp;

    public PartyInvite(UUID leader, UUID target) {
        this.leader = leader;
        this.target = target;
        this.timestamp = System.currentTimeMillis();
    }

    public UUID getLeader() { return leader; }
    public UUID getTarget() { return target; }
    public long getTimestamp() { return timestamp; }
}
