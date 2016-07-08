package com.frdfsnlght.inquisitor;

import static com.frdfsnlght.inquisitor.PlayerStats.group;
import java.util.Collection;
import java.util.Iterator;
import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;

/**
 *
 * @author Samuel Seidel
 */
public final class BedCheck implements Runnable {

    private final Collection<UUID> bedOwners;

    public BedCheck(Collection<UUID> bedOwners) {
        this.bedOwners = bedOwners;
    }

    @Override
    public void run() {
        Iterator<UUID> iterator = bedOwners.iterator();
        while (iterator.hasNext()) {
            UUID uuid = iterator.next();
            OfflinePlayer player = Global.plugin.getServer().getOfflinePlayer(uuid);
            if (player == null) {
                player = Global.plugin.getServer().getPlayer(uuid);
                if (player == null) {
                    continue;
                }
            }
            if (!player.hasPlayedBefore()) {
                continue;
            }
            if (player.getBedSpawnLocation() == null || player.getBedSpawnLocation().getBlock().getType() != Material.BED_BLOCK) {
                iterator.remove();
                Utils.debug("player '%s' no longer has a bed", uuid);

                Statistics stats = group.getStatistics(player.getName());
                stats.set(Statistic.bedServer, null);
                stats.set(Statistic.bedWorld, null);
                stats.set(Statistic.bedCoords, null);
                stats.flush();
                if (!player.isOnline()) {
                    group.removeStatistics(stats);
                }
            }
        }
    }
}
