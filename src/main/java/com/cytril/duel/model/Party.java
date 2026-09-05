package com.cytril.duel.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Party {

    private UUID leader;
    private final List<UUID> members = new ArrayList<>();

    public Party(UUID leader) {
        this.leader = leader;
        this.members.add(leader);
    }

    public UUID getLeader() { return leader; }
    public void setLeader(UUID leader) { this.leader = leader; }

    public List<UUID> getMembers() { return members; }

    public boolean isLeader(UUID uuid) { return leader.equals(uuid); }

    public int size() { return members.size(); }
}
