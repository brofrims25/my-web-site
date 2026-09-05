package com.cytril.duel.listener;

import com.cytril.duel.CytrilDuel;
import com.cytril.duel.gui.PartyMenuGui;
import com.cytril.duel.gui.ProfileGui;
import com.cytril.duel.manager.HotbarManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class HotbarInteractListener implements Listener {

    private final CytrilDuel plugin;

    public HotbarInteractListener(CytrilDuel plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item == null || !plugin.getHotbarManager().isHotbarItem(item)) return;

        event.setCancelled(true);
        int slot = player.getInventory().getHeldItemSlot();

        if (plugin.getDuelManager().isDueling(player.getUniqueId())) {
            player.sendMessage(plugin.msg("&cKamu tidak bisa membuka menu ini saat sedang duel."));
            return;
        }

        if (slot == HotbarManager.SLOT_PARTY) {
            player.openInventory(new PartyMenuGui(plugin, player).getInventory());
        } else if (slot == HotbarManager.SLOT_PROFILE) {
            player.openInventory(new ProfileGui(plugin, player).getInventory());
        } else if (slot == HotbarManager.SLOT_MODE) {
            new com.cytril.duel.gui.ArenaSelectGui(plugin, player, null, com.cytril.duel.model.Arena.Mode.GLOBAL).open(player);
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        if (plugin.getHotbarManager().isHotbarItem(event.getItemDrop().getItemStack())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryMove(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        // cegah item lobby dipindah/dibuang lewat inventory pemain sendiri (bukan GUI plugin)
        if (event.getClickedInventory() != null
                && event.getClickedInventory().equals(player.getInventory())
                && plugin.getHotbarManager().isHotbarItem(event.getCurrentItem())) {
            event.setCancelled(true);
        }
    }
}
