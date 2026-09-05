package com.cytril.duel.model;

import org.bukkit.Location;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/** Merepresentasikan 1 sesi duel yang sedang berlangsung di sebuah arena. */
public class ActiveDuel {

    private final Arena arena;
    private final String kitName;
    private final Set<UUID> teamA = new HashSet<>();
    private final Set<UUID> teamB = new HashSet<>();
    private final Set<UUID> aliveA = new HashSet<>();
    private final Set<UUID> aliveB = new HashSet<>();
    private final Set<UUID> spectators = new HashSet<>();
    private final boolean global;
    private final Location center;

    public ActiveDuel(Arena arena, String kitName, boolean global) {
        this.arena = arena;
        this.kitName = kitName;
        this.global = global;
        this.center = arena.getCenter();
    }

    public Arena getArena() { return arena; }
    public String getKitName() { return kitName; }
    public boolean isGlobal() { return global; }
    public Location getCenter() { return center; }

    public Set<UUID> getTeamA() { return teamA; }
    public Set<UUID> getTeamB() { return teamB; }
    public Set<UUID> getAliveA() { return aliveA; }
    public Set<UUID> getAliveB() { return aliveB; }
    public Set<UUID> getSpectators() { return spectators; }

    public boolean containsPlayer(UUID uuid) {
        return teamA.contains(uuid) || teamB.contains(uuid) || spectators.contains(uuid);
    }

    public int sideOf(UUID uuid) {
        if (teamA.contains(uuid)) return 1;
        if (teamB.contains(uuid)) return 2;
        return 0;
    }
}
