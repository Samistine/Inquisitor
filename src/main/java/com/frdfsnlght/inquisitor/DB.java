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
package com.frdfsnlght.inquisitor;

import com.frdfsnlght.inquisitor.exceptions.OptionsException;
import com.frdfsnlght.inquisitor.exceptions.InquisitorException;
import com.frdfsnlght.inquisitor.exceptions.PermissionsException;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.sql.rowset.serial.SerialClob;

/**
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */
public final class DB {

    private static final Set<String> OPTIONS = new HashSet<>();
    private static final Set<String> RESTART_OPTIONS = new HashSet<>();
    private static final Options options;

    private static final Set<DBListener> listeners = new HashSet<>();

    static {
        OPTIONS.add("debug");
        OPTIONS.add("url");
        OPTIONS.add("username");
        OPTIONS.add("password");
        OPTIONS.add("prefix");
        OPTIONS.add("shared");

        RESTART_OPTIONS.add("url");
        RESTART_OPTIONS.add("username");
        RESTART_OPTIONS.add("password");
        RESTART_OPTIONS.add("shared");

        options = new Options(DB.class, OPTIONS, "inq.db",
                new OptionsListener() {
                    @Override
                    public void onOptionSet(Context ctx, String name, String value) {
                        ctx.sendLog("database option '%s' set to '%s'", name, value);
                        if (RESTART_OPTIONS.contains(name)) {
                            Config.save(ctx);
                            stop();
                            start();
                        }
                    }

                    @Override
                    public String getOptionPermission(Context ctx, String name) {
                        return name;
                    }
                });
    }

    private static Connection db = null;
    private static boolean didUpdates = false;

    public static void init() {
    }

    public static void addListener(DBListener listener) {
        listeners.add(listener);
    }

    public static void start() {
        try {
            if ((db != null) && (!db.isClosed())) {
                return;
            }
        } catch (SQLException se) {
        }

        try {
            if (getUrl() == null)      throw new InquisitorException("url is not set");
            if (getUsername() == null) throw new InquisitorException("username is not set");
            if (getPassword() == null) throw new InquisitorException("password is not set");

            connect();

        } catch (Exception e) {
            Utils.warning("database connection cannot be completed: %s", e.getMessage());
        }
    }

    public static void stop() {
        if (db == null) return;
        listeners.forEach(DBListener::onDBDisconnecting);
        try {
            db.close();
        } catch (SQLException se) {
        }
        db = null;
        Utils.info("disconnected from database");
    }

    /* Begin options */

    public static String getUrl() {
        return Config.getStringDirect("db.url", null);
    }

    public static void setUrl(String s) {
        if ((s != null) && (s.equals("-") || s.equals("*"))) {
            s = null;
        }
        Config.setPropertyDirect("db.url", s);
    }

    public static String getUsername() {
        return Config.getStringDirect("db.username", null);
    }

    public static void setUsername(String s) {
        if ((s != null) && (s.equals("-") || s.equals("*"))) {
            s = null;
        }
        Config.setPropertyDirect("db.username", s);
    }

    public static String getPassword() {
        if (getRealPassword() == null) {
            return null;
        }
        return "*******";
    }

    public static String getRealPassword() {
        return Config.getStringDirect("db.password", null);
    }

    public static void setPassword(String s) {
        if ((s != null) && (s.equals("-") || s.equals("*"))) {
            s = null;
        }
        Config.setPropertyDirect("db.password", s);
    }

    public static String getPrefix() {
        return Config.getStringDirect("db.prefix", null);
    }

    public static void setPrefix(String s) {
        if ((s != null) && (s.equals("-") || s.equals("*"))) {
            s = null;
        }
        if (s != null) {
            if (!s.matches("^\\w+$")) {
                throw new IllegalArgumentException("illegal character");
            }
        }
        Config.setPropertyDirect("db.prefix", s);
    }

    public static boolean getShared() {
        return Config.getBooleanDirect("db.shared", true);
    }

