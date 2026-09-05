package com.cytril.duel;

import com.cytril.duel.command.DuelCommand;
import com.cytril.duel.command.PartyCommand;
import com.cytril.duel.listener.*;
import com.cytril.duel.manager.*;
import com.cytril.duel.placeholder.CytrilPlaceholderExpansion;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.Particle;
import org.bukkit.plugin.java.JavaPlugin;

public class CytrilDuel extends JavaPlugin {

    private static CytrilDuel instance;

    private PartyManager partyManager;
    private ArenaManager arenaManager;
    private KitManager kitManager;
    private StatsManager statsManager;
    private DuelManager duelManager;
    private SpectatorManager spectatorManager;
    private HotbarManager hotbarManager;
    private LeaderboardManager leaderboardManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        this.partyManager = new PartyManager(this);
        this.arenaManager = new ArenaManager(this);
        this.kitManager = new KitManager(this);
        this.statsManager = new StatsManager(this);
        this.spectatorManager = new SpectatorManager(this);
        this.duelManager = new DuelManager(this);
        this.hotbarManager = new HotbarManager();
        this.leaderboardManager = new LeaderboardManager(this);

        getServer().getPluginManager().registerEvents(new JoinListener(this), this);
        getServer().getPluginManager().registerEvents(new QuitListener(this), this);
        getServer().getPluginManager().registerEvents(new DeathListener(this), this);
        getServer().getPluginManager().registerEvents(new GuiClickListener(), this);
        getServer().getPluginManager().registerEvents(new HotbarInteractListener(this), this);
        getServer().getPluginManager().registerEvents(new MoveListener(this), this);
        getServer().getPluginManager().registerEvents(new CombatListener(this), this);

        getCommand("duel").setExecutor(new DuelCommand(this));
        getCommand("duel").setTabCompleter(new DuelCommand(this));
        getCommand("party").setExecutor(new PartyCommand(this));

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new CytrilPlaceholderExpansion(this).register();
            getLogger().info("PlaceholderAPI terdeteksi, placeholder %cytril_duel_*% berhasil didaftarkan.");
        }

        getLogger().info("CytrilDuel berhasil diaktifkan!");
    }

    @Override
    public void onDisable() {
        getLogger().info("CytrilDuel dimatikan.");
    }

    public static CytrilDuel getInstance() {
        return instance;
    }

    public PartyManager getPartyManager() { return partyManager; }
    public ArenaManager getArenaManager() { return arenaManager; }
    public KitManager getKitManager() { return kitManager; }
    public StatsManager getStatsManager() { return statsManager; }
    public DuelManager getDuelManager() { return duelManager; }
    public SpectatorManager getSpectatorManager() { return spectatorManager; }
    public HotbarManager getHotbarManager() { return hotbarManager; }
    public LeaderboardManager getLeaderboardManager() { return leaderboardManager; }

    public Component msg(String legacyText) {
        String prefix = getConfig().getString("messages.prefix", "&8[&6CytrilDuel&8] &r");
        return LegacyComponentSerializer.legacyAmpersand().deserialize(prefix + legacyText)
                .decoration(TextDecoration.ITALIC, false);
    }

    public Location getLobbyLocation() {
        String worldName = getConfig().getString("lobby.world", "");
        World world = Bukkit.getWorld(worldName);
        if (world == null) world = Bukkit.getWorlds().get(0);
        return new Location(world,
                getConfig().getDouble("lobby.x"),
                getConfig().getDouble("lobby.y"),
                getConfig().getDouble("lobby.z"),
                (float) getConfig().getDouble("lobby.yaw"),
                (float) getConfig().getDouble("lobby.pitch"));
    }

    public void teleportToLobby(Player player) {
        player.teleport(getLobbyLocation());
    }
    public boolean setting(String path, boolean def) {
        return getConfig().getBoolean(path, def);
    }

    public int settingInt(String path, int def) {
        return getConfig().getInt(path, def);
    }

    public double settingDouble(String path, double def) {
        return getConfig().getDouble(path, def);
    }

    public String settingString(String path, String def) {
        return getConfig().getString(path, def);
    }

    public void sendConfigured(Player player, String key, String fallback, java.util.Map<String, String> vars) {
        String text = getConfig().getString("messages." + key, fallback);
        if (vars != null) {
            for (var entry : vars.entrySet()) {
                text = text.replace("%" + entry.getKey() + "%", entry.getValue());
            }
        }
        player.sendMessage(msg(text));
    }

    public void playConfiguredSound(Player player, String key) {
        if (!setting("sounds.enabled", true)) return;
        String raw = getConfig().getString("sounds." + key);
        if (raw == null) return;
        try {
            Sound sound = Sound.valueOf(raw.toUpperCase());
            player.playSound(player.getLocation(), sound, (float) settingDouble("sounds.volume", 1.0), (float) settingDouble("sounds.pitch", 1.0));
        } catch (IllegalArgumentException ignored) {
            if (getConfig().getBoolean("general.debug", false)) {
                getLogger().warning("Sound tidak valid di config: sounds." + key + "=" + raw);
            }
        }
    }

    public void spawnConfiguredParticle(Location location, String path, Particle fallback, int count) {
        if (!setting("effects.enabled", true)) return;
        String raw = getConfig().getString(path);
        Particle particle = fallback;
        if (raw != null) {
            try { particle = Particle.valueOf(raw.toUpperCase()); } catch (IllegalArgumentException ignored) {}
        }
        location.getWorld().spawnParticle(particle, location, count, 0.5, 0.8, 0.5, 0.02);
    }

}
