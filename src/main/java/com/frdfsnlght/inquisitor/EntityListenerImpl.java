/*
 * Copyright 2012 frdfsnlght <frdfsnlght@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.frdfsnlght.inquisitor;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;

import org.bukkit.inventory.ItemStack;

/**
 * Handles Spigot's EntityEvents
 *
 * @see PlayerListenerImpl
 * @see BlockListenerImpl
 *
 * @author Thomas Bennedum <frdfsnlght@gmail.com>
 */
public final class EntityListenerImpl implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            PlayerSnapshot player = new PlayerSnapshot((Player) event.getEntity());

            if (PlayerStats.isStatsPlayer(player)) {
                float fallDistance = event.getEntity().getFallDistance();
                PlayerStats.pool.submit(() -> {
                    Utils.debug("onPlayerFall '%s'", player.getName());
                    Statistics stats = PlayerStats.group.getStatistics(player);
                    stats.add(Statistic.travelDistances, "Falling", fallDistance);
                });
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        Entity deadEnt = event.getEntity();
        if (!(deadEnt.getLastDamageCause() instanceof EntityDamageByEntityEvent)) {
            return;
        }

        EntityDamageByEntityEvent killEvent = (EntityDamageByEntityEvent) deadEnt.getLastDamageCause();
        Entity killerEnt = killEvent.getDamager();

        if (killerEnt instanceof Projectile) {
            killerEnt = (Entity) ((Projectile) killerEnt).getShooter();
        }

        if (!(killerEnt instanceof Player)) {
            return;
        }

        PlayerSnapshot player = new PlayerSnapshot((Player) killerEnt);
        if (!PlayerStats.isStatsPlayer(player)) {
            return;
        }

        ItemStack inHand = player.getItemInHand();
        Material weapon = null;
        if (inHand != null) {
            weapon = inHand.getType();
        }

        String killerName = player.getName();

        String weaponName = ((weapon == null) || (weapon == Material.AIR)) ? "None" : Utils.titleCase(weapon.toString());

        if (deadEnt instanceof Player) {
            String deadName = ((Player) deadEnt).getName();
            PlayerStats.pool.submit(() -> {
                Utils.debug("onPlayerKill '%s'", killerName);
                Statistics stats = PlayerStats.group.getStatistics(killerName);
                stats.incr(Statistic.totalPlayersKilled);
                stats.set(Statistic.lastPlayerKill, player.getDate());
                stats.set(Statistic.lastPlayerKilled, deadName);
                stats.incr(Statistic.playersKilled, deadName);
                stats.incr(Statistic.playersKilledByWeapon, weaponName);
            });
        } else {
            String deadName = Utils.normalizeEntityTypeName(deadEnt.getType());
            PlayerStats.pool.submit(() -> {
                Utils.debug("onMobKill '%s'", killerName);
                Statistics stats = PlayerStats.group.getStatistics(killerName);
                stats.incr(Statistic.totalMobsKilled);
                stats.set(Statistic.lastMobKill, player.getDate());
                stats.set(Statistic.lastMobKilled, deadName);
                stats.incr(Statistic.mobsKilled, deadName);
                stats.incr(Statistic.mobsKilledByWeapon, weaponName);
            });
        }
    }

    /**
     * Records arrows shot by players.
     *
     * @param event EntityShootBowEvent
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityShootBow(EntityShootBowEvent event) {
        if (event.getEntity() instanceof Player) {
            PlayerSnapshot player = new PlayerSnapshot((Player) event.getEntity());
            if (PlayerStats.isStatsPlayer(player)) {
                PlayerStats.pool.submit(() -> {
                    Utils.debug("onPlayerBowShoot '%s'", player.getName());
                    Statistics stats = PlayerStats.group.getStatistics(player);
                    stats.incr(Statistic.arrowsShot);
                });
            }
        }
    }

    /**
     * Records entities, by type, tamed by players.
     *
     * @param event EntityTameEvent
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityTame(EntityTameEvent event) {
        if (event.getOwner() instanceof Player) {
            PlayerSnapshot player = new PlayerSnapshot((Player) event.getOwner());
            if (PlayerStats.isStatsPlayer(player)) {
                EntityType entityType = event.getEntityType();
                PlayerStats.pool.submit(() -> {
                    Utils.debug("onPlayerTameAnimal '%s'", player.getName());
                    Statistics stats = PlayerStats.group.getStatistics(player);
                    stats.incr(Statistic.animalsTamed, Utils.normalizeEntityTypeName(entityType));
                });
            }
        }
    }

    /**
     * Records food, by type, digested by players.
     * <br>TODO: Remove food level changed check, for edible foods that don't
     * change food level
     *
     * @param event FoodLevelChangeEvent
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player) {
            PlayerSnapshot player = new PlayerSnapshot((Player) event.getEntity());
            if (PlayerStats.isStatsPlayer(player)) {
                if (event.getFoodLevel() <= player.getFoodLevel()) {
                    return;
                }
                Material food = player.getItemInHand().getType();
                if (food.isEdible()) {
                    PlayerStats.pool.submit(() -> {
                        Utils.debug("onPlayerEat '%s'", player.getName());
                        Statistics stats = PlayerStats.group.getStatistics(player);
                        stats.incr(Statistic.foodEaten, Utils.titleCase(food.name()));
                    });
                }
            }
        }
    }

}
