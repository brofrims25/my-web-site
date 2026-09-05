package com.cytril.duel.listener;

import com.cytril.duel.CytrilDuel;
import com.cytril.duel.model.ActiveDuel;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.entity.Firework;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.EnderPearl;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;

/** Semua aturan combat/PvP utama dikontrol dari config.yml. */
public class CombatListener implements Listener {
    private final CytrilDuel plugin;
    public CombatListener(CytrilDuel plugin) { this.plugin = plugin; }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;
        Player attacker = findPlayerDamager(event.getDamager());
        if (attacker == null) return;

        ActiveDuel vd = plugin.getDuelManager().getActiveDuel(victim.getUniqueId());
        ActiveDuel ad = plugin.getDuelManager().getActiveDuel(attacker.getUniqueId());
        if (vd == null || ad == null || vd != ad) {
            event.setCancelled(!plugin.setting("pvp.damage-others-outside-duel", false));
            return;
        }
        if (vd.isCountdown() || !plugin.setting("pvp.enabled", true)) { event.setCancelled(true); return; }

        if (attacker.getUniqueId().equals(victim.getUniqueId())) return;
        boolean projectile = event.getDamager() instanceof Projectile;
        if (projectile && !plugin.setting("pvp.allow-projectiles", true)) { event.setCancelled(true); return; }
        if (!projectile && !plugin.setting("pvp.allow-melee", true)) { event.setCancelled(true); return; }
        if (projectile && !plugin.setting("pvp.projectile-friendly-fire", false)
                && vd.sideOf(attacker.getUniqueId()) == vd.sideOf(victim.getUniqueId())) { event.setCancelled(true); return; }
        if (!projectile && !plugin.setting("pvp.friendly-fire", false)
                && vd.sideOf(attacker.getUniqueId()) == vd.sideOf(victim.getUniqueId())) { event.setCancelled(true); return; }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEnvironmentalDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        ActiveDuel duel = plugin.getDuelManager().getActiveDuel(player.getUniqueId());
        if (duel == null) {
            if (plugin.setting("lobby.disable-damage", true)) event.setCancelled(true);
            return;
        }
        if (duel.isCountdown()) { event.setCancelled(true); return; }
        switch (event.getCause()) {
            case FALL -> event.setCancelled(!plugin.setting("pvp.allow-fall-damage", true));
            case FIRE, FIRE_TICK, HOT_FLOOR -> event.setCancelled(!plugin.setting("pvp.allow-fire-damage", true));
            case DROWNING -> event.setCancelled(!plugin.setting("pvp.allow-drowning", true));
            case SUFFOCATION -> event.setCancelled(!plugin.setting("pvp.allow-suffocation", true));
            case LAVA -> event.setCancelled(!plugin.setting("pvp.allow-lava-damage", true));
            case VOID -> event.setCancelled(!plugin.setting("pvp.allow-void-damage", true));
            case CONTACT -> event.setCancelled(!plugin.setting("pvp.allow-contact-damage", true));
            case STARVATION -> event.setCancelled(!plugin.setting("pvp.allow-starvation", false));
            default -> { }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onFoodChange(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!plugin.getDuelManager().isDueling(player.getUniqueId())) {
            if (plugin.setting("lobby.disable-hunger", true)) event.setCancelled(true);
            return;
        }
        event.setCancelled(!plugin.setting("pvp.allow-starvation", false));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        if (plugin.getDuelManager().isDueling(event.getPlayer().getUniqueId()) && plugin.setting("duel.prevent-break", true)) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        if (plugin.getDuelManager().isDueling(event.getPlayer().getUniqueId()) && plugin.setting("duel.prevent-build", true)) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!plugin.getDuelManager().isDueling(player.getUniqueId())) return;
        var item = event.getItem();
        if (item != null) {
            String type = item.getType().name();
            if (type.equals("ENDER_PEARL") && !plugin.setting("pvp.allow-ender-pearl", true)) event.setCancelled(true);
            if ((type.equals("POTION") || type.equals("SPLASH_POTION") || type.equals("LINGERING_POTION"))
                    && !plugin.setting("pvp.allow-potions", true)) event.setCancelled(true);
            if (type.equals("FIREWORK_ROCKET") && !plugin.setting("pvp.allow-firework-damage", true)) event.setCancelled(true);
            if ((type.equals("END_CRYSTAL") && !plugin.setting("pvp.allow-crystals", false))
                    || (type.equals("TNT") && !plugin.setting("pvp.allow-tnt", false))
                    || (type.equals("RESPAWN_ANCHOR") && !plugin.setting("pvp.allow-respawn-anchor-explosion", false))) event.setCancelled(true);
        }
        if (plugin.getSpectatorManager().isRestrictedSpectator(player.getUniqueId()) && !plugin.setting("duel.allow-spectator-chat", true)) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        if (!plugin.getDuelManager().isDueling(player.getUniqueId())) return;
        if (event.getItem().getType().name().equals("CHORUS_FRUIT") && !plugin.setting("pvp.allow-chorus-fruit", false)) event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent event) {
        if (plugin.getDuelManager().isDueling(event.getPlayer().getUniqueId()) && plugin.setting("combat.disable-teleport-commands", true)
                && event.getCause() == PlayerTeleportEvent.TeleportCause.COMMAND) event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onFlight(PlayerToggleFlightEvent event) {
        if (plugin.getDuelManager().isDueling(event.getPlayer().getUniqueId()) && plugin.setting("combat.disable-flight", true)) event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (!plugin.getDuelManager().isDueling(player.getUniqueId())) return;
        if (!plugin.setting("combat.prevent-command", false)) return;
        String command = event.getMessage().substring(1).split(" ", 2)[0].toLowerCase();
        for (String allowed : plugin.getConfig().getStringList("combat.commands-allowed")) if (allowed.equalsIgnoreCase(command)) return;
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onRegainHealth(EntityRegainHealthEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (plugin.getDuelManager().isDueling(player.getUniqueId())) {
            if (!plugin.setting("pvp.natural-regeneration", true) && event.getRegainReason() == EntityRegainHealthEvent.RegainReason.SATIATED) event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (!(event.getEntity().getShooter() instanceof Player player)) return;
        if (!plugin.getDuelManager().isDueling(player.getUniqueId())) return;
        String type = event.getEntity().getType().name();
        if (type.equals("ENDER_PEARL") && !plugin.setting("pvp.allow-ender-pearl", true)) event.setCancelled(true);
        if (type.equals("SNOWBALL") && !plugin.setting("pvp.allow-snowballs", true)) event.setCancelled(true);
        if (type.equals("EGG") && !plugin.setting("pvp.allow-eggs", true)) event.setCancelled(true);
        if (type.equals("TRIDENT") && !plugin.setting("pvp.allow-tridents", true)) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onExplosion(EntityExplodeEvent event) {
        if (!(event.getEntity() instanceof Player) && !(event.getEntity() instanceof Firework)
                && !(event.getEntity() instanceof TNTPrimed)) return;
        boolean allowed = event.getEntity() instanceof Firework
                ? plugin.setting("pvp.allow-firework-damage", true)
                : plugin.setting("pvp.allow-tnt", false);
        if (!allowed) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockExplosion(BlockExplodeEvent event) {
        // Bed/respawn-anchor/TNT block explosions are safest to disable by default in duel worlds.
        if (!plugin.setting("pvp.allow-bed-explosion", false) || !plugin.setting("pvp.allow-respawn-anchor-explosion", false)) {
            for (Player player : event.getBlock().getWorld().getPlayers()) {
                if (plugin.getDuelManager().isDueling(player.getUniqueId())) { event.setCancelled(true); return; }
            }
        }
    }

    private Player findPlayerDamager(Entity damager) {
        if (damager instanceof Player p) return p;
        if (damager instanceof Projectile projectile && projectile.getShooter() instanceof Player p) return p;
        return null;
    }
}
