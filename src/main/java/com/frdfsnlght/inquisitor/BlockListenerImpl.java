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
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;

/**
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */
public class BlockListenerImpl implements Listener {

    // broken by player only
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (PlayerStats.isStatsPlayer(player)) {
            Material type = event.getBlock().getType();
                String pName = player.getName();

                Statistics stats = PlayerStats.group.getStatistics(pName);
                stats.incr(Statistic.totalBlocksBroken);
                stats.incr(Statistic.blocksBroken, Utils.titleCase(type.name()));
            if (type == Material.BED_BLOCK) {
                PlayerStats.checkBeds();
            }
        }
    }

    // placed by player only
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (PlayerStats.isStatsPlayer(player)) {
            String pName = player.getName();
            Material type = event.getBlock().getType();

            Statistics stats = PlayerStats.group.getStatistics(pName);
            stats.incr(Statistic.totalBlocksPlaced);
            stats.incr(Statistic.blocksPlaced, Utils.titleCase(type.name()));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent event) {
        if (event.getBlock().getType() == Material.BED_BLOCK) {
            PlayerStats.checkBeds();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockIgnite(BlockIgniteEvent event) {
        Player player = event.getPlayer();
        if (PlayerStats.isStatsPlayer(player)) {
            String pName = player.getName();

            Statistics stats = PlayerStats.group.getStatistics(pName);
            stats.incr(Statistic.firesStarted);
        }
    }

}
