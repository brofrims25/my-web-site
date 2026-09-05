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

import java.util.ArrayList;
import java.util.List;

/** Admin GUI untuk toggle setting PvP yang sering diubah saat server berjalan. */
public class SettingsGui extends CytrilInventoryHolder {
    private final CytrilDuel plugin;
    private final List<String> paths = List.of(
            "pvp.enabled", "pvp.allow-melee", "pvp.allow-projectiles", "pvp.allow-bows",
            "pvp.allow-potions", "pvp.allow-ender-pearl", "pvp.allow-fall-damage", "pvp.allow-fire-damage",
            "pvp.natural-regeneration", "pvp.friendly-fire", "duel.freeze-during-countdown", "duel.prevent-build",
            "duel.prevent-break", "combat.combat-log-enabled", "spectator.enabled", "arena.global.enabled"
    );
    private final List<String> labels = List.of(
            "PvP Global", "Melee", "Projectile", "Bow/Crossbow", "Potion", "Ender Pearl", "Fall Damage", "Fire Damage",
            "Natural Regeneration", "Friendly Fire", "Freeze Countdown", "Block Place", "Block Break", "Combat Log",
            "Spectator", "Global/FFA"
    );

    public SettingsGui(CytrilDuel plugin) {
        this.plugin = plugin;
        Inventory inv = Bukkit.createInventory(this, 27, Component.text("CytrilDuel Settings"));
        setInventory(inv);
        refresh();
    }

    private void refresh() {
        Inventory inv = getInventory();
        inv.clear();
        for (int i = 0; i < paths.size(); i++) inv.setItem(i, toggle(paths.get(i), labels.get(i)));
        ItemStack reload = item(Material.CLOCK, "&eReload Config", "Klik untuk memuat ulang config.yml");
        inv.setItem(22, reload);
        ItemStack close = item(Material.BARRIER, "&cTutup", "Tutup settings");
        inv.setItem(26, close);
    }

    private ItemStack toggle(String path, String label) {
        boolean value = plugin.getConfig().getBoolean(path, true);
        Material mat = value ? Material.LIME_DYE : Material.GRAY_DYE;
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(label, value ? NamedTextColor.GREEN : NamedTextColor.RED)
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(
                Component.text("Path: " + path, NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
                Component.text("Status: " + (value ? "ON" : "OFF"), value ? NamedTextColor.GREEN : NamedTextColor.RED)
                        .decoration(TextDecoration.ITALIC, false),
                Component.text("Klik untuk toggle", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false)
        ));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack item(Material material, String title, String loreText) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacyAmpersand().deserialize(title).decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(Component.text(loreText, NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)));
        item.setItemMeta(meta);
        return item;
    }

    public void open(Player player) { player.openInventory(getInventory()); }

    @Override
    public void onClick(Player player, int slot, InventoryClickEvent event) {
        event.setCancelled(true);
        if (!player.hasPermission("cytrilduel.admin")) return;
        if (slot == 26) { player.closeInventory(); return; }
        if (slot == 22) {
            plugin.reloadConfig();
            refresh();
            player.sendMessage(plugin.msg("&aConfig berhasil di-reload."));
            return;
        }
        if (slot < 0 || slot >= paths.size()) return;
        String path = paths.get(slot);
        boolean next = !plugin.getConfig().getBoolean(path, true);
        plugin.getConfig().set(path, next);
        plugin.saveConfig();
        refresh();
        plugin.playConfiguredSound(player, "click");
    }
}
