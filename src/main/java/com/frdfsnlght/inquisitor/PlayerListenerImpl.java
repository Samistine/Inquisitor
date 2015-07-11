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
import org.bukkit.Material;
import org.bukkit.entity.Cow;
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
        if (PlayerStats.hasNoPendingLogin(player.getUniqueId())) {
            PlayerStats.onPlayerJoin(player);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (PlayerStats.hasNoPendingLogin(player.getUniqueId())) {
            PlayerStats.onPlayerQuit(player);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerKick(PlayerKickEvent event) {
        Player player = event.getPlayer();
        String message = ChatColor.stripColor(event.getLeaveMessage());
        if (PlayerStats.hasNoPendingLogin(player.getUniqueId())) {
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
        if (PlayerStats.hasNoPendingLogin(player.getUniqueId())) {
            PlayerStats.onPlayerDeath(player, message, damageEvent.getCause());
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
            if (PlayerStats.hasNoPendingLogin(player.getUniqueId())) {
                PlayerStats.onPlayerMove(player, event.getTo());
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        if (PlayerStats.hasNoPendingLogin(player.getUniqueId())) {
            PlayerStats.onPlayerTeleport(player, event.getTo());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerChatAsync(final AsyncPlayerChatEvent event) {
        if (!PlayerStats.isStatsPlayer(event.getPlayer())) {
            return;
        }
        if (event.isAsynchronous()) {
            Utils.fire(new Runnable() {
                public void run() {
                    if (PlayerStats.isStatsPlayer(event.getPlayer()) && PlayerStats.hasNoPendingLogin(event.getPlayer().getUniqueId())) {
                        PlayerStats.group.getStatistics(event.getPlayer().getName()).incr("chatMessages");
                    }
                }
            });
        } else {
            if (PlayerStats.isStatsPlayer(event.getPlayer()) && PlayerStats.hasNoPendingLogin(event.getPlayer().getUniqueId())) {
                PlayerStats.group.getStatistics(event.getPlayer().getName()).incr("chatMessages");
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (PlayerStats.isStatsPlayer(player) && PlayerStats.hasNoPendingLogin(player.getUniqueId())) {
            Statistics stats = PlayerStats.group.getStatistics(player.getName());
            int amount = event.getItemDrop().getItemStack().getAmount();
            stats.add("totalItemsDropped", amount);
            stats.add(
                    "itemsDropped",
                    Utils.titleCase(event.getItemDrop().getItemStack().getType()
                            .name()), amount);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        if (PlayerStats.isStatsPlayer(event.getPlayer()) && PlayerStats.hasNoPendingLogin(event.getPlayer().getUniqueId())) {
            Statistics stats = PlayerStats.group.getStatistics(event.getPlayer().getName());
            ItemStack itemstack = event.getItem().getItemStack();
            stats.add("totalItemsPickedUp", itemstack.getAmount());
            stats.add("itemsPickedUp", Utils.titleCase(itemstack.getType().name()), itemstack.getAmount());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerPortal(PlayerPortalEvent event) {
        if (PlayerStats.isStatsPlayer(event.getPlayer()) && PlayerStats.hasNoPendingLogin(event.getPlayer().getUniqueId())) {
            PlayerStats.group.getStatistics(event.getPlayer().getName()).incr("portalsCrossed");
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        if (PlayerStats.isStatsPlayer(player) && PlayerStats.hasNoPendingLogin(event.getPlayer().getUniqueId())) {
            switch (player.getItemInHand().getType()) {
                case BUCKET:
                    if (event.getRightClicked() instanceof Cow) {
                        PlayerStats.group.getStatistics(player.getName())
                                .incr("cowsMilked");
                    }
                    break;
                case BOWL:
                    if (event.getRightClicked() instanceof MushroomCow) {
                        PlayerStats.group.getStatistics(player.getName())
                                .incr("mooshroomsMilked");
                    }
                    break;
                case INK_SACK:
                    if (event.getRightClicked() instanceof Sheep) {
                        PlayerStats.group.getStatistics(player.getName())
                                .incr("sheepDyed");
                    }
                    break;
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerExp(PlayerExpChangeEvent event) {
        Player player = event.getPlayer();
        if (PlayerStats.isStatsPlayer(player) && PlayerStats.hasNoPendingLogin(player.getUniqueId())) {
            PlayerStats.group.getStatistics(player.getName()).add("lifetimeExperience", event.getAmount());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEnchant(EnchantItemEvent event) {
        Player player = event.getEnchanter();
        if (PlayerStats.isStatsPlayer(player) && PlayerStats.hasNoPendingLogin(player.getUniqueId())) {
            PlayerStats.group.getStatistics(player.getName()).incr("itemsEnchanted");
            PlayerStats.group.getStatistics(player.getName()).add("itemEnchantmentLevels", event.getExpLevelCost());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerEnterBed(PlayerBedEnterEvent event) {
        if (PlayerStats.isStatsPlayer(event.getPlayer()) && PlayerStats.hasNoPendingLogin(event.getPlayer().getUniqueId())) {
            PlayerStats.onPlayerEnterBed(event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerShearEntity(PlayerShearEntityEvent event) {
        Player player = event.getPlayer();
        if (PlayerStats.isStatsPlayer(player) && PlayerStats.hasNoPendingLogin(player.getUniqueId())) {
            switch (event.getEntity().getType()) {
                case SHEEP:
                    PlayerStats.group.getStatistics(player.getName()).incr("sheepSheared");
                    break;
                case MUSHROOM_COW:
                    PlayerStats.group.getStatistics(player.getName()).incr("mooshroomSheared");
                    break;
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerFishEvent(PlayerFishEvent event) {
        Player player = event.getPlayer();
        if (event.getState() == PlayerFishEvent.State.CAUGHT_FISH) {
            if (PlayerStats.isStatsPlayer(player) && PlayerStats.hasNoPendingLogin(player.getUniqueId())) {
                PlayerStats.group.getStatistics(player.getName()).incr("fishCaught");
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerEggThrow(PlayerEggThrowEvent event) {
        Player player = event.getPlayer();
        if (PlayerStats.isStatsPlayer(player) && PlayerStats.hasNoPendingLogin(player.getUniqueId())) {
            PlayerStats.group.getStatistics(player.getName()).incr("eggsThrown", Utils.normalizeEntityTypeName(event.getHatchingType()));
        }
    }

    //TODO: Some work here is needed
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerBucketFill(PlayerBucketFillEvent event) {
        Player player = event.getPlayer();
        if (PlayerStats.isStatsPlayer(player) && PlayerStats.hasNoPendingLogin(player.getUniqueId())) {
            Material bucket = event.getBucket();
            switch (bucket) {
                case BUCKET:
                    switch (event.getBlockClicked().getType()) {
                        case WATER:
                        case STATIONARY_WATER:
                            PlayerStats.group.getStatistics(player.getName()).incr("waterBucketsFilled");
                            break;
                        case LAVA:
                        case STATIONARY_LAVA:
                            PlayerStats.group.getStatistics(player.getName()).incr("lavaBucketsFilled");
                            break;
                    }
                    break;
                case WATER_BUCKET:
                    PlayerStats.group.getStatistics(event.getPlayer().getName()).incr("waterBucketsFilled");
                    break;
                case LAVA_BUCKET:
                    PlayerStats.group.getStatistics(event.getPlayer().getName()).incr("lavaBucketsFilled");
                    break;
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
        Player player = event.getPlayer();
        if (PlayerStats.isStatsPlayer(player) && PlayerStats.hasNoPendingLogin(player.getUniqueId())) {
            Material bucket = event.getBucket();
            switch (bucket) {
                case WATER_BUCKET:
                    PlayerStats.group.getStatistics(player.getName()).incr("waterBucketsEmptied");
                    break;
                case LAVA_BUCKET:
                    PlayerStats.group.getStatistics(player.getName()).incr("lavaBucketsEmptied");
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
                Statistics stats = PlayerStats.group.getStatistics(player.getName());
                int amount = event.getRecipe().getResult().getAmount();
                stats.add("totalItemsCrafted", amount);
                stats.add("itemsCrafted", Utils.titleCase(event.getRecipe().getResult().getType().name()), amount);
            }
        }
    }

}
