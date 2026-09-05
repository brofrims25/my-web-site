package com.cytril.duel.listener;

import com.cytril.duel.CytrilDuel;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinListener implements Listener {

    private final CytrilDuel plugin;

    public JoinListener(CytrilDuel plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        // requirement: setiap login ulang, pemain tidak otomatis berada di party manapun
        // (party sebelumnya sudah dibersihkan saat quit, lihat QuitListener)
        if (plugin.setting("general.auto-teleport-to-lobby-on-join", true)) plugin.teleportToLobby(event.getPlayer());
        if (plugin.setting("hotbar.enabled", true) && plugin.setting("hotbar.refresh-on-join", true)) plugin.getHotbarManager().giveHotbar(event.getPlayer());
        plugin.sendConfigured(event.getPlayer(), "join", "&aSelamat datang di &6CytrilDuel&a! Ketik &f/duel &auntuk mulai.", null);
    }
}
