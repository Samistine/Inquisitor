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
        final PlayerSnapshot player = new PlayerSnapshot(event.getPlayer());
        if (PlayerStats.isStatsPlayer(player)) {
            PlayerStats.pool.submit(() -> {
                PlayerStats.onPlayerJoin(player);
            });
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        final PlayerSnapshot player = new PlayerSnapshot(event.getPlayer());
        if (PlayerStats.isStatsPlayer(player)) {
            PlayerStats.pool.submit(() -> {
                PlayerStats.onPlayerQuit(player);
            });
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerKick(PlayerKickEvent event) {
        final PlayerSnapshot player = new PlayerSnapshot(event.getPlayer());
        if (PlayerStats.isStatsPlayer(player)) {
            String message = ChatColor.stripColor(event.getLeaveMessage());
            PlayerStats.pool.submit(() -> {
                PlayerStats.onPlayerKick(player, message);
            });
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        final PlayerSnapshot player = new PlayerSnapshot(event.getEntity());
        final String message = ChatColor.stripColor(event.getDeathMessage());
        final EntityDamageEvent damageEvent = player.getLastDamageCause();
        if (damageEvent == null) {
            return;
        }
        final EntityDamageEvent.DamageCause damageCause = damageEvent.getCause();
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
        final PlayerSnapshot player = new PlayerSnapshot(event.getPlayer());
        final Location to = event.getTo();
        if (PlayerStats.isStatsPlayer(player)) {
            PlayerStats.pool.submit(() -> {
                PlayerStats.onPlayerMove(player, to);
            });
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        final PlayerSnapshot player = new PlayerSnapshot(event.getPlayer());
        final Location to = event.getTo();
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
                final Statistics stats = PlayerStats.group.getStatistics(player);
                stats.incr(Statistic.chatMessages);
            });
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        final PlayerSnapshot player = new PlayerSnapshot(event.getPlayer());
        final ItemStack itemstack = event.getItemDrop().getItemStack();
        final int amount = itemstack.getAmount();
        final Material type = itemstack.getType();
        if (PlayerStats.isStatsPlayer(player)) {
            PlayerStats.pool.submit(() -> {
                final Statistics stats = PlayerStats.group.getStatistics(player);
                stats.add(Statistic.totalItemsDropped, amount);
                stats.add(Statistic.itemsDropped, Utils.titleCase(type.name()), amount);
            });
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        PlayerSnapshot player = new PlayerSnapshot(event.getPlayer());
        final ItemStack itemstack = event.getItem().getItemStack();
        final int amount = itemstack.getAmount();
        final Material type = itemstack.getType();
        if (PlayerStats.isStatsPlayer(player)) {
            PlayerStats.pool.submit(() -> {
                final Statistics stats = PlayerStats.group.getStatistics(player);
                stats.add(Statistic.totalItemsPickedUp, amount);
                stats.add(Statistic.itemsPickedUp, Utils.titleCase(type.name()), amount);
            });
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerPortal(PlayerPortalEvent event) {
        final PlayerSnapshot player = new PlayerSnapshot(event.getPlayer());
        if (PlayerStats.isStatsPlayer(player)) {
            PlayerStats.pool.submit(() -> {
                final Statistics stats = PlayerStats.group.getStatistics(player);
                stats.incr(Statistic.portalsCrossed);
            });
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        final PlayerSnapshot player = new PlayerSnapshot(event.getPlayer());
        final Material itemTypeInHand = event.getPlayer().getItemInHand().getType();
        final EntityType rightClickedType = event.getRightClicked().getType();
        if (PlayerStats.isStatsPlayer(player)) {
            PlayerStats.pool.submit(() -> {
                final Statistics stats = PlayerStats.group.getStatistics(player);
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
        final PlayerSnapshot player = new PlayerSnapshot(event.getPlayer());
        final int amount = event.getAmount();
        if (PlayerStats.isStatsPlayer(player)) {
            PlayerStats.pool.submit(() -> {
                final Statistics stats = PlayerStats.group.getStatistics(player);
                stats.add(Statistic.lifetimeExperience, amount);
            });
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEnchant(EnchantItemEvent event) {
        final PlayerSnapshot player = new PlayerSnapshot(event.getEnchanter());
        final int amount = event.getExpLevelCost();
        if (PlayerStats.isStatsPlayer(player)) {
            PlayerStats.pool.submit(() -> {
                final Statistics stats = PlayerStats.group.getStatistics(player);
                stats.incr(Statistic.itemsEnchanted);
                stats.add(Statistic.itemEnchantmentLevels, amount);
            });
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerEnterBed(PlayerBedEnterEvent event) {
        final PlayerSnapshot player = new PlayerSnapshot(event.getPlayer());
        if (PlayerStats.isStatsPlayer(player)) {
            PlayerStats.pool.submit(() -> {
                PlayerStats.onPlayerEnterBed(player);//Needs work
            });
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerShearEntity(PlayerShearEntityEvent event) {
        final PlayerSnapshot player = new PlayerSnapshot(event.getPlayer());
        final EntityType type = event.getEntity().getType();
        if (PlayerStats.isStatsPlayer(player)) {
            PlayerStats.pool.submit(() -> {
                final Statistics stats = PlayerStats.group.getStatistics(player);
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
        final PlayerSnapshot player = new PlayerSnapshot(event.getPlayer());
        if (event.getState() == PlayerFishEvent.State.CAUGHT_FISH) {
            if (PlayerStats.isStatsPlayer(player)) {
                PlayerStats.pool.submit(() -> {
                    final Statistics stats = PlayerStats.group.getStatistics(player);
                    stats.incr(Statistic.fishCaught);
                });
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerEggThrow(PlayerEggThrowEvent event) {
        final PlayerSnapshot player = new PlayerSnapshot(event.getPlayer());
        final EntityType hatchingType = event.getHatchingType();
        if (PlayerStats.isStatsPlayer(player)) {
            PlayerStats.pool.submit(() -> {
                final Statistics stats = PlayerStats.group.getStatistics(player);
                stats.incr(Statistic.eggsThrown, Utils.normalizeEntityTypeName(hatchingType));
            });
        }
    }

    //TODO: Some work here is needed
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerBucketFill(PlayerBucketFillEvent event) {//needs work
        final PlayerSnapshot player = new PlayerSnapshot(event.getPlayer());
        final Material bucket = event.getBucket();
        final Material blockClicked = event.getBlockClicked().getType();
        if (PlayerStats.isStatsPlayer(player)) {
            PlayerStats.pool.submit(() -> {
                final Statistics stats = PlayerStats.group.getStatistics(player);
                switch (bucket) {
                    case BUCKET:
                        switch (blockClicked) {
                            case WATER:
                            case STATIONARY_WATER:
                                stats.incr(Statistic.waterBucketsFilled);
                                break;
                            case LAVA:
                            case STATIONARY_LAVA:
                                stats.incr(Statistic.lavaBucketsFilled);
                                break;
                            default:
                                Utils.warning("onPlayerBucketFill 1: Something went wrong here");
                                break;
                        }
                        break;
                    case WATER_BUCKET:
                        stats.incr(Statistic.waterBucketsFilled);
                        break;
                    case LAVA_BUCKET:
                        stats.incr(Statistic.lavaBucketsFilled);
                        break;
                    default:
                        Utils.warning("onPlayerBucketFill 2: Something went wrong here");
                        break;
                }
            });
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {//needs work
        final PlayerSnapshot player = new PlayerSnapshot(event.getPlayer());
        final Material bucket = event.getBucket();
        if (PlayerStats.isStatsPlayer(player)) {
            PlayerStats.pool.submit(() -> {
                final Statistics stats = PlayerStats.group.getStatistics(player);
                switch (bucket) {
                    case WATER_BUCKET:
                        stats.incr(Statistic.waterBucketsEmptied);
                        break;
                    case LAVA_BUCKET:
                        stats.incr(Statistic.lavaBucketsEmptied);
                        break;
                    default:
                        Utils.warning("onPlayerBucketEmpty: Something went wrong here");
                        break;
                }
            });
        }
    }

    // not really a player event, but an inventory event
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCraftItem(CraftItemEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            final PlayerSnapshot player = new PlayerSnapshot((Player) event.getWhoClicked());
            final Material material = event.getRecipe().getResult().getType();
            final int amount = event.getRecipe().getResult().getAmount();
            if (PlayerStats.isStatsPlayer(player)) {
                PlayerStats.pool.submit(() -> {
                    final Statistics stats = PlayerStats.group.getStatistics(player);
                    stats.add(Statistic.totalItemsCrafted, amount);
                    stats.add(Statistic.itemsCrafted, Utils.titleCase(material.name()), amount);
                });
            }
        }
    }

}
