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

import java.util.Date;
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
 *
 * @author Thomas Bennedum <frdfsnlght@gmail.com>
 */
public final class EntityListenerImpl implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (PlayerStats.isStatsPlayer(player)) {
                String pName = player.getName();
                float fallDistance = event.getEntity().getFallDistance();

                Statistics stats = PlayerStats.group.getStatistics(pName);
                stats.add(Statistic.travelDistances, "Falling", fallDistance);
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

        Player player = (Player) killerEnt;
        if (!PlayerStats.isStatsPlayer(player)) {
            return;
        }

        ItemStack inHand = player.getItemInHand();
        Material weapon = null;
        if (inHand != null) {
            weapon = inHand.getType();
        }

        String killerName = player.getName();
        Statistics stats = PlayerStats.group.getStatistics(killerName);
        String weaponName = ((weapon == null) || (weapon == Material.AIR)) ? "None" : Utils.titleCase(weapon.toString());

        if (deadEnt instanceof Player) {
            String deadName = ((Player) deadEnt).getName();
            Utils.debug("onPlayerKill '%s'", killerName);
            stats.incr(Statistic.totalPlayersKilled);
            stats.set(Statistic.lastPlayerKill, new Date());
            stats.set(Statistic.lastPlayerKilled, deadName);
            stats.incr(Statistic.playersKilled, deadName);
            stats.incr(Statistic.playersKilledByWeapon, weaponName);
        } else {
            String deadName = Utils.normalizeEntityTypeName(deadEnt.getType());
            Utils.debug("onMobKill '%s'", killerName);
            stats.incr(Statistic.totalMobsKilled);
            stats.set(Statistic.lastMobKill, new Date());
            stats.set(Statistic.lastMobKilled, deadName);
            stats.incr(Statistic.mobsKilled, deadName);
            stats.incr(Statistic.mobsKilledByWeapon, weaponName);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityShootBow(EntityShootBowEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (PlayerStats.isStatsPlayer(player)) {
                String pName = player.getName();

                Statistics stats = PlayerStats.group.getStatistics(pName);
                stats.incr(Statistic.arrowsShot);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityTame(EntityTameEvent event) {
        if (event.getOwner() instanceof Player) {
            Player player = (Player) event.getOwner();
            if (PlayerStats.isStatsPlayer(player)) {
                String pName = player.getName();
                EntityType entityType = event.getEntityType();

                Statistics stats = PlayerStats.group.getStatistics(pName);
                stats.incr(Statistic.animalsTamed, Utils.normalizeEntityTypeName(entityType));
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (PlayerStats.isStatsPlayer(player)) {
                if (event.getFoodLevel() <= player.getFoodLevel()) {
                    return;
                }
                Material food = player.getItemInHand().getType();
                if (food.isEdible()) {
                    String pName = player.getName();

                    Statistics stats = PlayerStats.group.getStatistics(pName);
                    stats.incr(Statistic.foodEaten, Utils.titleCase(food.name()));
                }
            }
        }
    }

}
