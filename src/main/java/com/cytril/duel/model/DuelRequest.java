package com.cytril.duel.model;

import java.util.UUID;

/** Permintaan duel dari 1 pemain (atau ketua party) ke pemain lain. */
public class DuelRequest {

    private final UUID sender;
    private final UUID target;
    private final String arenaName;
    private final String kitName;
    private final long timestamp;

    public DuelRequest(UUID sender, UUID target, String arenaName, String kitName) {
        this.sender = sender;
        this.target = target;
        this.arenaName = arenaName;
        this.kitName = kitName;
        this.timestamp = System.currentTimeMillis();
    }

    public UUID getSender() { return sender; }
    public UUID getTarget() { return target; }
    public String getArenaName() { return arenaName; }
    public String getKitName() { return kitName; }
    public long getTimestamp() { return timestamp; }
}
