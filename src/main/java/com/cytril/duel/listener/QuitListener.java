package com.cytril.duel.listener;

import com.cytril.duel.CytrilDuel;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class QuitListener implements Listener {

    private final CytrilDuel plugin;

    public QuitListener(CytrilDuel plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        // jika sedang duel, dianggap kalah/mati agar lawan tetap bisa lanjut/menang
        if (plugin.getDuelManager().isDueling(event.getPlayer().getUniqueId())) {
            plugin.getDuelManager().handleQuitDuringDuel(event.getPlayer());
        }
        // party: jika leader keluar -> party dibubarkan; jika member -> keluar otomatis
        plugin.getPartyManager().handleQuit(event.getPlayer().getUniqueId());
        plugin.getSpectatorManager().clear(event.getPlayer().getUniqueId());
    }
}
