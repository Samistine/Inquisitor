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
package com.frdfsnlght.inquisitor.handlers;

import com.frdfsnlght.inquisitor.DB;
import com.frdfsnlght.inquisitor.PlayerStats;
import com.frdfsnlght.inquisitor.Statistic;
import com.frdfsnlght.inquisitor.TypeMap;
import com.frdfsnlght.inquisitor.Utils;
import com.frdfsnlght.inquisitor.webserver.WebRequest;
import com.frdfsnlght.inquisitor.webserver.WebResponse;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;

/**
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */


public final class PlayerHandler extends TemplateHandler {

    @Override
    public void handleRequest(WebRequest req, WebResponse res) throws IOException {
        String playerName = req.getParameter("playerName", null);
        if (playerName == null) {
            renderTemplate(req, res, "resources/playerNotFound.ftl");
            return;
        }
        TypeMap data = new TypeMap();
        playerName = playerName.trim();
        data.set("playerName", playerName);

        TypeMap player = getPlayer(playerName);
        if (player == null) {
            res.setStatus(404, "Not found");
            renderTemplate(req, res, "resources/playerNotFound.ftl", data);
            return;
        }
        data.set("player", player);
        renderTemplate(req, res, "resources/player.ftl", data);
    }

    private TypeMap getPlayer(String name) {
        Set<Statistic> stats = PlayerStats.group.getStatistics();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT `").append(Statistic.MappedObjectsColumn).append('`');
            for (Statistic stat : stats) {
                if (stat.isMapped()) continue;
                sql.append(",`").append(stat.getName()).append('`');
            }
            sql.append(" FROM ").append(DB.tableName(PlayerStats.group.getName()));
            sql.append(" WHERE `name`=?");
            stmt = DB.prepare(sql.toString());
            stmt.setString(1, name);
            rs = stmt.executeQuery();
            if (! rs.next()) return null;
            TypeMap player = PlayerStats.group.loadStatistics(rs, stats);
            if (player == null) return null;
            player.set("name", name);
            return player;
        } catch (SQLException se) {
            Utils.severe("SQLException while selecting player '%s': %s", name, se.getMessage());
            return null;
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException se) {}
        }
    }

}
