package com.cytril.duel.manager;

import com.cytril.duel.CytrilDuel;
import com.cytril.duel.model.Party;
import com.cytril.duel.model.PartyInvite;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class PartyManager {

    private final CytrilDuel plugin;
    // leaderUUID -> Party
    private final Map<UUID, Party> parties = new HashMap<>();
    // memberUUID -> leaderUUID
    private final Map<UUID, UUID> memberIndex = new HashMap<>();
    // targetUUID -> undangan yang masuk (bisa lebih dari satu, ambil terbaru)
    private final Map<UUID, PartyInvite> pendingInvites = new HashMap<>();

    public PartyManager(CytrilDuel plugin) {
        this.plugin = plugin;
    }

    public boolean isInParty(UUID uuid) {
        return memberIndex.containsKey(uuid);
    }

    public Party getParty(UUID member) {
        UUID leader = memberIndex.get(member);
        if (leader == null) return null;
        return parties.get(leader);
    }

    public boolean isLeader(UUID uuid) {
        Party p = getParty(uuid);
        return p != null && p.isLeader(uuid);
    }

    /** Pastikan pemain punya party (buat baru jika belum punya, dipakai saat solo duel). */
    public Party getOrCreateSolo(UUID uuid) {
        Party p = getParty(uuid);
        if (p != null) return p;
        Party solo = new Party(uuid);
        parties.put(uuid, solo);
        memberIndex.put(uuid, uuid);
        return solo;
    }

    public void invite(Player leaderPlayer, Player target) {
        UUID leaderUuid = leaderPlayer.getUniqueId();

        // jika belum punya party, jadikan dia leader dari party baru
        Party party = getParty(leaderUuid);
        if (party == null) {
            party = new Party(leaderUuid);
            parties.put(leaderUuid, party);
            memberIndex.put(leaderUuid, leaderUuid);
        } else if (!party.isLeader(leaderUuid)) {
            leaderPlayer.sendMessage(plugin.msg("&cHanya ketua party yang bisa mengundang."));
            return;
        }

        if (isInParty(target.getUniqueId())) {
            leaderPlayer.sendMessage(plugin.msg("&c" + target.getName() + " sudah berada di dalam party lain."));
            return;
        }

        pendingInvites.put(target.getUniqueId(), new PartyInvite(leaderUuid, target.getUniqueId()));

        leaderPlayer.sendMessage(plugin.msg("&aUndangan party dikirim ke &f" + target.getName()));

        Component msg = Component.text(leaderPlayer.getName() + " mengundangmu ke party.", NamedTextColor.GOLD)
                .appendNewline()
                .append(Component.text("[TERIMA]", NamedTextColor.GREEN)
                        .clickEvent(ClickEvent.runCommand("/party accept " + leaderPlayer.getName())))
                .append(Component.text("   "))
                .append(Component.text("[TOLAK]", NamedTextColor.RED)
                        .clickEvent(ClickEvent.runCommand("/party deny " + leaderPlayer.getName())));

        target.sendMessage(msg);
    }

    public void accept(Player target, String leaderName) {
        PartyInvite invite = pendingInvites.get(target.getUniqueId());
        Player leaderPlayer = Bukkit.getPlayerExact(leaderName);
        if (invite == null || leaderPlayer == null || !invite.getLeader().equals(leaderPlayer.getUniqueId())) {
            target.sendMessage(plugin.msg("&cTidak ada undangan party dari pemain tersebut (mungkin sudah kedaluwarsa)."));
            return;
        }
        pendingInvites.remove(target.getUniqueId());

        if (isInParty(target.getUniqueId())) {
            target.sendMessage(plugin.msg("&cKamu sudah berada di dalam party."));
            return;
        }

        Party party = parties.get(invite.getLeader());
        if (party == null) {
            target.sendMessage(plugin.msg("&cParty tersebut sudah tidak ada."));
            return;
        }

        party.getMembers().add(target.getUniqueId());
        memberIndex.put(target.getUniqueId(), party.getLeader());

        for (UUID m : party.getMembers()) {
            Player p = Bukkit.getPlayer(m);
            if (p != null) p.sendMessage(plugin.msg("&a" + target.getName() + " bergabung ke party!"));
        }
    }

    public void deny(Player target, String leaderName) {
        PartyInvite invite = pendingInvites.remove(target.getUniqueId());
        Player leaderPlayer = Bukkit.getPlayerExact(leaderName);
        if (leaderPlayer != null) {
            leaderPlayer.sendMessage(plugin.msg("&c" + target.getName() + " menolak undangan party."));
        }
        target.sendMessage(plugin.msg("&7Undangan party ditolak."));
    }

    /** Pemain keluar dari party. Jika dia leader, seluruh party dibubarkan. */
    public void leave(Player player) {
        Party party = getParty(player.getUniqueId());
        if (party == null) {
            player.sendMessage(plugin.msg("&cKamu tidak berada di dalam party."));
            return;
        }

        if (party.isLeader(player.getUniqueId())) {
            disband(party);
        } else {
            party.getMembers().remove(player.getUniqueId());
            memberIndex.remove(player.getUniqueId());
            player.sendMessage(plugin.msg("&7Kamu keluar dari party."));
            notify(party, player.getName() + " keluar dari party.");
        }
    }

    public void disband(Party party) {
        notify(party, "Party dibubarkan karena ketua keluar/logout.");
        for (UUID m : party.getMembers()) {
            memberIndex.remove(m);
        }
        parties.remove(party.getLeader());
    }

    /** Dipanggil saat pemain logout: sesuai requirement, jika leader keluar party dihapus & member auto keluar. */
    public void handleQuit(UUID uuid) {
        Party party = getParty(uuid);
        if (party == null) return;
        if (party.isLeader(uuid)) {
            disband(party);
        } else {
            party.getMembers().remove(uuid);
            memberIndex.remove(uuid);
            notify(party, Bukkit.getOfflinePlayer(uuid).getName() + " keluar dari party (logout).");
        }
    }

    private void notify(Party party, String text) {
        for (UUID m : party.getMembers()) {
            Player p = Bukkit.getPlayer(m);
            if (p != null) p.sendMessage(plugin.msg("&7" + text));
        }
    }

    public List<Player> getOnlineMembers(Party party) {
        return party.getMembers().stream()
                .map(Bukkit::getPlayer)
                .filter(p -> p != null && p.isOnline())
                .collect(Collectors.toList());
    }
}
