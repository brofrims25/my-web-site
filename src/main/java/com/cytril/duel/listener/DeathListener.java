package com.cytril.duel.listener;

import com.cytril.duel.CytrilDuel;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class DeathListener implements Listener {

    private final CytrilDuel plugin;

    public DeathListener(CytrilDuel plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        if (!plugin.getDuelManager().isDueling(event.getEntity().getUniqueId())) return;

        // jangan drop item/exp saat duel, dan langsung respawn di tempat (dijadikan spectator oleh DuelManager)
        event.setCancelled(false);
        event.getDrops().clear();
        event.setDroppedExp(0);
        event.deathMessage(null);

        var player = event.getEntity();
        var killer = player.getKiller();

        // respawn instan agar tidak melihat layar kematian lama, lalu jadikan spectator
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            player.spigot().respawn();
            plugin.getDuelManager().handleDeath(player, killer);
        });
    }
}
