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

import com.frdfsnlght.inquisitor.DB;
import com.frdfsnlght.inquisitor.Utils;
import com.frdfsnlght.inquisitor.webserver.WebRequest;
import com.frdfsnlght.inquisitor.webserver.WebResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */


public final class FindPlayersHandler extends APIHandler {

    @Override
    public void handleRequest(WebRequest req, WebResponse res) throws IOException {
        String playerName = req.getParameter("playerName", null);
        if (playerName != null) playerName = playerName.trim();
        if ((playerName == null) || playerName.isEmpty()) {
            sendFailure(req, res, "playerName required");
            return;
        }

        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            Connection conn = DB.connect();
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT name FROM ").append(DB.tableName("players"));
            sql.append(" WHERE name LIKE ?");
            sql.append(" ORDER BY NAME ASC");
            stmt = conn.prepareStatement(sql.toString());
            stmt.setString(1, "%" + playerName + "%");
            rs = stmt.executeQuery();
            List<String> names = new ArrayList<String>();
            while (rs.next())
                names.add(rs.getString("name"));
            sendSuccess(req, res, names);
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

}
