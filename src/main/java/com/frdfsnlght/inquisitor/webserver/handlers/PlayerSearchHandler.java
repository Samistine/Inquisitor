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
import com.frdfsnlght.inquisitor.TypeMap;
import com.frdfsnlght.inquisitor.Utils;
import com.frdfsnlght.inquisitor.webserver.WebRequest;
import com.frdfsnlght.inquisitor.webserver.WebResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */
public final class PlayerSearchHandler extends TemplateHandler {

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

        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            Connection conn = DB.connect();
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT name FROM ").append(DB.tableName("players"));
            sql.append(" WHERE name LIKE ?");
            Utils.debug(sql.toString());
            stmt = conn.prepareStatement(sql.toString());
            stmt.setString(1, "%" + playerName + "%");
            rs = stmt.executeQuery();
            if (! rs.next()) {
                renderTemplate(req, res, "resources/playerNotFound.ftl");
                return;
            }
            String name = rs.getString("name");
            if (rs.next()) {
                // multiple rows
                res.redirect("/players/?playerName=" + URLEncoder.encode(playerName, "US-ASCII"));
            } else {
                // single row
                res.redirect("/player/" + URLEncoder.encode(name, "US-ASCII"));
            }
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
