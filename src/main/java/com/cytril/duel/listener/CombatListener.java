package com.cytril.duel.listener;

import com.cytril.duel.CytrilDuel;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;

/** Mencegah PvP/kelaparan di lobby, PvP hanya diizinkan saat kedua pemain berstatus dueling. */
public class CombatListener implements Listener {

    private final CytrilDuel plugin;

    public CombatListener(CytrilDuel plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;
        Player attacker = null;
        if (event.getDamager() instanceof Player p) attacker = p;
        else if (event.getDamager() instanceof org.bukkit.entity.Projectile proj
                && proj.getShooter() instanceof Player p) attacker = p;

        if (attacker == null) return;

        boolean victimDueling = plugin.getDuelManager().isDueling(victim.getUniqueId());
        boolean attackerDueling = plugin.getDuelManager().isDueling(attacker.getUniqueId());

        if (!victimDueling || !attackerDueling) {
            event.setCancelled(true);
            return;
        }

        // pastikan keduanya berada di sesi duel yang sama (mencegah damage lintas arena)
        if (plugin.getDuelManager().getActiveDuel(victim.getUniqueId())
                != plugin.getDuelManager().getActiveDuel(attacker.getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onFoodChange(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!plugin.getDuelManager().isDueling(player.getUniqueId())) {
            event.setCancelled(true);
        }
    }
}
