package com.cytril.duel.model;

import org.bukkit.inventory.ItemStack;

public class Kit {

    private final String name;
    private final ItemStack[] contents; // 36 slot inventory utama
    private final ItemStack[] armor;    // 4 slot armor

    public Kit(String name, ItemStack[] contents, ItemStack[] armor) {
        this.name = name;
        this.contents = contents;
        this.armor = armor;
    }

    public String getName() { return name; }
    public ItemStack[] getContents() { return contents; }
    public ItemStack[] getArmor() { return armor; }
}
