package com.cytril.duel.command;

import com.cytril.duel.CytrilDuel;
import com.cytril.duel.gui.MainMenuGui;
import com.cytril.duel.gui.SettingsGui;
import com.cytril.duel.model.Arena;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class DuelCommand implements CommandExecutor, TabCompleter {

    private final CytrilDuel plugin;

    public DuelCommand(CytrilDuel plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Perintah ini hanya bisa dijalankan oleh pemain.");
            return true;
        }

        if (args.length == 0) {
            if (plugin.getDuelManager().isDueling(player.getUniqueId())) {
                player.sendMessage(plugin.msg("&cKamu sedang berada dalam duel."));
                return true;
            }
            player.openInventory(new MainMenuGui(plugin, player).getInventory());
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "accept" -> {
                if (args.length < 2) { player.sendMessage(plugin.msg("&cGunakan: /duel accept <pemain>")); return true; }
                plugin.getDuelManager().accept(player, args[1]);
            }
            case "deny" -> {
                if (args.length < 2) { player.sendMessage(plugin.msg("&cGunakan: /duel deny <pemain>")); return true; }
                plugin.getDuelManager().deny(player, args[1]);
            }
            case "cancel" -> plugin.getDuelManager().cancel(player);
            case "stats" -> {
                Player target = args.length >= 2 ? Bukkit.getPlayerExact(args[1]) : player;
                if (target == null) { player.sendMessage(plugin.msg("&cPemain tidak ditemukan.")); return true; }
                player.sendMessage(plugin.msg("&6Statistik " + target.getName() + ":"));
                player.sendMessage(plugin.msg("&7Menang: &a" + plugin.getStatsManager().getWins(target.getUniqueId())));
                player.sendMessage(plugin.msg("&7Kalah: &c" + plugin.getStatsManager().getLosses(target.getUniqueId())));
                player.sendMessage(plugin.msg("&7Kill: &e" + plugin.getStatsManager().getKills(target.getUniqueId())));
            }
            case "setlobby" -> {
                if (!player.hasPermission("cytrilduel.admin")) { noPerm(player); return true; }
                Location loc = player.getLocation();
                plugin.getConfig().set("lobby.world", loc.getWorld().getName());
                plugin.getConfig().set("lobby.x", loc.getX());
                plugin.getConfig().set("lobby.y", loc.getY());
                plugin.getConfig().set("lobby.z", loc.getZ());
                plugin.getConfig().set("lobby.yaw", (double) loc.getYaw());
                plugin.getConfig().set("lobby.pitch", (double) loc.getPitch());
                plugin.saveConfig();
                player.sendMessage(plugin.msg("&aLobby PvP berhasil diatur ke posisimu sekarang."));
            }
            case "setarena" -> {
                if (!player.hasPermission("cytrilduel.admin")) { noPerm(player); return true; }
                // /duel setarena <nama> <lebar> <kit1,kit2,...> <duel|global>
                if (args.length < 5) {
                    player.sendMessage(plugin.msg("&cGunakan: /duel setarena <nama> <lebar> <kit> <duel|global>"));
                    return true;
                }
                String name = args[1];
                int width;
                try {
                    width = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    player.sendMessage(plugin.msg("&cLebar arena harus berupa angka."));
                    return true;
                }
                String kits = args[3];
                Arena.Mode mode;
                try {
                    mode = Arena.Mode.valueOf(args[4].toUpperCase());
                } catch (IllegalArgumentException e) {
                    player.sendMessage(plugin.msg("&cMode harus 'duel' atau 'global'."));
                    return true;
                }
                plugin.getArenaManager().createArena(name, width, kits, mode, player.getLocation());
                player.sendMessage(plugin.msg("&aArena '" + name + "' berhasil dibuat! (mode: " + mode + ", kit: " + kits + ")"));
            }
            case "setkit" -> {
                if (!player.hasPermission("cytrilduel.admin")) { noPerm(player); return true; }
                if (args.length < 2) { player.sendMessage(plugin.msg("&cGunakan: /duel setkit <nama>")); return true; }
                plugin.getKitManager().setKit(args[1], player);
                player.sendMessage(plugin.msg("&aKit '" + args[1] + "' disimpan dari inventorymu saat ini."));
            }
            case "spectatorradius" -> {
                if (!player.hasPermission("cytrilduel.admin")) { noPerm(player); return true; }
                if (args.length < 2) { player.sendMessage(plugin.msg("&cGunakan: /duel spectatorradius <angka>")); return true; }
                try {
                    int radius = Integer.parseInt(args[1]);
                    plugin.getConfig().set("spectator-radius", radius);
                    plugin.saveConfig();
                    player.sendMessage(plugin.msg("&aBatas jarak spectator diatur ke " + radius + " blok."));
                } catch (NumberFormatException e) {
                    player.sendMessage(plugin.msg("&cMasukkan angka yang valid."));
                }
            }
            case "top" -> {
                String type = args.length >= 2 ? args[1].toLowerCase() : "win";
                int amount = 10;
                if (args.length >= 3) {
                    try { amount = Integer.parseInt(args[2]); } catch (NumberFormatException ignored) {}
                }
                com.cytril.duel.manager.LeaderboardManager.Type lbType = switch (type) {
                    case "kill" -> com.cytril.duel.manager.LeaderboardManager.Type.KILL;
                    case "deat", "death", "loss", "losses" -> com.cytril.duel.manager.LeaderboardManager.Type.LOSS;
                    default -> com.cytril.duel.manager.LeaderboardManager.Type.WIN;
                };
                player.sendMessage(plugin.msg("&6=== Leaderboard " + type.toUpperCase() + " ==="));
                var top = plugin.getLeaderboardManager().getTop(lbType);
                if (top.isEmpty()) {
                    player.sendMessage(plugin.msg("&7Belum ada data."));
                } else {
                    int limit = Math.min(amount, top.size());
                    for (int i = 0; i < limit; i++) {
                        var e = top.get(i);
                        player.sendMessage(plugin.msg("&e#" + (i + 1) + " &f" + e.name() + " &7- &a" + e.value()));
                    }
                }
            }
            case "reload" -> {
                if (!player.hasPermission("cytrilduel.admin")) { noPerm(player); return true; }
                plugin.reloadConfig();
                player.sendMessage(plugin.msg("&aConfig CytrilDuel berhasil di-reload."));
            }
            case "settings" -> {
                if (!player.hasPermission("cytrilduel.admin")) { noPerm(player); return true; }
                new SettingsGui(plugin).open(player);
            }
            case "leaderboardreload" -> {
                if (!player.hasPermission("cytrilduel.admin")) { noPerm(player); return true; }
                plugin.getLeaderboardManager().refresh();
                player.sendMessage(plugin.msg("&aCache leaderboard berhasil dimuat ulang."));
            }
            case "help" -> sendHelp(player);
            default -> sendHelp(player);
        }
        return true;
    }

    private void noPerm(Player player) {
        player.sendMessage(plugin.msg("&cKamu tidak memiliki izin untuk perintah ini."));
    }

    private void sendHelp(Player player) {
        player.sendMessage(plugin.msg("&6=== CytrilDuel Help ==="));
        player.sendMessage(plugin.msg("&f/duel &7- buka menu pilih lawan"));
        player.sendMessage(plugin.msg("&f/duel accept <pemain> &7- terima ajakan duel"));
        player.sendMessage(plugin.msg("&f/duel deny <pemain> &7- tolak ajakan duel"));
        player.sendMessage(plugin.msg("&f/duel cancel &7- batalkan ajakan duel yang kamu kirim"));
        player.sendMessage(plugin.msg("&f/duel stats [pemain] &7- lihat statistik"));
        player.sendMessage(plugin.msg("&f/duel top <win|kill|deat> [jumlah] &7- lihat leaderboard"));
        player.sendMessage(plugin.msg("&f/duel reload &7- reload config.yml"));
        if (player.hasPermission("cytrilduel.admin")) player.sendMessage(plugin.msg("&e/duel settings &7- buka pengaturan PvP dalam game"));
        if (player.hasPermission("cytrilduel.admin")) {
            player.sendMessage(plugin.msg("&e/duel setlobby &7- set lobby PvP"));
            player.sendMessage(plugin.msg("&e/duel setarena <nama> <lebar> <kit> <duel|global> &7- buat arena"));
            player.sendMessage(plugin.msg("&e/duel setkit <nama> &7- simpan kit dari inventory"));
            player.sendMessage(plugin.msg("&e/duel spectatorradius <angka> &7- atur batas jarak spectator"));
            player.sendMessage(plugin.msg("&e/duel leaderboardreload &7- paksa muat ulang cache leaderboard"));
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> out = new ArrayList<>();
        if (args.length == 1) {
            out.addAll(List.of("accept", "deny", "cancel", "stats", "top", "reload", "settings", "help"));
            if (sender.hasPermission("cytrilduel.admin")) {
                out.addAll(List.of("setlobby", "setarena", "setkit", "spectatorradius", "leaderboardreload"));
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("top")) {
            out.addAll(List.of("win", "kill", "deat"));
        } else if (args.length == 5 && args[0].equalsIgnoreCase("setarena")) {
            out.addAll(List.of("duel", "global"));
        }
        return out;
    }
}
