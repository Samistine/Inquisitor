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
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;

/**
 * Handles Spigot's BlockEvents
 *
 * @see PlayerListenerImpl
 * @see EntityListenerImpl
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */
public final class BlockListenerImpl implements Listener {

    /**
     * Records blocks broken by players.
     * <br>
     * If the broken block is a bed then a bed check will be
     * performed/scheduled.
     *
     * @param event BlockBreakEvent
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        PlayerSnapshot player = new PlayerSnapshot(event.getPlayer());
        if (PlayerStats.isStatsPlayer(player)) {
            Material type = event.getBlock().getType();
            PlayerStats.submitChange(() -> {
                Utils.debug("onPlayerBreakBlock '%s'", player);
                Statistics stats = PlayerStats.group.getStatistics(player);
                stats.incr(Statistic.totalBlocksBroken);
                stats.incr(Statistic.blocksBroken, Utils.titleCase(type.name()));
            });
        }
        if (event.getBlock().getType() == Material.BED_BLOCK) {
            PlayerStats.checkBeds();
        }
    }

    /**
     * Records blocks placed by players.
     *
     * @param event BlockPlaceEvent
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        PlayerSnapshot player = new PlayerSnapshot(event.getPlayer());
        if (PlayerStats.isStatsPlayer(player)) {
            Material type = event.getBlock().getType();
            PlayerStats.submitChange(() -> {
                Utils.debug("onPlayerPlaceBlock '%s'", player);
                Statistics stats = PlayerStats.group.getStatistics(player);
                stats.incr(Statistic.totalBlocksPlaced);
                stats.incr(Statistic.blocksPlaced, Utils.titleCase(type.name()));
            });
        }
    }

    /**
     * Handles internal bed checking on block burn.
     * <br>
     * If the burning block is a bed then a bed check will be
     * performed/scheduled.
     *
     * @param event BlockBurnEvent
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent event) {
        if (event.getBlock().getType() == Material.BED_BLOCK) {
            PlayerStats.checkBeds();
        }
    }

    /**
     * Records fire starts for players.
     *
     * @param event BlockIgniteEvent
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockIgnite(BlockIgniteEvent event) {
        PlayerSnapshot player = new PlayerSnapshot(event.getPlayer());
        if (PlayerStats.isStatsPlayer(player)) {
            PlayerStats.submitChange(() -> {
                Utils.debug("onPlayerStartFire '%s'", player);
                Statistics stats = PlayerStats.group.getStatistics(player);
                stats.incr(Statistic.firesStarted);
            });
        }
    }

}
