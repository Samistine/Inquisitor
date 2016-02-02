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
import org.bukkit.entity.Cow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.MushroomCow;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
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
        Player player = event.getPlayer();
        if (PlayerStats.isStatsPlayer(player) && PlayerStats.hasNoPendingLogin(player.getUniqueId())) {
            PlayerStats.onPlayerJoin(player);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (PlayerStats.isStatsPlayer(player) && PlayerStats.hasNoPendingLogin(player.getUniqueId())) {
            PlayerStats.onPlayerQuit(player);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerKick(PlayerKickEvent event) {
        Player player = event.getPlayer();
        if (PlayerStats.isStatsPlayer(player) && PlayerStats.hasNoPendingLogin(player.getUniqueId())) {
            String message = ChatColor.stripColor(event.getLeaveMessage());
            PlayerStats.onPlayerKick(player, message);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = (Player) event.getEntity();
        String message = ChatColor.stripColor(event.getDeathMessage());
        EntityDamageEvent damageEvent = player.getLastDamageCause();
        if (damageEvent == null) {
            return;
        }
        EntityDamageEvent.DamageCause damageCause = damageEvent.getCause();
        if (PlayerStats.isStatsPlayer(player) && PlayerStats.hasNoPendingLogin(player.getUniqueId())) {
            PlayerStats.onPlayerDeath(player, message, damageCause);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        // You can not short circuit this statement, each MUST be true.
        // Otherwise travel stats will not be correct
        Player player = event.getPlayer();
        if (player.isOnline()) {
            if ((event.getFrom().getBlockX() == event.getTo().getBlockX())
                    & (event.getFrom().getBlockY() == event.getTo().getBlockY())
                    & (event.getFrom().getBlockZ() == event.getTo().getBlockZ())) {
                return;
            }
            Location to = event.getTo();
            if (PlayerStats.isStatsPlayer(player) && PlayerStats.hasNoPendingLogin(player.getUniqueId())) {
                PlayerStats.onPlayerMove(player, to);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        Location to = event.getTo();
        if (PlayerStats.isStatsPlayer(player) && PlayerStats.hasNoPendingLogin(player.getUniqueId())) {
            PlayerStats.onPlayerTeleport(player, to);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerChatAsync(AsyncPlayerChatEvent event) {
        if (PlayerStats.isStatsPlayer(event.getPlayer()) && PlayerStats.hasNoPendingLogin(event.getPlayer().getUniqueId())) {
            final String pName = event.getPlayer().getName();
            if (event.isAsynchronous()) {
                Utils.fire(new Runnable() {
                    public void run() {
                        PlayerStats.group.getStatistics(pName).incr(Statistic.chatMessages);
                    }
                });
            } else {
                PlayerStats.group.getStatistics(pName).incr(Statistic.chatMessages);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (PlayerStats.isStatsPlayer(player) && PlayerStats.hasNoPendingLogin(player.getUniqueId())) {
            ItemStack itemstack = event.getItemDrop().getItemStack();
            int amount = itemstack.getAmount();
            Material type = itemstack.getType();

            Statistics stats = PlayerStats.group.getStatistics(player.getName());
            stats.add(Statistic.totalItemsDropped, amount);
            stats.add(Statistic.itemsDropped, Utils.titleCase(type.name()), amount);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        if (PlayerStats.isStatsPlayer(player) && PlayerStats.hasNoPendingLogin(player.getUniqueId())) {
            String pName = player.getName();
            ItemStack itemstack = event.getItem().getItemStack();
            int amount = itemstack.getAmount();
            Material type = itemstack.getType();

            Statistics stats = PlayerStats.group.getStatistics(pName);
            stats.add(Statistic.totalItemsPickedUp, amount);
            stats.add(Statistic.itemsPickedUp, Utils.titleCase(type.name()), amount);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerPortal(PlayerPortalEvent event) {
        Player player = event.getPlayer();
        if (PlayerStats.isStatsPlayer(player) && PlayerStats.hasNoPendingLogin(player.getUniqueId())) {
            String pName = player.getName();

            PlayerStats.group.getStatistics(pName).incr(Statistic.portalsCrossed);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        if (PlayerStats.isStatsPlayer(player) && PlayerStats.hasNoPendingLogin(event.getPlayer().getUniqueId())) {
            String pName = player.getName();
            Material itemTypeInHand = player.getItemInHand().getType();
            Entity interactedWith = event.getRightClicked();//TODO: Not thread safe variable

            Statistics stats = PlayerStats.group.getStatistics(pName);
            switch (itemTypeInHand) {
                case BUCKET:
                    if (event.getRightClicked() instanceof Cow) {
                        stats.incr(Statistic.cowsMilked);
                    }
                    break;
                case BOWL:
                    if (event.getRightClicked() instanceof MushroomCow) {
                        stats.incr(Statistic.mooshroomsMilked);
                    }
                    break;
                case INK_SACK:
                    if (event.getRightClicked() instanceof Sheep) {
                        stats.incr(Statistic.sheepDyed);
                    }
                    break;
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerExp(PlayerExpChangeEvent event) {
        Player player = event.getPlayer();
        if (PlayerStats.isStatsPlayer(player) && PlayerStats.hasNoPendingLogin(player.getUniqueId())) {
            String pName = player.getName();
            int amount = event.getAmount();

            PlayerStats.group.getStatistics(pName).add(Statistic.lifetimeExperience, amount);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEnchant(EnchantItemEvent event) {
        Player player = event.getEnchanter();
        if (PlayerStats.isStatsPlayer(player) && PlayerStats.hasNoPendingLogin(player.getUniqueId())) {
            String pName = player.getName();
            int amount = event.getExpLevelCost();

            PlayerStats.group.getStatistics(pName).incr(Statistic.itemsEnchanted);
            PlayerStats.group.getStatistics(pName).add(Statistic.itemEnchantmentLevels, amount);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerEnterBed(PlayerBedEnterEvent event) {
        Player player = event.getPlayer();
        if (PlayerStats.isStatsPlayer(player) && PlayerStats.hasNoPendingLogin(player.getUniqueId())) {

            PlayerStats.onPlayerEnterBed(player);//Needs work
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerShearEntity(PlayerShearEntityEvent event) {
        Player player = event.getPlayer();
        if (PlayerStats.isStatsPlayer(player) && PlayerStats.hasNoPendingLogin(player.getUniqueId())) {
            String pName = player.getName();
            switch (event.getEntity().getType()) {//needs work
                case SHEEP:
                    PlayerStats.group.getStatistics(pName).incr(Statistic.sheepSheared);
                    break;
                case MUSHROOM_COW:
                    PlayerStats.group.getStatistics(pName).incr(Statistic.mooshroomsSheared);
                    break;
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerFishEvent(PlayerFishEvent event) {
        Player player = event.getPlayer();
        if (event.getState() == PlayerFishEvent.State.CAUGHT_FISH) {
            if (PlayerStats.isStatsPlayer(player) && PlayerStats.hasNoPendingLogin(player.getUniqueId())) {
                String pName = player.getName();

                PlayerStats.group.getStatistics(pName).incr(Statistic.fishCaught);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerEggThrow(PlayerEggThrowEvent event) {
        Player player = event.getPlayer();
        if (PlayerStats.isStatsPlayer(player) && PlayerStats.hasNoPendingLogin(player.getUniqueId())) {
            EntityType hatchingType = event.getHatchingType();
            String pName = player.getName();

            PlayerStats.group.getStatistics(pName).incr(Statistic.eggsThrown, Utils.normalizeEntityTypeName(hatchingType));
        }
    }

    //TODO: Some work here is needed
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerBucketFill(PlayerBucketFillEvent event) {//needs work
        Player player = event.getPlayer();
        if (PlayerStats.isStatsPlayer(player) && PlayerStats.hasNoPendingLogin(player.getUniqueId())) {
            Material bucket = event.getBucket();
            switch (bucket) {
                case BUCKET:
                    switch (event.getBlockClicked().getType()) {
                        case WATER:
                        case STATIONARY_WATER:
                            PlayerStats.group.getStatistics(player.getName()).incr(Statistic.waterBucketsFilled);
                            break;
                        case LAVA:
                        case STATIONARY_LAVA:
                            PlayerStats.group.getStatistics(player.getName()).incr(Statistic.lavaBucketsFilled);
                            break;
                    }
                    break;
                case WATER_BUCKET:
                    PlayerStats.group.getStatistics(event.getPlayer().getName()).incr(Statistic.waterBucketsFilled);
                    break;
                case LAVA_BUCKET:
                    PlayerStats.group.getStatistics(event.getPlayer().getName()).incr(Statistic.lavaBucketsFilled);
                    break;
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {//needs work
        Player player = event.getPlayer();
        if (PlayerStats.isStatsPlayer(player) && PlayerStats.hasNoPendingLogin(player.getUniqueId())) {
            Material bucket = event.getBucket();
            switch (bucket) {
                case WATER_BUCKET:
                    PlayerStats.group.getStatistics(player.getName()).incr(Statistic.waterBucketsEmptied);
                    break;
                case LAVA_BUCKET:
                    PlayerStats.group.getStatistics(player.getName()).incr(Statistic.lavaBucketsEmptied);
                    break;
            }
        }
    }

    // not really a player event, but an inventory event
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCraftItem(CraftItemEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            Player player = (Player) event.getWhoClicked();
            if (PlayerStats.isStatsPlayer(player) && PlayerStats.hasNoPendingLogin(player.getUniqueId())) {
                String pName = player.getName();
                int amount = event.getRecipe().getResult().getAmount();
                Material material = event.getRecipe().getResult().getType();

                Statistics stats = PlayerStats.group.getStatistics(pName);
                stats.add(Statistic.totalItemsCrafted, amount);
                stats.add(Statistic.itemsCrafted, Utils.titleCase(material.name()), amount);
            }
        }
    }

}
