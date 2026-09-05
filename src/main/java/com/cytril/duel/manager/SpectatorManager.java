package com.cytril.duel.manager;

import com.cytril.duel.CytrilDuel;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Mengatur pemain yang mati saat duel menjadi spectator dengan batas jarak dari titik tengah arena. */
public class SpectatorManager {

    private final CytrilDuel plugin;
    private final Map<UUID, Location> centerOf = new HashMap<>();

    public SpectatorManager(CytrilDuel plugin) {
        this.plugin = plugin;
    }

    public void makeSpectator(Player player, Location center) {
        player.setGameMode(GameMode.SPECTATOR);
        centerOf.put(player.getUniqueId(), center);
    }

    public boolean isRestrictedSpectator(UUID uuid) {
        return centerOf.containsKey(uuid);
    }

    public void clear(UUID uuid) {
        centerOf.remove(uuid);
    }

    /** Dipanggil dari PlayerMoveEvent. Mengembalikan pemain jika melewati batas radius. */
    public void checkBounds(Player player) {
        Location center = centerOf.get(player.getUniqueId());
        if (center == null) return;
        int radius = plugin.getConfig().getInt("spectator-radius", 100);
        if (player.getLocation().distanceSquared(center) > (double) radius * radius) {
            Location clamped = clampToRadius(player.getLocation(), center, radius);
            player.teleport(clamped);
            player.sendActionBar(plugin.msg("&cKamu sudah mencapai batas jarak spectator!"));
        }
    }

    private Location clampToRadius(Location loc, Location center, int radius) {
        double dx = loc.getX() - center.getX();
        double dz = loc.getZ() - center.getZ();
        double dist = Math.sqrt(dx * dx + dz * dz);
        if (dist <= radius) return loc;
        double scale = (radius - 1) / dist;
        Location result = loc.clone();
        result.setX(center.getX() + dx * scale);
        result.setZ(center.getZ() + dz * scale);
        return result;
    }
}
