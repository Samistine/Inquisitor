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
import com.frdfsnlght.inquisitor.JSON;
import com.frdfsnlght.inquisitor.TypeMap;
import com.frdfsnlght.inquisitor.Utils;
import com.frdfsnlght.inquisitor.webserver.handlers.api.APIHandler;
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
 * @author Samuel Seidel
 */
public class Groups extends APIHandler {

    @Override
    public void handleRequest(WebRequest req, WebResponse res) throws IOException {
        String playerName = req.getParameter("playerName", null);
        if (playerName != null) {
            playerName = playerName.trim();
        }
        if ((playerName == null) || playerName.isEmpty()) {
            sendFailure(req, res, "playerName required");
            return;
        }

        String[] players = playerName.split("\\+");

        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            Connection conn = DB.connect();
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT name, groups FROM ").append(DB.tableName("players"));
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < players.length; i++) {
                builder.append("?,");
            }
            String str = builder.deleteCharAt(builder.length() - 1).toString();
            sql.append(" WHERE name in (" + str + ")");
            sql.append(" ORDER BY NAME ASC");
            System.out.println(sql.toString());
            stmt = conn.prepareStatement(sql.toString());
            for (int i = 0; i < players.length; i++) {
                stmt.setString(i + 1, players[i]);
            }
            System.out.println(stmt.toString());
            rs = stmt.executeQuery();
            List<TypeMap> names = new ArrayList<>();
            while (rs.next()) {
                names.add(playerProfile(rs));
            }
            System.out.println(names);
            sendSuccess(req, res, names);
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

    private TypeMap playerProfile(ResultSet rs) throws SQLException {
        TypeMap profile = new TypeMap();
        profile.set("name", rs.getString("name"));
        profile.set("groups",  JSON.decode(rs.getObject("groups").toString()));
        return profile;
    }

}
