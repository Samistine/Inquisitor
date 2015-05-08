package com.frdfsnlght.inquisitor.handlers.api;

import com.frdfsnlght.inquisitor.DB;
import com.frdfsnlght.inquisitor.TypeMap;
import com.frdfsnlght.inquisitor.Utils;
import com.frdfsnlght.inquisitor.webserver.WebRequest;
import com.frdfsnlght.inquisitor.webserver.WebResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.json.simple.JSONObject;

/**
 *
 * @author Samistine <samuel@samistine.com>
 */
public final class JsonStatsHandler extends APIHandler {

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

                mainMap.set("uuid", rs.getString("uuid"));
                mainMap.set("displayname", rs.getString("displayName"));
                mainMap.set("uuid", rs.getString("uuid"));

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
                mainMap.set("online", onlineBool);
                mainMap.set("sessiontime", rs.getFloat("sessionTime"));
                
                    TypeMap playerLocation = new TypeMap();
                    playerLocation.set("server", rs.getString("server")); //for those of you using one database for all your servers ^.^
                    playerLocation.set("world", rs.getString("world"));
                    playerLocation.set("coords", rs.getString("coords"));   
                mainMap.set("playerLocation", playerLocation);
                
                    TypeMap joinQuits = new TypeMap();
                    setJoinQuits(joinQuits, rs); //Populate the list
                mainMap.set("joinquits", joinQuits); //Add this list to the main list
                
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
                
                        TypeMap playerStatusInventory = new TypeMap();
                        playerStatusInventory.set("heldItemSlot", rs.getInt("heldItemSlot"));
                        playerStatusInventory.set("inventory", rs.getObject("inventory")); //the actual inventory ^.^
                        playerStatusInventory.set("armor", rs.getObject("armor"));
                        playerStatusInventory.set("ender", rs.getObject("ender"));
                    playerStatus.set("inventory", playerStatusInventory);
                mainMap.set("playerStatus", playerStatus);
                
                
                json.putAll(mainMap);
                System.out.println(json);
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

/*    private class Player {

        private String name;
        private String uuid;
        private int joins;

        public Player(String name, String uuid, int joins) {
            this.name = name;
            this.uuid = uuid;
            this.joins = joins;
        }
    }*/

    private void setJoinQuits(TypeMap joinQuits, ResultSet rs) throws SQLException {
        joinQuits.set("joins", rs.getInt("joins"));
        joinQuits.set("quits", rs.getInt("quits"));
        joinQuits.set("firstjoin", String.valueOf(rs.getTimestamp("firstJoin")));
        joinQuits.set("lastjoin", String.valueOf(rs.getTimestamp("lastJoin")));
        joinQuits.set("lastquit", String.valueOf(rs.getTimestamp("lastQuit")));
    }
}
