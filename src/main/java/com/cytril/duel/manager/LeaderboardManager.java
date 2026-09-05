package com.cytril.duel.manager;

import com.cytril.duel.CytrilDuel;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Menghitung & menyimpan cache top pemain berdasarkan menang/kalah/kill.
 * Data ini yang dipakai untuk placeholder leaderboard, sehingga bisa
 * "dicampur" / dipasang di plugin lain seperti HolographicDisplays,
 * DeluxeMenus, TAB, Leaderboardz, dsb — selama plugin tersebut mendukung
 * PlaceholderAPI.
 */
public class LeaderboardManager {

    public enum Type { WIN, LOSS, KILL }

    public record Entry(UUID uuid, String name, int value) {}

    private final CytrilDuel plugin;

    private List<Entry> topWins = Collections.emptyList();
    private List<Entry> topLosses = Collections.emptyList();
    private List<Entry> topKills = Collections.emptyList();

    private static final int MAX_CACHED = 100;

    public LeaderboardManager(CytrilDuel plugin) {
        this.plugin = plugin;
        refresh();
        int interval = Math.max(10, plugin.getConfig().getInt("leaderboard-refresh-seconds", 60));
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::refresh, 20L * interval, 20L * interval);
    }

    /** Hitung ulang cache leaderboard. Aman dipanggil dari thread async. */
    public synchronized void refresh() {
        List<UUID> all = plugin.getStatsManager().getAllPlayerUUIDs();

        List<Entry> wins = new ArrayList<>();
        List<Entry> losses = new ArrayList<>();
        List<Entry> kills = new ArrayList<>();

        for (UUID uuid : all) {
            String name = resolveName(uuid);
            wins.add(new Entry(uuid, name, plugin.getStatsManager().getWins(uuid)));
            losses.add(new Entry(uuid, name, plugin.getStatsManager().getLosses(uuid)));
            kills.add(new Entry(uuid, name, plugin.getStatsManager().getKills(uuid)));
        }

        wins.sort((a, b) -> b.value() - a.value());
        losses.sort((a, b) -> b.value() - a.value());
        kills.sort((a, b) -> b.value() - a.value());

        this.topWins = trim(wins);
        this.topLosses = trim(losses);
        this.topKills = trim(kills);
    }

    private List<Entry> trim(List<Entry> list) {
        return list.subList(0, Math.min(list.size(), MAX_CACHED));
    }

    private String resolveName(UUID uuid) {
        OfflinePlayer p = Bukkit.getOfflinePlayer(uuid);
        String name = p.getName();
        return name != null ? name : "Unknown";
    }

    public synchronized List<Entry> getTop(Type type) {
        return switch (type) {
            case WIN -> topWins;
            case LOSS -> topLosses;
            case KILL -> topKills;
        };
    }

    /** position dimulai dari 1 (peringkat 1, bukan index 0). Null jika tidak ada datanya. */
    public Entry getEntry(Type type, int position) {
        List<Entry> list = getTop(type);
        int index = position - 1;
        if (index < 0 || index >= list.size()) return null;
        return list.get(index);
    }
}
