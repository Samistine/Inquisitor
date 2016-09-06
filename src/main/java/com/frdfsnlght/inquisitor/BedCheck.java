package com.frdfsnlght.inquisitor;

import java.util.ArrayList;
import java.util.Collection;
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
    private final Collection<UUID> bedOwners;//Synchronized Set
    private final ArrayList<UUID> bedOwnersSnapshot;

    /**
     *
     * @param server server to check
     * @param bedOwners synchronized set of players to check
     */
    public BedCheck(Server server, Collection<UUID> bedOwners) {
        this.server = server;
        this.bedOwners = bedOwners;
        synchronized (bedOwners) {
            bedOwnersSnapshot = new ArrayList(bedOwners);
        }
    }

    @Override
    public void run() {
        for (UUID uuid : bedOwnersSnapshot) {
            OfflinePlayer offlinePlayer = server.getOfflinePlayer(uuid);

            if (!offlinePlayer.isOnline() && !offlinePlayer.hasPlayedBefore()) {
                continue;
            }

            if (offlinePlayer.getBedSpawnLocation() == null || offlinePlayer.getBedSpawnLocation().getBlock().getType() != Material.BED_BLOCK) {
                bedOwners.remove(uuid);

                final InquisitorPlayer inqPlayer = new InquisitorPlayer.InquisitorPlayerImpl(offlinePlayer.getUniqueId(), offlinePlayer.getName());

                PlayerStats.submitChange(() -> {
                    Utils.debug("player '%s' no longer has a bed", inqPlayer);
                    Statistics stats = PlayerStats.group.getStatistics(inqPlayer);
                    stats.set(Statistic.bedServer, null);
                    stats.set(Statistic.bedWorld, null);
                    stats.set(Statistic.bedCoords, null);
                    stats.flush();
                    if (!inqPlayer.isOnline()) {
                        PlayerStats.group.removeStatistics(stats);
                    }
                });
            }
        }

    }
}
