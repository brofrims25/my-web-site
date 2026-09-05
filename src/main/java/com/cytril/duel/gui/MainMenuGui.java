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

/** GUI /duel: menampilkan daftar pemain online (kepala skin) untuk diajak duel + tombol keluar. */
public class MainMenuGui extends CytrilInventoryHolder {

    private final CytrilDuel plugin;
    private final List<Player> listed = new ArrayList<>();

    public MainMenuGui(CytrilDuel plugin, Player viewer) {
        this.plugin = plugin;
        Inventory inv = Bukkit.createInventory(this, 54, Component.text("Pilih Lawan Duel"));
        setInventory(inv);

        int slot = 0;
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.equals(viewer)) continue;
            if (slot >= 45) break;
            listed.add(online);
            inv.setItem(slot++, buildHead(online));
        }

        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta meta = close.getItemMeta();
        meta.displayName(Component.text("Tutup", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
        close.setItemMeta(meta);
        inv.setItem(49, close);
    }

    private ItemStack buildHead(Player target) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        meta.setOwningPlayer(target);
        meta.displayName(Component.text(target.getName(), NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(
                Component.text("Klik untuk memilih arena & kit", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
                Component.text("lalu kirim ajakan duel.", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)
        ));
        head.setItemMeta(meta);
        return head;
    }

    @Override
    public void onClick(Player player, int slot, InventoryClickEvent event) {
        event.setCancelled(true);
        if (slot == 49) {
            player.closeInventory();
            return;
        }
        if (slot < 0 || slot >= listed.size()) return;
        Player target = listed.get(slot);
        if (target == null || !target.isOnline()) {
            player.sendMessage(plugin.msg("&cPemain tersebut sudah offline."));
            return;
        }
        new ArenaSelectGui(plugin, player, target, com.cytril.duel.model.Arena.Mode.DUEL).open(player);
    }
}
