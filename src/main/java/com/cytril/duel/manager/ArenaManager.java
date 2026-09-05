package com.cytril.duel.manager;

import com.cytril.duel.CytrilDuel;
import com.cytril.duel.model.Arena;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ArenaManager {

    private final CytrilDuel plugin;
    private final File file;
    private FileConfiguration cfg;
    private final Map<String, Arena> arenas = new LinkedHashMap<>();

    public ArenaManager(CytrilDuel plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "arenas.yml");
        load();
    }

    public void load() {
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Gagal membuat arenas.yml: " + e.getMessage());
            }
        }
        cfg = YamlConfiguration.loadConfiguration(file);
        arenas.clear();
        ConfigurationSection sec = cfg.getConfigurationSection("arenas");
        if (sec == null) return;
        for (String name : sec.getKeys(false)) {
            ConfigurationSection a = sec.getConfigurationSection(name);
            int width = a.getInt("width");
            List<String> kits = a.getStringList("kits");
            Arena.Mode mode = Arena.Mode.valueOf(a.getString("mode", "DUEL"));
            Location center = new Location(
                    Bukkit.getWorld(a.getString("world")),
                    a.getDouble("x"), a.getDouble("y"), a.getDouble("z"),
                    (float) a.getDouble("yaw"), (float) a.getDouble("pitch"));
            arenas.put(name.toLowerCase(), new Arena(name, width, kits, mode, center));
        }
    }

    public void save() {
        cfg.set("arenas", null);
        for (Arena arena : arenas.values()) {
            String path = "arenas." + arena.getName();
            Location c = arena.getCenter();
            cfg.set(path + ".width", arena.getWidth());
            cfg.set(path + ".kits", arena.getKitNames());
            cfg.set(path + ".mode", arena.getMode().name());
            cfg.set(path + ".world", c.getWorld().getName());
            cfg.set(path + ".x", c.getX());
            cfg.set(path + ".y", c.getY());
            cfg.set(path + ".z", c.getZ());
            cfg.set(path + ".yaw", c.getYaw());
            cfg.set(path + ".pitch", c.getPitch());
        }
        try {
            cfg.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Gagal menyimpan arenas.yml: " + e.getMessage());
        }
    }

    public Arena createArena(String name, int width, String kitCsv, Arena.Mode mode, Location center) {
        List<String> kits = new ArrayList<>();
        for (String k : kitCsv.split(",")) kits.add(k.trim());
        Arena arena = new Arena(name, width, kits, mode, center);
        arenas.put(name.toLowerCase(), arena);
        save();
        return arena;
    }

    public Arena get(String name) {
        return arenas.get(name.toLowerCase());
    }

    public Map<String, Arena> getAll() {
        return arenas;
    }

    public List<Arena> getByMode(Arena.Mode mode) {
        List<Arena> result = new ArrayList<>();
        for (Arena a : arenas.values()) if (a.getMode() == mode) result.add(a);
        return result;
    }

    public Arena findFreeArena(Arena.Mode mode, String kitName) {
        for (Arena a : arenas.values()) {
            if (a.getMode() == mode && !a.isInUse() && (kitName == null || a.allowsKit(kitName))) {
                return a;
            }
        }
        return null;
    }
}
