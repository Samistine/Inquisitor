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

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerEggThrowEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import org.bukkit.inventory.ItemStack;

/**
 * Handles Spigot's PlayerEvents
 *
 * @see EntityListenerImpl
 * @see BlockListenerImpl
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */
public final class PlayerListenerImpl implements Listener {

    /*
     * @EventHandler(priority = EventPriority.NORMAL) public void
     * onPlayerPreLogin(PlayerPreLoginEvent event) { if (event.getResult() !=
     * PlayerPreLoginEvent.Result.ALLOWED) return; }
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        PlayerSnapshot player = new PlayerSnapshot(event.getPlayer());
        if (PlayerStats.isStatsPlayer(player)) {
            PlayerStats.pool.submit(() -> {
                PlayerStats.onPlayerJoin(player);
            });
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        PlayerSnapshot player = new PlayerSnapshot(event.getPlayer());
        if (PlayerStats.isStatsPlayer(player)) {
            PlayerStats.pool.submit(() -> {
                PlayerStats.onPlayerQuit(player);
            });
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerKick(PlayerKickEvent event) {
        PlayerSnapshot player = new PlayerSnapshot(event.getPlayer());
        if (PlayerStats.isStatsPlayer(player)) {
            String message = ChatColor.stripColor(event.getLeaveMessage());
            PlayerStats.pool.submit(() -> {
                PlayerStats.onPlayerKick(player, message);
            });
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        PlayerSnapshot player = new PlayerSnapshot(event.getEntity());
        String message = ChatColor.stripColor(event.getDeathMessage());
        EntityDamageEvent damageEvent = player.getLastDamageCause();
        if (damageEvent == null) {
            return;
        }
        EntityDamageEvent.DamageCause damageCause = damageEvent.getCause();
        if (PlayerStats.isStatsPlayer(player)) {
            PlayerStats.pool.submit(() -> {
                PlayerStats.onPlayerDeath(player, message, damageCause);
            });
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        // You can not short circuit this statement, each MUST be true.
        // Otherwise travel stats will not be correct
        if ((event.getFrom().getBlockX() == event.getTo().getBlockX())
                & (event.getFrom().getBlockY() == event.getTo().getBlockY())
                & (event.getFrom().getBlockZ() == event.getTo().getBlockZ())) {
            return;
        }
        Location to = event.getTo();
        PlayerSnapshot player = new PlayerSnapshot(event.getPlayer());
        if (PlayerStats.isStatsPlayer(player)) {
            PlayerStats.pool.submit(() -> {
                PlayerStats.onPlayerMove(player, to);
            });
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        PlayerSnapshot player = new PlayerSnapshot(event.getPlayer());
        Location to = event.getTo();
        if (PlayerStats.isStatsPlayer(player)) {
            PlayerStats.pool.submit(() -> {
                PlayerStats.onPlayerTeleport(player, to);
            });
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerChatAsync(AsyncPlayerChatEvent event) {
        final PlayerSnapshot player = new PlayerSnapshot(event.getPlayer());

        if (PlayerStats.isStatsPlayer(player)) {
            PlayerStats.pool.submit(() -> {
                PlayerStats.group.getStatistics(player).incr(Statistic.chatMessages);
            });
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        PlayerSnapshot player = new PlayerSnapshot(event.getPlayer());
        if (PlayerStats.isStatsPlayer(player)) {
            ItemStack itemstack = event.getItemDrop().getItemStack();
            int amount = itemstack.getAmount();
            Material type = itemstack.getType();

            PlayerStats.pool.submit(() -> {
                Statistics stats = PlayerStats.group.getStatistics(player);
                stats.add(Statistic.totalItemsDropped, amount);
                stats.add(Statistic.itemsDropped, Utils.titleCase(type.name()), amount);
            });
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        PlayerSnapshot player = new PlayerSnapshot(event.getPlayer());
        if (PlayerStats.isStatsPlayer(player)) {
            ItemStack itemstack = event.getItem().getItemStack();
            int amount = itemstack.getAmount();
            Material type = itemstack.getType();
            PlayerStats.pool.submit(() -> {
                Statistics stats = PlayerStats.group.getStatistics(player);
                stats.add(Statistic.totalItemsPickedUp, amount);
                stats.add(Statistic.itemsPickedUp, Utils.titleCase(type.name()), amount);
            });
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerPortal(PlayerPortalEvent event) {
        PlayerSnapshot player = new PlayerSnapshot(event.getPlayer());
        if (PlayerStats.isStatsPlayer(player)) {
            PlayerStats.pool.submit(() -> {
                Statistics stats = PlayerStats.group.getStatistics(player);
                stats.incr(Statistic.portalsCrossed);
            });
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        PlayerSnapshot player = new PlayerSnapshot(event.getPlayer());
        if (PlayerStats.isStatsPlayer(player)) {
            Material itemTypeInHand = event.getPlayer().getItemInHand().getType();
            EntityType rightClickedType = event.getRightClicked().getType();//TODO: Not thread safe variable
            PlayerStats.pool.submit(() -> {
                Statistics stats = PlayerStats.group.getStatistics(player);
                switch (itemTypeInHand) {
                    case BUCKET:
                        if (rightClickedType == EntityType.COW) {
                            stats.incr(Statistic.cowsMilked);
                        }
                        break;
                    case BOWL:
                        if (rightClickedType == EntityType.MUSHROOM_COW) {
                            stats.incr(Statistic.mooshroomsMilked);
                        }
                        break;
                    case INK_SACK:
                        if (rightClickedType == EntityType.SHEEP) {
                            stats.incr(Statistic.sheepDyed);
                        }
                        break;
                }
            });
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerExp(PlayerExpChangeEvent event) {
        PlayerSnapshot player = new PlayerSnapshot(event.getPlayer());
        if (PlayerStats.isStatsPlayer(player)) {
            int amount = event.getAmount();
            PlayerStats.pool.submit(() -> {
                Statistics stats = PlayerStats.group.getStatistics(player);
                stats.add(Statistic.lifetimeExperience, amount);
            });
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEnchant(EnchantItemEvent event) {
        PlayerSnapshot player = new PlayerSnapshot(event.getEnchanter());
        if (PlayerStats.isStatsPlayer(player)) {
            int amount = event.getExpLevelCost();
            PlayerStats.pool.submit(() -> {
                Statistics stats = PlayerStats.group.getStatistics(player);
                stats.incr(Statistic.itemsEnchanted);
                stats.add(Statistic.itemEnchantmentLevels, amount);
            });
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerEnterBed(PlayerBedEnterEvent event) {
        PlayerSnapshot player = new PlayerSnapshot(event.getPlayer());
        if (PlayerStats.isStatsPlayer(player)) {
            PlayerStats.pool.submit(() -> {
                PlayerStats.onPlayerEnterBed(player);//Needs work
            });
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerShearEntity(PlayerShearEntityEvent event) {
        PlayerSnapshot player = new PlayerSnapshot(event.getPlayer());
        if (PlayerStats.isStatsPlayer(player)) {
            EntityType type = event.getEntity().getType();
            PlayerStats.pool.submit(() -> {
                Statistics stats = PlayerStats.group.getStatistics(player);
                switch (type) {
                    case SHEEP:
                        stats.incr(Statistic.sheepSheared);
                        break;
                    case MUSHROOM_COW:
                        stats.incr(Statistic.mooshroomsSheared);
                        break;
                }
            });
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerFishEvent(PlayerFishEvent event) {
        PlayerSnapshot player = new PlayerSnapshot(event.getPlayer());
        if (event.getState() == PlayerFishEvent.State.CAUGHT_FISH) {
            if (PlayerStats.isStatsPlayer(player)) {
                PlayerStats.pool.submit(() -> {
                    Statistics stats = PlayerStats.group.getStatistics(player);
                    stats.incr(Statistic.fishCaught);
                });
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerEggThrow(PlayerEggThrowEvent event) {
        PlayerSnapshot player = new PlayerSnapshot(event.getPlayer());
        if (PlayerStats.isStatsPlayer(player)) {
            EntityType hatchingType = event.getHatchingType();
            PlayerStats.pool.submit(() -> {
                Statistics stats = PlayerStats.group.getStatistics(player);
                stats.incr(Statistic.eggsThrown, Utils.normalizeEntityTypeName(hatchingType));
            });
        }
    }

    //TODO: Some work here is needed
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerBucketFill(PlayerBucketFillEvent event) {//needs work
        PlayerSnapshot player = new PlayerSnapshot(event.getPlayer());
        if (PlayerStats.isStatsPlayer(player)) {
            Material bucket = event.getBucket();
            switch (bucket) {
                case BUCKET:
                    switch (event.getBlockClicked().getType()) {
                        case WATER:
                        case STATIONARY_WATER:
                            PlayerStats.pool.submit(() -> {
                                PlayerStats.group.getStatistics(player).incr(Statistic.waterBucketsFilled);
                            });
                            break;
                        case LAVA:
                        case STATIONARY_LAVA:
                            PlayerStats.pool.submit(() -> {
                                PlayerStats.group.getStatistics(player).incr(Statistic.lavaBucketsFilled);
                            });
                            break;
                    }
                    break;
                case WATER_BUCKET:
                    PlayerStats.pool.submit(() -> {
                        PlayerStats.group.getStatistics(player).incr(Statistic.waterBucketsFilled);
                    });
                    break;
                case LAVA_BUCKET:
                    PlayerStats.pool.submit(() -> {
                        PlayerStats.group.getStatistics(player).incr(Statistic.lavaBucketsFilled);
                    });
                    break;
            }

        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {//needs work
        PlayerSnapshot player = new PlayerSnapshot(event.getPlayer());
        if (PlayerStats.isStatsPlayer(player)) {
            Material bucket = event.getBucket();
            PlayerStats.pool.submit(() -> {
                Statistics stats = PlayerStats.group.getStatistics(player);
                switch (bucket) {
                    case WATER_BUCKET:
                        stats.incr(Statistic.waterBucketsEmptied);
                        break;
                    case LAVA_BUCKET:
                        stats.incr(Statistic.lavaBucketsEmptied);
                        break;
                }
            });
        }
    }

    // not really a player event, but an inventory event
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCraftItem(CraftItemEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            PlayerSnapshot player = new PlayerSnapshot((Player) event.getWhoClicked());
            if (PlayerStats.isStatsPlayer(player)) {
                int amount = event.getRecipe().getResult().getAmount();
                Material material = event.getRecipe().getResult().getType();
                PlayerStats.pool.submit(() -> {
                    Statistics stats = PlayerStats.group.getStatistics(player);
                    stats.add(Statistic.totalItemsCrafted, amount);
                    stats.add(Statistic.itemsCrafted, Utils.titleCase(material.name()), amount);
                });
            }
        }
    }

}
