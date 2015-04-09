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
package com.frdfsnlght.inquisitor.handlers.api;

import com.avaje.ebean.text.json.JsonElementObject;
import com.frdfsnlght.inquisitor.DB;
import com.frdfsnlght.inquisitor.JSON;
import com.frdfsnlght.inquisitor.TypeMap;
import com.frdfsnlght.inquisitor.Utils;
import com.frdfsnlght.inquisitor.WebRequest;
import com.frdfsnlght.inquisitor.WebResponse;

import java.io.IOException;
import java.security.acl.LastOwnerException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */


public final class JsonStatsHandler extends APIHandler {

    @Override
    public void handleRequest(WebRequest req, WebResponse res) throws IOException {
    	String playerName = req.getParameter("playerName", null);
    	System.out.println(playerName);
    	System.out.println("did");

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
            	case 0: onlineBool = false;
            			break;
            	case 1: onlineBool = true;
            			break;
            	}
            	mainMap.set("online", onlineBool);
            	mainMap.set("sessiontime", rs.getFloat("sessionTime"));
            	
            	
            	
            	TypeMap joinQuits = new TypeMap();
            	joinQuits.set("joins", rs.getInt("joins"));
            	joinQuits.set("quits", rs.getInt("quits"));
            	joinQuits.set("firstjoin", String.valueOf(rs.getTimestamp("firstJoin")));
            	joinQuits.set("lastjoin", String.valueOf(rs.getTimestamp("lastJoin")));
            	joinQuits.set("lastquit", String.valueOf(rs.getTimestamp("lastQuit")));
            	
            	
            	mainMap.set("joinquits", joinQuits);
            	json.putAll(mainMap);
                System.out.println(json);
                sendSuccess(req, res, json);
            } else if (json.isEmpty()){
            	sendFailure(req, res, "Samistine, Something went wrong **json is empty**");
            }
        } catch (SQLException se) {
            res.setStatus(500, "Internal Server Error");
            Utils.severe("SQLException while finding players: %s", se.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException se) {}
        }
    }
    private class Player {
    	private String name;
    	private String uuid;
    	private int joins;
		public Player(String name, String uuid, int joins) {
			this.name = name;
			this.uuid = uuid;
			this.joins = joins;
		}
    }
}