    public static void setShared(boolean b) {
        Config.setPropertyDirect("db.shared", b);
    }

    public static Options options() {
        return options;
    }

    /* End options */
    public static String tableName(String baseName) {
        String prefix = getPrefix();
        if (prefix != null) {
            baseName = prefix + baseName;
        }
        return '`' + baseName + '`';
    }

    public static Connection connect() throws SQLException {
        if (!isConnected()) {
            if (db != null) {
                Utils.warning("unexpectedly disconnected from database");
                db = null;
            }
            db = DriverManager.getConnection(getUrl(), getUsername(), getRealPassword());
            db.setAutoCommit(true);
            db.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
            Utils.info("connected to database");
            if (!didUpdates) didUpdates = new Updates().doUpdates();
            listeners.forEach(DBListener::onDBConnected);
        }
        return db;
    }

    public static boolean isConnected() {
        try {
            return (db != null) && (!db.isClosed()) && db.isValid(1);
        } catch (Exception se) { //Changed from SQLException to all exceptions
            se.printStackTrace();
            return false;
        }
    }

    public static PreparedStatement prepare(String sql) throws SQLException {
        Utils.debug(sql);
        return connect().prepareStatement(sql);
    }

    public static Clob encodeToJSON(Object obj) throws SQLException {
        if (obj == null) return null;
        return new SerialClob(JSON.encode(obj).toCharArray());
    }

    public static Object decodeFromJSON(Clob clob) throws SQLException {
        if (clob == null) return null;
        return JSON.decode(clob.getSubString(1L, (int) clob.length()));
    }

    public static Date decodeTimestamp(Timestamp ts) throws SQLException {
        if (ts == null) return null;
        return new Date(ts.getTime());
    }

    public static Timestamp encodeTimestamp(Date d) throws SQLException {
        if (d == null) return null;
        return new Timestamp(d.getTime());
    }

    public static boolean tableExists(String name) throws SQLException {
        boolean exists;
        try (PreparedStatement stmt = prepare("SHOW TABLES LIKE ?")) {
            String prefix = getPrefix();
            if (prefix != null) {
                name = prefix + name;
            }
            stmt.setString(1, name);
            try (ResultSet rs = stmt.executeQuery()) {
                exists = rs.next();
            }
        }
        return exists;
    }

    public static boolean dropTable(String name) throws SQLException {
        if (!tableExists(name)) return false;
        Utils.debug("dropping table '%s'", name);
        try (PreparedStatement stmt = prepare("DROP TABLE " + tableName(name))) {
            stmt.executeUpdate();
        }
        return true;
    }

    public static boolean columnExists(String tableName, String columnName) throws SQLException {
        boolean exists;
        try (PreparedStatement stmt = prepare("SHOW COLUMNS FROM " + tableName(tableName) + " LIKE ?")) {
            stmt.setString(1, columnName);
            try (ResultSet rs = stmt.executeQuery()) {
                exists = rs.next();
            }
        }
        return exists;
    }

    public static boolean addColumn(String tableName, String columnName, String columnDef) throws SQLException {
        if (columnExists(tableName, columnName)) {
            return false;
        }
        Utils.debug("adding column '%s' to table '%s'", columnName, tableName);
        try (PreparedStatement stmt = prepare(
                "ALTER TABLE " + tableName(tableName) + " ADD `" + columnName + "` " + columnDef)) {
            stmt.executeUpdate();
        }
        return true;
    }

    public static boolean dropColumn(String tableName, String columnName) throws SQLException {
        if (!columnExists(tableName, columnName)) return false;
        Utils.debug("dropping column '%s' from table '%s'", columnName, tableName);
        try (PreparedStatement stmt = prepare(
                "ALTER TABLE " + tableName(tableName) + " DROP `" + columnName + "`")) {
            stmt.executeUpdate();
        }
        return true;
    }

    public static interface DBListener {
        public void onDBConnected();
        public void onDBDisconnecting();
    }

}
