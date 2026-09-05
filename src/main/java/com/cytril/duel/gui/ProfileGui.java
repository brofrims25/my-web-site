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

import java.util.List;

public class ProfileGui extends CytrilInventoryHolder {

    public ProfileGui(CytrilDuel plugin, Player viewer) {
        Inventory inv = Bukkit.createInventory(this, 27, Component.text("Profil Duel - " + viewer.getName()));
        setInventory(inv);

        int wins = plugin.getStatsManager().getWins(viewer.getUniqueId());
        int losses = plugin.getStatsManager().getLosses(viewer.getUniqueId());
        int kills = plugin.getStatsManager().getKills(viewer.getUniqueId());

        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta sm = (SkullMeta) head.getItemMeta();
        sm.setOwningPlayer(viewer);
        sm.displayName(Component.text(viewer.getName(), NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));
        sm.lore(List.of(
                Component.text("Menang: " + wins, NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false),
                Component.text("Kalah: " + losses, NamedTextColor.RED).decoration(TextDecoration.ITALIC, false),
                Component.text("Kill: " + kills, NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false)
        ));
        head.setItemMeta(sm);
        inv.setItem(13, head);

        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta cm = close.getItemMeta();
        cm.displayName(Component.text("Tutup", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
        close.setItemMeta(cm);
        inv.setItem(22, close);
    }

    @Override
    public void onClick(Player player, int slot, InventoryClickEvent event) {
        event.setCancelled(true);
        if (slot == 22) player.closeInventory();
    }
}
