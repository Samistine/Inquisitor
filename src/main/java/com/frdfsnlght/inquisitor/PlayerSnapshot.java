package com.frdfsnlght.inquisitor;

import java.util.Date;
import java.util.UUID;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Thread Safe, copies stuff from the BukkitPlayer on creation.
 *
 * @author Samuel
 */
public class PlayerSnapshot {

    private final Date date;

    private final UUID uuid;
    private final String name;
    private final Location location;
    private final GameMode gameMode;
    private final ItemStack itemInHand;
    private final int foodLevel;
    private final boolean flying;
    private final boolean insideVehicle;
    private final Entity vehicle;
    private final boolean sneaking;
    private final boolean sprinting;
    private final EntityDamageEvent lastDamageCause;

    public PlayerSnapshot(Player player) {
        this.uuid = player.getUniqueId();
        this.name = player.getName();
        this.location = player.getLocation();
        this.gameMode = player.getGameMode();
        this.itemInHand = player.getItemInHand();
        this.foodLevel = player.getFoodLevel();
        this.flying = player.isFlying();
        this.insideVehicle = player.isInsideVehicle();
        this.vehicle = player.getVehicle();
        this.sneaking = player.isSneaking();
        this.sprinting = player.isSprinting();
        this.lastDamageCause = player.getLastDamageCause();
        this.date = new Date();
    }

    public UUID getUUID() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public Location getLocation() {
        return location;
    }

    public GameMode getGameMode() {
        return gameMode;
    }

    /**
     * Not Safely Mutable
     *
     * @return item in hand
     */
    public ItemStack getItemInHand() {
        return itemInHand;
    }

    public int getFoodLevel() {
        return foodLevel;
    }

    public boolean isFlying() {
        return flying;
    }

    public boolean isInsideVehicle() {
        return insideVehicle;
    }

    public Entity getVehicle() {
        return vehicle;
    }

    public boolean isSneaking() {
        return sneaking;
    }

    public boolean isSprinting() {
        return sprinting;
    }

    public EntityDamageEvent getLastDamageCause() {
        return lastDamageCause;
    }

    public Date getDate() {
        return date;
    }

}
