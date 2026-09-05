package com.cytril.duel.listener;

import com.cytril.duel.CytrilDuel;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class MoveListener implements Listener {

    private final CytrilDuel plugin;

    public MoveListener(CytrilDuel plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!plugin.getSpectatorManager().isRestrictedSpectator(event.getPlayer().getUniqueId())) return;
        plugin.getSpectatorManager().checkBounds(event.getPlayer());
    }
}
