package com.cytril.duel.command;

import com.cytril.duel.CytrilDuel;
import com.cytril.duel.model.Party;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PartyCommand implements CommandExecutor {

    private final CytrilDuel plugin;

    public PartyCommand(CytrilDuel plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Perintah ini hanya bisa dijalankan oleh pemain.");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(plugin.msg("&cGunakan: /party <invite|accept|deny|leave|list> [pemain]"));
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "invite" -> {
                if (args.length < 2) { player.sendMessage(plugin.msg("&cGunakan: /party invite <pemain>")); return true; }
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null) { player.sendMessage(plugin.msg("&cPemain tidak ditemukan.")); return true; }
                plugin.getPartyManager().invite(player, target);
            }
            case "accept" -> {
                if (args.length < 2) { player.sendMessage(plugin.msg("&cGunakan: /party accept <pemain>")); return true; }
                plugin.getPartyManager().accept(player, args[1]);
            }
            case "deny" -> {
                if (args.length < 2) { player.sendMessage(plugin.msg("&cGunakan: /party deny <pemain>")); return true; }
                plugin.getPartyManager().deny(player, args[1]);
            }
            case "leave" -> plugin.getPartyManager().leave(player);
            case "list" -> {
                Party party = plugin.getPartyManager().getParty(player.getUniqueId());
                if (party == null) {
                    player.sendMessage(plugin.msg("&7Kamu tidak berada di dalam party."));
                    return true;
                }
                player.sendMessage(plugin.msg("&6Anggota party (" + party.size() + "):"));
                party.getMembers().forEach(uuid -> {
                    String name = Bukkit.getOfflinePlayer(uuid).getName();
                    boolean isLeader = party.isLeader(uuid);
                    player.sendMessage(plugin.msg("&7- " + name + (isLeader ? " &6(Ketua)" : "")));
                });
            }
            default -> player.sendMessage(plugin.msg("&cSub perintah tidak dikenal."));
        }
        return true;
    }
}
