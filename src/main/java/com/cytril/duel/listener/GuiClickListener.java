package com.cytril.duel.listener;

import com.cytril.duel.gui.CytrilInventoryHolder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

public class GuiClickListener implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof CytrilInventoryHolder holder)) return;
        if (!(event.getWhoClicked() instanceof Player player)) return;

        // klik di inventory pemain sendiri (bagian bawah) saat GUI terbuka -> batalkan juga supaya item tidak tertukar
        event.setCancelled(true);

        if (event.getClickedInventory() == null || !event.getClickedInventory().equals(event.getInventory())) {
            return; // klik terjadi di inventory pemain, bukan di GUI
        }

        holder.onClick(player, event.getSlot(), event);
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof CytrilInventoryHolder) {
            event.setCancelled(true);
        }
    }
}
