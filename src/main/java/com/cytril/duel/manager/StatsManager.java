package com.cytril.duel.manager;

import com.cytril.duel.CytrilDuel;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class StatsManager {

    private final CytrilDuel plugin;
    private final File file;
    private FileConfiguration cfg;

    public StatsManager(CytrilDuel plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "stats.yml");
        load();
    }

    public void load() {
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Gagal membuat stats.yml: " + e.getMessage());
            }
        }
        cfg = YamlConfiguration.loadConfiguration(file);
    }

    private void save() {
        try {
            cfg.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Gagal menyimpan stats.yml: " + e.getMessage());
        }
    }

    public int getWins(UUID uuid) { return cfg.getInt(uuid + ".wins", 0); }
    public int getLosses(UUID uuid) { return cfg.getInt(uuid + ".losses", 0); }
    public int getKills(UUID uuid) { return cfg.getInt(uuid + ".kills", 0); }

    /** Semua UUID pemain yang pernah tercatat statistiknya (dipakai untuk leaderboard). */
    public java.util.List<UUID> getAllPlayerUUIDs() {
        java.util.List<UUID> list = new java.util.ArrayList<>();
        for (String key : cfg.getKeys(false)) {
            try {
                list.add(UUID.fromString(key));
            } catch (IllegalArgumentException ignored) {
                // lewati key yang bukan UUID valid
            }
        }
        return list;
    }

    public void addWin(UUID uuid) {
        cfg.set(uuid + ".wins", getWins(uuid) + 1);
        save();
    }

    public void addLoss(UUID uuid) {
        cfg.set(uuid + ".losses", getLosses(uuid) + 1);
        save();
    }

    public void addKill(UUID uuid) {
        cfg.set(uuid + ".kills", getKills(uuid) + 1);
        save();
    }
}
