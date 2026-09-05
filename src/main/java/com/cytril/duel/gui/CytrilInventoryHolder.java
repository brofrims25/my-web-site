package com.cytril.duel.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

/** Holder dasar untuk semua GUI plugin ini supaya bisa dikenali di InventoryClickEvent. */
public abstract class CytrilInventoryHolder implements InventoryHolder {

    private Inventory inventory;

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public abstract void onClick(org.bukkit.entity.Player player, int slot, org.bukkit.event.inventory.InventoryClickEvent event);
}
