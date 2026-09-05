package com.cytril.duel.model;

import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

public class Arena {

    public enum Mode { DUEL, GLOBAL }

    private final String name;
    private final int width;
    private final List<String> kitNames = new ArrayList<>();
    private final Mode mode;
    private final Location center;

    // status runtime (tidak disimpan ke file)
    private boolean inUse = false;

    public Arena(String name, int width, List<String> kitNames, Mode mode, Location center) {
        this.name = name;
        this.width = width;
        this.kitNames.addAll(kitNames);
        this.mode = mode;
        this.center = center;
    }

    public String getName() { return name; }
    public int getWidth() { return width; }
    public List<String> getKitNames() { return kitNames; }
    public Mode getMode() { return mode; }
    public Location getCenter() { return center; }

    public boolean isInUse() { return inUse; }
    public void setInUse(boolean inUse) { this.inUse = inUse; }

    /** Titik spawn tim A (offset dari titik tengah). */
    public Location getSpawnA() {
        Location loc = center.clone();
        loc.setX(loc.getX() + (width / 2.0) - 1);
        return loc;
    }

    /** Titik spawn tim B (offset dari titik tengah). */
    public Location getSpawnB() {
        Location loc = center.clone();
        loc.setX(loc.getX() - (width / 2.0) + 1);
        loc.setYaw(loc.getYaw() + 180f);
        return loc;
    }

    public boolean allowsKit(String kitName) {
        return kitNames.stream().anyMatch(k -> k.equalsIgnoreCase(kitName));
    }
}
