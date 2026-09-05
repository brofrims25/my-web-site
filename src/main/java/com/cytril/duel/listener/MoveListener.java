package com.cytril.duel.listener;

import com.cytril.duel.CytrilDuel;
import com.cytril.duel.model.ActiveDuel;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class MoveListener implements Listener {
    private final CytrilDuel plugin;
    public MoveListener(CytrilDuel plugin) { this.plugin = plugin; }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        var player = event.getPlayer();
        ActiveDuel duel = plugin.getDuelManager().getActiveDuel(player.getUniqueId());
        if (duel != null && duel.isCountdown() && plugin.setting("duel.freeze-during-countdown", true)) {
            if (event.getTo() != null && (event.getFrom().getX() != event.getTo().getX() || event.getFrom().getZ() != event.getTo().getZ())) {
                event.setTo(event.getFrom());
            }
            return;
        }
        if (!plugin.getSpectatorManager().isRestrictedSpectator(player.getUniqueId())) return;
        plugin.getSpectatorManager().checkBounds(player);
    }
}
