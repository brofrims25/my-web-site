package com.cytril.duel.manager;

import com.cytril.duel.CytrilDuel;
import com.cytril.duel.model.Kit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.*;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

public class KitManager {

    private final CytrilDuel plugin;
    private final File file;
    private FileConfiguration cfg;
    private final Map<String, Kit> kits = new LinkedHashMap<>();

    public KitManager(CytrilDuel plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "kits.yml");
        load();
    }

    public void load() {
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Gagal membuat kits.yml: " + e.getMessage());
            }
        }
        cfg = YamlConfiguration.loadConfiguration(file);
        kits.clear();
        if (cfg.getConfigurationSection("kits") == null) return;
        for (String name : cfg.getConfigurationSection("kits").getKeys(false)) {
            try {
                String contentsB64 = cfg.getString("kits." + name + ".contents");
                String armorB64 = cfg.getString("kits." + name + ".armor");
                ItemStack[] contents = deserialize(contentsB64);
                ItemStack[] armor = deserialize(armorB64);
                kits.put(name.toLowerCase(), new Kit(name, contents, armor));
            } catch (Exception e) {
                plugin.getLogger().warning("Gagal load kit " + name + ": " + e.getMessage());
            }
        }
    }

    public void setKit(String name, Player player) {
        PlayerInventory inv = player.getInventory();
        ItemStack[] contents = inv.getContents().clone();
        ItemStack[] armor = inv.getArmorContents().clone();
        Kit kit = new Kit(name, contents, armor);
        kits.put(name.toLowerCase(), kit);

        try {
            cfg.set("kits." + name + ".contents", serialize(contents));
            cfg.set("kits." + name + ".armor", serialize(armor));
            cfg.save(file);
        } catch (Exception e) {
            plugin.getLogger().severe("Gagal menyimpan kit: " + e.getMessage());
        }
    }

    public Kit get(String name) {
        return kits.get(name.toLowerCase());
    }

    public Map<String, Kit> getAll() {
        return kits;
    }

    public void giveKit(Kit kit, Player player) {
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.getInventory().setContents(cloneArray(kit.getContents()));
        player.getInventory().setArmorContents(cloneArray(kit.getArmor()));
        player.updateInventory();
    }

    private ItemStack[] cloneArray(ItemStack[] src) {
        ItemStack[] out = new ItemStack[src.length];
        for (int i = 0; i < src.length; i++) out[i] = src[i] == null ? null : src[i].clone();
        return out;
    }

    private String serialize(ItemStack[] items) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (BukkitObjectOutputStream boos = new BukkitObjectOutputStream(baos)) {
            boos.writeInt(items.length);
            for (ItemStack item : items) boos.writeObject(item);
        }
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }

    private ItemStack[] deserialize(String data) throws IOException, ClassNotFoundException {
        if (data == null) return new ItemStack[0];
        ByteArrayInputStream bais = new ByteArrayInputStream(Base64.getDecoder().decode(data));
        try (BukkitObjectInputStream bois = new BukkitObjectInputStream(bais)) {
            int len = bois.readInt();
            ItemStack[] items = new ItemStack[len];
            for (int i = 0; i < len; i++) items[i] = (ItemStack) bois.readObject();
            return items;
        }
    }
}
