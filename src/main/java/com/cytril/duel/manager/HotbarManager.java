package com.cytril.duel.manager;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

public class HotbarManager {

    public static final int SLOT_PARTY = 0;
    public static final int SLOT_PROFILE = 4;
    public static final int SLOT_MODE = 8;

    public void giveHotbar(Player player) {
        player.getInventory().setItem(SLOT_PARTY, buildItem(Material.GOLD_INGOT,
                "&6Party", "&7Klik untuk mengelola/mengundang party"));

        ItemStack head = buildItem(Material.PLAYER_HEAD, "&bProfil & Status",
                "&7Klik untuk melihat statistik duelmu");
        SkullMeta skullMeta = (SkullMeta) head.getItemMeta();
        skullMeta.setOwningPlayer(player);
        head.setItemMeta(skullMeta);
        player.getInventory().setItem(SLOT_PROFILE, head);

        player.getInventory().setItem(SLOT_MODE, buildItem(Material.NETHER_STAR,
                "&dPilih Mode Duel", "&7Klik untuk memilih mode/arena"));
    }

    public boolean isHotbarItem(ItemStack item) {
        if (item == null || item.getItemMeta() == null) return false;
        Material m = item.getType();
        return m == Material.GOLD_INGOT || m == Material.PLAYER_HEAD || m == Material.NETHER_STAR;
    }

    private ItemStack buildItem(Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(color(name)).decoration(TextDecoration.ITALIC, false));
        java.util.List<Component> loreList = new java.util.ArrayList<>();
        for (String line : lore) {
            loreList.add(Component.text(color(line)).decoration(TextDecoration.ITALIC, false));
        }
        meta.lore(loreList);
        item.setItemMeta(meta);
        return item;
    }

    private String color(String s) {
        return org.bukkit.ChatColor.translateAlternateColorCodes('&', s);
    }
}
