package com.frdfsnlght.inquisitor;

import static com.frdfsnlght.inquisitor.PlayerStats.group;
import java.util.Collection;
import java.util.Iterator;
import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;

/**
 * Checks for destroyed beds. If a player's bed, <b>on the server this is called
 * on</b>, is destroyed then their <code>bedServer</code>,
 * <code>bedWorld</code>, and <code>bedCoords</code> are removed.
 *
 * @author Samuel Seidel
 */
public final class BedCheck implements Runnable {

    private final Server server;
    private final Collection<UUID> bedOwners;

    /**
     *
     * @param server server to check
     * @param bedOwners players to check
     */
    public BedCheck(Server server, Collection<UUID> bedOwners) {
        this.server = server;
        this.bedOwners = bedOwners;
    }

    @Override
    public void run() {
        Iterator<UUID> iterator = bedOwners.iterator();
        while (iterator.hasNext()) {
            UUID uuid = iterator.next();
            OfflinePlayer player = server.getOfflinePlayer(uuid);
            if (player == null) {//Needed?
                player = server.getPlayer(uuid);
                if (player == null) {
                    continue;
                }
            }
            if (!player.hasPlayedBefore()) {
                continue;
            }
            if (player.getBedSpawnLocation() == null || player.getBedSpawnLocation().getBlock().getType() != Material.BED_BLOCK) {
                iterator.remove();

                final InquisitorPlayer player_ = new InquisitorPlayer.InquisitorPlayerImpl(player.getUniqueId(), player.getName());

                PlayerStats.submitChange(() -> {
                    Utils.debug("player '%s' no longer has a bed", player_.getUUID());
                    Statistics stats = group.getStatistics(player_);
                    stats.set(Statistic.bedServer, null);
                    stats.set(Statistic.bedWorld, null);
                    stats.set(Statistic.bedCoords, null);
                    stats.flush();
                    if (!player_.isOnline()) {
                        group.removeStatistics(stats);
                    }
                });
            }
        }
    }
}
