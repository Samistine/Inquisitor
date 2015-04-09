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
import com.frdfsnlght.inquisitor.WebRequest;
import com.frdfsnlght.inquisitor.WebResponse;
import com.frdfsnlght.inquisitor.WebServer;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */


public final class PlayersHandler extends TemplateHandler {

    /*
    private static final String[] DEFAULT_COLUMNS = new String[] {
            "level",
            "deaths",
            "totalPlayersKilled",
            "totalMobsKilled",
            "totalBlocksBroken",
            "totalBlocksPlaced",
            "totalItemsDropped",
            "totalItemsPickedUp",
            "totalItemsCrafted",
            "totalDistanceTraveled",
            "totalTime",
            "online"
    };
*/

    /*
    private static final String DEFAULT_SORT_COLUMN = "name";
    private static final String DEFAULT_SORT_DIR = "ASC";
*/

    /*
    private static final String[] DEFAULT_RESTRICT_COLUMNS = new String[] {
            "address",
            "coords",
            "bedServer",
            "bedWorld",
            "bedCoords",
    };
*/

    @Override
    public void handleRequest(WebRequest req, WebResponse res) throws IOException {
        String[] defColumns = WebServer.getPlayersColumns();
        String defSortColumn = WebServer.getPlayersSortColumn();
        String defSortDir = WebServer.getPlayersSortDir();

        String columnsStr = req.getParameter("columns", null);
        String sortColumn = req.getParameter("sortBy", defSortColumn);
        String sortDir = req.getParameter("sortDir", defSortDir).toUpperCase();
        int pageSize = req.getParameters().getInt("pageSize", WebServer.getPlayersPageSize());
        int pageNum = req.getParameters().getInt("page", 1);
        String playerName = req.getParameter("playerName", null);

        Set<String> allColumns = WebServer.getPlayerColumns(false);

        List<String> columns = new ArrayList<String>();
        if (columnsStr == null)
            columns.addAll(Arrays.asList(defColumns));
        else
            for (String column : columnsStr.split("\\s*,\\s*"))
                if (allColumns.contains(column)) columns.add(column);
        
        if (! columns.contains(sortColumn)) sortColumn = WebServer.DEFAULT_SORTCOLUMN;
        if ((! sortDir.equals("ASC")) && (! sortDir.equals("DESC"))) sortDir = WebServer.DEFAULT_SORTDIR;
        if (pageSize < 1) pageSize = 1;
        if (pageNum < 1) pageNum = 1;
        if (playerName != null) {
            playerName = playerName.trim();
            if (playerName.isEmpty()) playerName = null;
        }

        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT COUNT(1) FROM ").append(DB.tableName(PlayerStats.group.getName()));
            if (playerName != null)
                sql.append(" WHERE `name` LIKE ?");
            stmt = DB.prepare(sql.toString());
            if (playerName != null)
                stmt.setString(1, "%" + playerName + "%");
            rs = stmt.executeQuery();
            rs.next();
            int totalPlayers = rs.getInt(1);
            rs.close();
            stmt.close();

            int totalPages = (int)Math.ceil((double)totalPlayers / (double)pageSize);
            if (totalPages < 1) totalPages = 1;
            if (pageNum > totalPages) pageNum = totalPages;
            int limitOffset = (pageNum - 1) * pageSize;

            sql = new StringBuilder();
            sql.append("SELECT `name`");
            for (String column : columns)
                sql.append(',').append(column);
            sql.append(" FROM ").append(DB.tableName(PlayerStats.group.getName()));
            if (playerName != null)
                sql.append(" WHERE `name` LIKE ?");
            sql.append(" ORDER BY `").append(sortColumn).append("` ").append(sortDir);
            if (! "name".equals(sortColumn))
                sql.append(", `name` ASC");
            sql.append(" LIMIT ").append(limitOffset).append(",").append(pageSize);
            stmt = DB.prepare(sql.toString());
            if (playerName != null)
                stmt.setString(1, "%" + playerName + "%");
            rs = stmt.executeQuery();

            List<TypeMap> players = new ArrayList<TypeMap>(pageSize);
            while (rs.next()) {
                TypeMap player = PlayerStats.group.loadStatistics(rs, columns);
                player.set("name", rs.getString("name"));
                players.add(player);
            }

            TypeMap data = new TypeMap();
            data.put("totalPlayers", totalPlayers);
            data.put("totalPages", totalPages);
            data.put("page", pageNum);
            data.put("pageSize", pageSize);
            data.put("showColumns", columns);
            data.put("sortBy", sortColumn);
            data.put("sortDir", sortDir);
            data.put("players", players);
            data.put("firstPlayerOffset", limitOffset);
            data.put("playerName", playerName);

            List<String> hideColumns = new ArrayList<String>();
            for (String col : allColumns)
                if (! columns.contains(col)) hideColumns.add(col);
            data.put("hideColumns", hideColumns);

            renderTemplate(req, res, "resources/players.ftl", data);

        } catch (SQLException se) {
            res.setStatus(500, "Internal Server Error");
            Utils.severe("SQLException while selecting players: %s", se.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException se) {}
        }
    }

}
