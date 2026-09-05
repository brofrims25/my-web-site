package com.cytril.duel.placeholder;

import com.cytril.duel.CytrilDuel;
import com.cytril.duel.manager.LeaderboardManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Placeholder yang tersedia:
 *
 *  Statistik pemain:
 *   %cytril_duel_win%   -> jumlah menang pemain yang dipakaikan placeholder
 *   %cytril_duel_deat%  -> jumlah kalah
 *   %cytril_duel_kill%  -> jumlah kill
 *
 *  Leaderboard (tidak bergantung pemain yang melihat, bisa dipasang di plugin lain
 *  seperti HolographicDisplays / DeluxeMenus / TAB / Leaderboardz):
 *   %cytril_duel_top_win_name_<posisi>%    -> nama pemain peringkat ke-<posisi> menang
 *   %cytril_duel_top_win_value_<posisi>%   -> jumlah menang pemain di peringkat itu
 *   %cytril_duel_top_deat_name_<posisi>%   -> nama pemain peringkat ke-<posisi> kalah
 *   %cytril_duel_top_deat_value_<posisi>%  -> jumlah kalah di peringkat itu
 *   %cytril_duel_top_kill_name_<posisi>%   -> nama pemain peringkat ke-<posisi> kill
 *   %cytril_duel_top_kill_value_<posisi>%  -> jumlah kill di peringkat itu
 *
 *  Contoh pemakaian di hologram top 3 kill:
 *   Line 1: %cytril_duel_top_kill_name_1% - %cytril_duel_top_kill_value_1%
 *   Line 2: %cytril_duel_top_kill_name_2% - %cytril_duel_top_kill_value_2%
 *   Line 3: %cytril_duel_top_kill_name_3% - %cytril_duel_top_kill_value_3%
 */
public class CytrilPlaceholderExpansion extends PlaceholderExpansion {

    private static final Pattern TOP_PATTERN =
            Pattern.compile("top_(win|deat|kill)_(name|value)_(\\d+)");

    private final CytrilDuel plugin;

    public CytrilPlaceholderExpansion(CytrilDuel plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "cytril_duel";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Cytril";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.1.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        String lower = params.toLowerCase();

        // --- leaderboard placeholders (tidak butuh player, bisa null) ---
        Matcher matcher = TOP_PATTERN.matcher(lower);
        if (matcher.matches()) {
            LeaderboardManager.Type type = switch (matcher.group(1)) {
                case "win" -> LeaderboardManager.Type.WIN;
                case "deat" -> LeaderboardManager.Type.LOSS;
                default -> LeaderboardManager.Type.KILL;
            };
            String field = matcher.group(2);
            int position = Integer.parseInt(matcher.group(3));

            LeaderboardManager.Entry entry = plugin.getLeaderboardManager().getEntry(type, position);
            if (entry == null) {
                return field.equals("value") ? "0" : "-";
            }
            return field.equals("value") ? String.valueOf(entry.value()) : entry.name();
        }

        // --- statistik pemain individu ---
        if (player == null) return "";
        return switch (lower) {
            case "win" -> String.valueOf(plugin.getStatsManager().getWins(player.getUniqueId()));
            case "deat", "death", "losses" -> String.valueOf(plugin.getStatsManager().getLosses(player.getUniqueId()));
            case "kill" -> String.valueOf(plugin.getStatsManager().getKills(player.getUniqueId()));
            default -> null;
        };
    }
}
