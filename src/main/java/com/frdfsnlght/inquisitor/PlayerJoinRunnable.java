/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.frdfsnlght.inquisitor;

import static com.frdfsnlght.inquisitor.PlayerStats.group;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.PreparedStatement;
import java.util.Date;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author Samuel
 */
public class PlayerJoinRunnable extends BukkitRunnable {

    private final String puuidst;// = player.getUniqueId().toString();
    private final String pname;// = player.getName();
    private final Date date;// = new Date();
    private final String servername;// = Global.plugin.getServer().getServerName();

    public PlayerJoinRunnable(String puuidst, String pname, Date date, String servername) {
        this.puuidst = puuidst;
        this.pname = pname;
        this.date = date;
        this.servername = servername;
    }

    public void run() {
        try {
            //Very simply update statement that will allow players to add their UUIDs to the db during the
            // conversion process, then once a name change happens it will update it based on the uuid matching.
            StringBuilder sql = new StringBuilder();
            sql.append("UPDATE ").append(DB.tableName(group.getName())).append(" SET `");
            sql.append(group.getKeyName()).append("`=?, `uuid`=? WHERE `");
            sql.append(group.getKeyName()).append("`=? OR `uuid`=?");
            PreparedStatement stmt = DB.prepare(sql.toString());
            stmt.setString(1, pname);
            stmt.setString(2, puuidst);
            stmt.setString(3, pname);
            stmt.setString(4, puuidst);
            stmt.execute();

            final Statistics stats = group.getStatistics(pname);
            stats.set("uuid", puuidst);
            stats.incr("joins");
            stats.set("lastJoin", date);
            stats.set("sessionTime", 0);
            stats.set("online", true);
            if (!stats.isInDB()) {
                stats.set("firstJoin", date);
            }
            String bedServer = stats.getString("bedServer");
            if ((bedServer != null) && bedServer.equals(servername)) {
                PlayerStats.bedOwners.add(pname);
            }
            PlayerStats.playerStates.put(pname, new PlayerState(stats.getFloat("totalTime")));

            Utils.debug("totalTime: " + stats.getFloat("totalTime"));

            Global.plugin.getServer().getScheduler().runTaskAsynchronously(Global.plugin, new Runnable() {
                public void run() {
                    stats.flushSync();
                }
            });
        } catch (Exception ex) {
            Utils.severe("OnPlayerJoin Exception message: " + ex.getMessage());
            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            Utils.severe("Stack Trace: " + sw.toString());
        }
    }

}
