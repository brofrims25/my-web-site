package com.cytril.duel.gui;

import com.cytril.duel.CytrilDuel;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

/** GUI item gold: undang pemain ke party, keluar party. */
public class PartyMenuGui extends CytrilInventoryHolder {

    private final CytrilDuel plugin;
    private final List<Player> listed = new ArrayList<>();

    public PartyMenuGui(CytrilDuel plugin, Player viewer) {
        this.plugin = plugin;
        Inventory inv = Bukkit.createInventory(this, 54, Component.text("Menu Party"));
        setInventory(inv);

        int slot = 0;
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.equals(viewer)) continue;
            if (slot >= 45) break;
            listed.add(online);
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta sm = (SkullMeta) head.getItemMeta();
            sm.setOwningPlayer(online);
            sm.displayName(Component.text(online.getName(), NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));
            sm.lore(List.of(Component.text("Klik untuk mengundang ke party", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)));
            head.setItemMeta(sm);
            inv.setItem(slot++, head);
        }

        ItemStack leave = new ItemStack(Material.REDSTONE_BLOCK);
        ItemMeta lm = leave.getItemMeta();
        lm.displayName(Component.text("Keluar Party", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
        leave.setItemMeta(lm);
        inv.setItem(49, leave);

        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta cm = close.getItemMeta();
        cm.displayName(Component.text("Tutup", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
        close.setItemMeta(cm);
        inv.setItem(53, close);
    }

    @Override
    public void onClick(Player player, int slot, InventoryClickEvent event) {
        event.setCancelled(true);
        if (slot == 53) {
            player.closeInventory();
            return;
        }
        if (slot == 49) {
            player.closeInventory();
            plugin.getPartyManager().leave(player);
            return;
        }
        if (slot < 0 || slot >= listed.size()) return;
        Player target = listed.get(slot);
        player.closeInventory();
        plugin.getPartyManager().invite(player, target);
    }
}
