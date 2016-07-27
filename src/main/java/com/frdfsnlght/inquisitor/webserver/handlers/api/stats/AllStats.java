/*
 * The MIT License
 *
 * Copyright 2016 Samuel Seidel.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.frdfsnlght.inquisitor.webserver.handlers.api.stats;

import com.frdfsnlght.inquisitor.DB;
import com.frdfsnlght.inquisitor.TypeMap;
import com.frdfsnlght.inquisitor.Utils;
import com.frdfsnlght.inquisitor.webserver.handlers.api.APIHandler;
import com.frdfsnlght.inquisitor.webserver.WebRequest;
import com.frdfsnlght.inquisitor.webserver.WebResponse;
import com.frdfsnlght.inquisitor.webserver.WebServer;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

/**
 *
 * @author Samistine <samuel@samistine.com>
 */
public final class AllStats extends APIHandler {

    @Override
    public void handleRequest(WebRequest req, WebResponse res) throws IOException {
        String playerName = req.getParameter("playerName", null);
        System.out.println("Requesting stats from " + playerName);

        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            Connection conn = DB.connect();
            StringBuilder sql = new StringBuilder();

            sql.append("SELECT * FROM ").append(DB.tableName("players"));
            sql.append(" WHERE name =?");
            //sql.append(" ORDER BY NAME ASC");
            stmt = conn.prepareStatement(sql.toString());
            //stmt.setString(1, "%" + playerName + "%");
            stmt.setString(1, playerName);

            rs = stmt.executeQuery();
            //JSONArray names = new JSONArray();
            JSONObject json = new JSONObject();
            //while (rs.next()) {//Not needed since we only *should* have one row
            if (rs.next() == true) {
                TypeMap mainMap = new TypeMap();
                List<String> restrictedColumns = WebServer.getPlayersRestrictColumnsAsList();
                TypeMap statsMap = playerStats(rs);

                //Respect disabled columns in our api response
                for (String str : restrictedColumns) {
                    statsMap.remove(str);
                    Utils.debug("Removing column " + str + " from the API response");
                }
                mainMap.set("stats", statsMap);
                json.putAll(mainMap);
                Utils.debug("Sending response to API request", json);
                sendSuccess(req, res, json);
            } else if (json.isEmpty()) {
                sendFailure(req, res, "Samistine, Something went wrong **json is empty**");
            }
        } catch (SQLException se) {
            res.setStatus(500, "Internal Server Error");
            Utils.severe("SQLException while finding players: %s", se.getMessage());
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException se) {
            }
        }
    }

    private TypeMap playerStats(ResultSet rs) throws SQLException {
        try {
            return new ResultSetConverter(rs).getAllStats();
        } catch (ParseException ex) {
            Logger.getLogger(AllStats.class.getName()).log(Level.SEVERE, null, ex);
            return new TypeMap();
        }
    }

    /*private TypeMap playerJoinQuits(ResultSet rs) throws SQLException {
     TypeMap joinQuits = new TypeMap();
     joinQuits.set("joins", rs.getInt("joins"));
     joinQuits.set("quits", rs.getInt("quits"));
     joinQuits.set("firstJoin", String.valueOf(rs.getTimestamp("firstJoin")));
     joinQuits.set("lastJoin", String.valueOf(rs.getTimestamp("lastJoin")));
     joinQuits.set("lastQuit", String.valueOf(rs.getTimestamp("lastQuit")));
     return joinQuits;
     }

     private TypeMap playerLocationMap(ResultSet rs) throws SQLException {
     TypeMap playerLocation = new TypeMap();
     playerLocation.set("server", rs.getString("server")); //for those of you using one database for all your servers ^.^
     playerLocation.set("world", rs.getString("world"));
     playerLocation.set("coords", rs.getString("coords"));
     return playerLocation;
     }

     private TypeMap playerInventories(ResultSet rs) throws SQLException {
     TypeMap playerStatusInventory = new TypeMap();
     playerStatusInventory.set("heldItemSlot", rs.getInt("heldItemSlot"));
     playerStatusInventory.set("inventory", JSON.decode(rs.getObject("inventory").toString()));
     playerStatusInventory.set("armor", JSON.decode(rs.getObject("armor").toString())); //the actual inventory ^.^
     playerStatusInventory.set("ender", JSON.decode(rs.getObject("ender").toString()));
     return playerStatusInventory;
     }

     private TypeMap playerStatus(ResultSet rs) throws SQLException {
     TypeMap playerStatus = new TypeMap();
     playerStatus.set("health", rs.getInt("health"));
     playerStatus.set("gameMode", rs.getString("gameMode"));

     playerStatus.set("level", rs.getInt("level"));
     playerStatus.set("exp", rs.getFloat("exp"));

     playerStatus.set("remainingAir", rs.getInt("remainingAir"));
     playerStatus.set("fireTicks", rs.getInt("fireTicks")); //Not sure what this is?

     playerStatus.set("foodLevel", rs.getInt("foodLevel"));
     playerStatus.set("exhaustion", rs.getFloat("exhaustion"));
     playerStatus.set("saturation", rs.getFloat("saturation"));
     playerStatus.set("potionEffects", rs.getObject("potionEffects"));
     return playerStatus;
     }

     private TypeMap playerProfile(ResultSet rs) throws SQLException {
     TypeMap profile = new TypeMap();
     profile.set("uuid", rs.getString("uuid"));
     profile.set("displayName", rs.getString("displayName"));
     profile.set("groups", JSON.decode(rs.getObject("groups").toString()));

     int online = rs.getInt("online");
     boolean onlineBool = false;
     switch (online) {
     case 0:
     onlineBool = false;
     break;
     case 1:
     onlineBool = true;
     break;
     }
     profile.set("online", onlineBool);
     profile.set("sessionTime", rs.getFloat("sessionTime"));
     return profile;
     }*/
}
