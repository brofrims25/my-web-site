package com.cytril.duel.manager;

import com.cytril.duel.CytrilDuel;
import com.cytril.duel.model.ActiveDuel;
import com.cytril.duel.model.Arena;
import com.cytril.duel.model.DuelRequest;
import com.cytril.duel.model.Kit;
import com.cytril.duel.model.Party;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class DuelManager {

    private final CytrilDuel plugin;

    // requests aktif, key = target uuid (setiap target hanya boleh punya 1 request masuk aktif)
    private final Map<UUID, DuelRequest> incoming = new HashMap<>();
    // request yang dikirim sender, dipakai untuk /duel cancel
    private final Map<UUID, DuelRequest> outgoing = new HashMap<>();

    // pemain yang sedang berada di dalam duel -> ActiveDuel
    private final Map<UUID, ActiveDuel> activeByPlayer = new HashMap<>();
    private final Set<ActiveDuel> activeDuels = new HashSet<>();

    public DuelManager(CytrilDuel plugin) {
        this.plugin = plugin;
    }

    public boolean isDueling(UUID uuid) {
        return activeByPlayer.containsKey(uuid);
    }

    public ActiveDuel getActiveDuel(UUID uuid) {
        return activeByPlayer.get(uuid);
    }

    // ---------------------------------------------------------------
    // REQUEST DUEL (1v1 / party vs party)
    // ---------------------------------------------------------------

    public void sendRequest(Player sender, Player target, Arena arena, String kitName) {
        if (sender.equals(target)) {
            sender.sendMessage(plugin.msg("&cKamu tidak bisa menantang dirimu sendiri."));
            return;
        }
        if (isDueling(sender.getUniqueId()) || isDueling(target.getUniqueId())) {
            sender.sendMessage(plugin.msg("&cKamu atau target sedang berada dalam duel lain."));
            return;
        }
        if (outgoing.containsKey(sender.getUniqueId())) {
            sender.sendMessage(plugin.msg("&cKamu masih memiliki ajakan duel yang belum direspon. Gunakan /duel cancel."));
            return;
        }

        DuelRequest req = new DuelRequest(sender.getUniqueId(), target.getUniqueId(), arena.getName(), kitName);
        incoming.put(target.getUniqueId(), req);
        outgoing.put(sender.getUniqueId(), req);

        sender.sendMessage(plugin.msg("&aAjakan duel dikirim ke &f" + target.getName() +
                " &7(arena: " + arena.getName() + ", kit: " + kitName + ")"));

        Component msg = Component.text(sender.getName() + " menantangmu duel! (arena: " + arena.getName() + ")", NamedTextColor.GOLD)
                .appendNewline()
                .append(Component.text("[TERIMA]", NamedTextColor.GREEN)
                        .clickEvent(ClickEvent.runCommand("/duel accept " + sender.getName())))
                .append(Component.text("   "))
                .append(Component.text("[TOLAK]", NamedTextColor.RED)
                        .clickEvent(ClickEvent.runCommand("/duel deny " + sender.getName())));
        target.sendMessage(msg);

        scheduleTimeout(req);
    }

    private void scheduleTimeout(DuelRequest req) {
        int timeout = plugin.getConfig().getInt("request-timeout", 60);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (incoming.get(req.getTarget()) == req) {
                incoming.remove(req.getTarget());
                outgoing.remove(req.getSender());
                Player s = Bukkit.getPlayer(req.getSender());
                Player t = Bukkit.getPlayer(req.getTarget());
                if (s != null) s.sendMessage(plugin.msg("&7Ajakan duelmu ke " +
                        (t != null ? t.getName() : "pemain") + " kedaluwarsa."));
                if (t != null) t.sendMessage(plugin.msg("&7Ajakan duel darimu sudah kedaluwarsa."));
            }
        }, timeout * 20L);
    }

    public void accept(Player target, String senderName) {
        Player sender = Bukkit.getPlayerExact(senderName);
        DuelRequest req = incoming.get(target.getUniqueId());
        if (req == null || sender == null || !req.getSender().equals(sender.getUniqueId())) {
            target.sendMessage(plugin.msg("&cTidak ada ajakan duel dari pemain tersebut (mungkin sudah kedaluwarsa)."));
            return;
        }
        incoming.remove(target.getUniqueId());
        outgoing.remove(sender.getUniqueId());

        Arena arena = plugin.getArenaManager().get(req.getArenaName());
        if (arena == null || arena.isInUse()) {
            sender.sendMessage(plugin.msg("&cArena sudah tidak tersedia lagi."));
            target.sendMessage(plugin.msg("&cArena sudah tidak tersedia lagi."));
            return;
        }

        Party partyA = plugin.getPartyManager().getOrCreateSolo(sender.getUniqueId());
        Party partyB = plugin.getPartyManager().getOrCreateSolo(target.getUniqueId());

        startDuel(arena, req.getKitName(), partyA, partyB);
    }

    public void deny(Player target, String senderName) {
        Player sender = Bukkit.getPlayerExact(senderName);
        DuelRequest req = incoming.remove(target.getUniqueId());
        if (req != null) outgoing.remove(req.getSender());
        if (sender != null) sender.sendMessage(plugin.msg("&c" + target.getName() + " menolak ajakan duelmu."));
        target.sendMessage(plugin.msg("&7Ajakan duel ditolak."));
    }

    public void cancel(Player sender) {
        DuelRequest req = outgoing.remove(sender.getUniqueId());
        if (req == null) {
            sender.sendMessage(plugin.msg("&cKamu tidak memiliki ajakan duel yang menunggu."));
            return;
        }
        incoming.remove(req.getTarget());
        Player target = Bukkit.getPlayer(req.getTarget());
        sender.sendMessage(plugin.msg("&7Ajakan duel dibatalkan."));
        if (target != null) target.sendMessage(plugin.msg("&7" + sender.getName() + " membatalkan ajakan duel."));
    }

    // ---------------------------------------------------------------
    // MULAI & AKHIRI DUEL
    // ---------------------------------------------------------------

    public void startDuel(Arena arena, String kitName, Party partyA, Party partyB) {
        arena.setInUse(true);
        ActiveDuel duel = new ActiveDuel(arena, kitName, false);
        Kit kit = plugin.getKitManager().get(kitName);

        Location spawnA = arena.getSpawnA();
        Location spawnB = arena.getSpawnB();

        for (UUID uuid : partyA.getMembers()) {
            duel.getTeamA().add(uuid);
            duel.getAliveA().add(uuid);
            activeByPlayer.put(uuid, duel);
            prepPlayer(uuid, spawnA, kit);
        }
        for (UUID uuid : partyB.getMembers()) {
            duel.getTeamB().add(uuid);
            duel.getAliveB().add(uuid);
            activeByPlayer.put(uuid, duel);
            prepPlayer(uuid, spawnB, kit);
        }

        activeDuels.add(duel);
        broadcastToDuel(duel, "&6Duel dimulai! &f" + arena.getName() + " &7| kit: " + kitName);
    }

    /** Bergabung ke arena mode GLOBAL (FFA, party diabaikan). */
    public void joinGlobal(Player player, Arena arena) {
        if (isDueling(player.getUniqueId())) {
            player.sendMessage(plugin.msg("&cKamu sudah berada dalam duel."));
            return;
        }
        ActiveDuel duel = findGlobalDuel(arena);
        if (duel == null) {
            duel = new ActiveDuel(arena, arena.getKitNames().isEmpty() ? null : arena.getKitNames().get(0), true);
            activeDuels.add(duel);
            arena.setInUse(true);
        }
        duel.getTeamA().add(player.getUniqueId());
        duel.getAliveA().add(player.getUniqueId());
        activeByPlayer.put(player.getUniqueId(), duel);
        Kit kit = plugin.getKitManager().get(duel.getKitName());
        prepPlayer(player.getUniqueId(), arena.getSpawnA(), kit);
        broadcastToDuel(duel, "&e" + player.getName() + " bergabung ke arena global " + arena.getName());
    }

    private ActiveDuel findGlobalDuel(Arena arena) {
        for (ActiveDuel d : activeDuels) {
            if (d.isGlobal() && d.getArena().equals(arena)) return d;
        }
        return null;
    }

    private void prepPlayer(UUID uuid, Location spawn, Kit kit) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return;
        player.teleport(spawn);
        player.setGameMode(GameMode.SURVIVAL);
        player.setHealth(player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue());
        player.setFoodLevel(20);
        if (kit != null) plugin.getKitManager().giveKit(kit, player);
    }

    /** Dipanggil oleh listener kematian pemain. */
    public void handleDeath(Player player, Player killer) {
        ActiveDuel duel = activeByPlayer.get(player.getUniqueId());
        if (duel == null) return;

        if (killer != null) {
            plugin.getStatsManager().addKill(killer.getUniqueId());
        }

        if (duel.isGlobal()) {
            duel.getAliveA().remove(player.getUniqueId());
            plugin.getSpectatorManager().makeSpectator(player, duel.getCenter());
            duel.getSpectators().add(player.getUniqueId());
            broadcastToDuel(duel, "&c" + player.getName() + " tersingkir dari arena global!");
            if (duel.getAliveA().size() <= 1) {
                endGlobal(duel);
            }
            return;
        }

        int side = duel.sideOf(player.getUniqueId());
        if (side == 1) duel.getAliveA().remove(player.getUniqueId());
        if (side == 2) duel.getAliveB().remove(player.getUniqueId());

        plugin.getSpectatorManager().makeSpectator(player, duel.getCenter());
        duel.getSpectators().add(player.getUniqueId());
        broadcastToDuel(duel, "&c" + player.getName() + " tumbang dan menjadi spectator!");

        if (duel.getAliveA().isEmpty() && !duel.getAliveB().isEmpty()) {
            endDuel(duel, duel.getTeamB(), duel.getTeamA());
        } else if (duel.getAliveB().isEmpty() && !duel.getAliveA().isEmpty()) {
            endDuel(duel, duel.getTeamA(), duel.getTeamB());
        }
    }

    private void endDuel(ActiveDuel duel, Set<UUID> winners, Set<UUID> losers) {
        for (UUID uuid : winners) plugin.getStatsManager().addWin(uuid);
        for (UUID uuid : losers) plugin.getStatsManager().addLoss(uuid);

        broadcastToDuel(duel, "&6Duel selesai! Pemenang: &a" +
                winners.stream().map(u -> Bukkit.getOfflinePlayer(u).getName()).reduce((a, b) -> a + ", " + b).orElse("-"));

        for (UUID uuid : duel.getTeamA()) cleanupPlayer(uuid);
        for (UUID uuid : duel.getTeamB()) cleanupPlayer(uuid);

        duel.getArena().setInUse(false);
        activeDuels.remove(duel);
    }

    private void endGlobal(ActiveDuel duel) {
        UUID winner = duel.getAliveA().stream().findFirst().orElse(null);
        if (winner != null) {
            plugin.getStatsManager().addWin(winner);
            broadcastToDuel(duel, "&6Arena global selesai! Pemenang: &a" + Bukkit.getOfflinePlayer(winner).getName());
        }
        for (UUID uuid : new HashSet<>(duel.getTeamA())) cleanupPlayer(uuid);
        duel.getArena().setInUse(false);
        activeDuels.remove(duel);
    }

    private void cleanupPlayer(UUID uuid) {
        activeByPlayer.remove(uuid);
        plugin.getSpectatorManager().clear(uuid);
        Player player = Bukkit.getPlayer(uuid);
        if (player != null) {
            plugin.teleportToLobby(player);
            player.setGameMode(GameMode.SURVIVAL);
            player.getInventory().clear();
            player.getInventory().setArmorContents(null);
            plugin.getHotbarManager().giveHotbar(player);
        }
    }

    private void broadcastToDuel(ActiveDuel duel, String msg) {
        for (UUID uuid : duel.getTeamA()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) p.sendMessage(plugin.msg(msg));
        }
        for (UUID uuid : duel.getTeamB()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) p.sendMessage(plugin.msg(msg));
        }
        for (UUID uuid : duel.getSpectators()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) p.sendMessage(plugin.msg(msg));
        }
    }

    /** Dipanggil saat pemain logout mendadak ketika sedang duel. */
    public void handleQuitDuringDuel(Player player) {
        ActiveDuel duel = activeByPlayer.get(player.getUniqueId());
        if (duel == null) return;
        handleDeath(player, null);
    }
}
