package com.cytril.duel.gui;

import com.cytril.duel.CytrilDuel;
import com.cytril.duel.model.Arena;
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

import java.util.ArrayList;
import java.util.List;

/**
 * GUI pemilihan arena/maps (mode DUEL: kirim ajakan ke target tertentu,
 * mode GLOBAL: langsung join arena FFA yang dipilih).
 */
public class ArenaSelectGui extends CytrilInventoryHolder {

    private final CytrilDuel plugin;
    private final Player target; // null jika mode GLOBAL
    private final Arena.Mode mode;
    private final List<Arena> listed = new ArrayList<>();

    public ArenaSelectGui(CytrilDuel plugin, Player viewer, Player target, Arena.Mode mode) {
        this.plugin = plugin;
        this.target = target;
        this.mode = mode;

        Inventory inv = Bukkit.createInventory(this, 27,
                Component.text(mode == Arena.Mode.DUEL ? "Pilih Arena & Kit" : "Pilih Arena Global"));
        setInventory(inv);

        int slot = 0;
        for (Arena arena : plugin.getArenaManager().getByMode(mode)) {
            if (slot >= 26) break;
            listed.add(arena);
            inv.setItem(slot++, buildArenaItem(arena));
        }

        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta cm = close.getItemMeta();
        cm.displayName(Component.text("Tutup", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
        close.setItemMeta(cm);
        inv.setItem(26, close);
    }

    private ItemStack buildArenaItem(Arena arena) {
        Material mat = arena.isInUse() ? Material.RED_STAINED_GLASS_PANE : Material.LIME_STAINED_GLASS_PANE;
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(arena.getName(), NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Kit: " + String.join(", ", arena.getKitNames()), NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("Status: " + (arena.isInUse() ? "Sedang dipakai" : "Kosong"), NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    public void open(Player viewer) {
        viewer.openInventory(getInventory());
    }

    @Override
    public void onClick(Player player, int slot, InventoryClickEvent event) {
        event.setCancelled(true);
        if (slot == 26) {
            player.closeInventory();
            return;
        }
        if (slot < 0 || slot >= listed.size()) return;
        Arena arena = listed.get(slot);

        if (arena.isInUse() && mode == Arena.Mode.DUEL) {
            player.sendMessage(plugin.msg("&cArena sedang dipakai, pilih arena lain."));
            return;
        }

        String kitName = arena.getKitNames().isEmpty() ? null : arena.getKitNames().get(0);

        if (mode == Arena.Mode.GLOBAL) {
            player.closeInventory();
            plugin.getDuelManager().joinGlobal(player, arena);
            return;
        }

        if (target == null || !target.isOnline()) {
            player.sendMessage(plugin.msg("&cTarget sudah tidak online."));
            player.closeInventory();
            return;
        }

        player.closeInventory();
        plugin.getDuelManager().sendRequest(player, target, arena, kitName);
    }
}
