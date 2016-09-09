/*
 * The MIT License
 *
 * Copyright 2016 Samuel Seidel.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
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
public class PlayerSnapshot implements InquisitorPlayer {

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

    @Override
    public UUID getUUID() {
        return uuid;
    }

    @Override
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
    @Override
    public String toString() {
        return "PlayerSnapshot{" + "uuid=" + uuid + ", name=" + name + '}';
    }

}
