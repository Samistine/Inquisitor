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

    private static final Set<String> OPTIONS = new HashSet<String>();
    private static final Set<String> RESTART_OPTIONS = new HashSet<String>();
    private static final Options options;

    private static final Set<DBListener> listeners = new HashSet<DBListener>();

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
                    public void onOptionSet(Context ctx, String name, String value) {
                        ctx.sendLog("database option '%s' set to '%s'", name, value);
                        if (RESTART_OPTIONS.contains(name)) {
                            Config.save(ctx);
                            stop();
                            start();
                        }
                    }

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
            if (getUrl() == null) {
                throw new InquisitorException("url is not set");
            }
            if (getUsername() == null) {
                throw new InquisitorException("username is not set");
            }
            if (getPassword() == null) {
                throw new InquisitorException("password is not set");
            }
            connect();

        } catch (Exception e) {
            Utils.warning("database connection cannot be completed: %s", e.getMessage());
        }
    }

    public static void stop() {
        if (db == null) {
            return;
        }
        for (DBListener listener : listeners) {
            listener.onDBDisconnecting();
        }
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

    public static void getOptions(Context ctx, String name) throws OptionsException, PermissionsException {
        options.getOptions(ctx, name);
    }

    public static String getOption(Context ctx, String name) throws OptionsException, PermissionsException {
        return options.getOption(ctx, name);
    }

    public static void setOption(Context ctx, String name, String value) throws OptionsException, PermissionsException {
        options.setOption(ctx, name, value);
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
            if (!didUpdates) {
                doUpdates();
            }
            for (DBListener listener : listeners) {
                listener.onDBConnected();
            }
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
        if (obj == null) {
            return null;
        }
        return new SerialClob(JSON.encode(obj).toCharArray());
    }

    public static Object decodeFromJSON(Clob clob) throws SQLException {
        if (clob == null) {
            return null;
        }
        return JSON.decode(clob.getSubString(1L, (int) clob.length()));
    }

    public static Date decodeTimestamp(Timestamp ts) throws SQLException {
        if (ts == null) {
            return null;
        }
        return new Date(ts.getTime());
    }

    public static Timestamp encodeTimestamp(Date d) throws SQLException {
        if (d == null) {
            return null;
        }
        return new Timestamp(d.getTime());
    }

    public static boolean tableExists(String name) throws SQLException {
        PreparedStatement stmt = prepare("SHOW TABLES LIKE ?");
        String prefix = getPrefix();
        if (prefix != null) {
            name = prefix + name;
        }
        stmt.setString(1, name);
        ResultSet rs = stmt.executeQuery();
        boolean exists = rs.next();
        rs.close();
        stmt.close();
        return exists;
    }

    public static boolean dropTable(String name) throws SQLException {
        if (!tableExists(name)) {
            return false;
        }
        Utils.debug("dropping table '%s'", name);
        PreparedStatement stmt = prepare("DROP TABLE " + tableName(name));
        stmt.executeUpdate();
        stmt.close();
        return true;
    }

    public static boolean columnExists(String tableName, String columnName) throws SQLException {
        PreparedStatement stmt = prepare("SHOW COLUMNS FROM "
                + tableName(tableName) + " LIKE ?");
        stmt.setString(1, columnName);
        ResultSet rs = stmt.executeQuery();
        boolean exists = rs.next();
        rs.close();
        stmt.close();
        return exists;
    }

    public static boolean addColumn(String tableName, String columnName, String columnDef) throws SQLException {
        if (columnExists(tableName, columnName)) {
            return false;
        }
        Utils.debug("adding column '%s' to table '%s'", columnName, tableName);
        PreparedStatement stmt = prepare("ALTER TABLE " + tableName(tableName)
                + " ADD `" + columnName + "` " + columnDef);
        stmt.executeUpdate();
        stmt.close();
        return true;
    }

    public static boolean dropColumn(String tableName, String columnName) throws SQLException {
        if (!columnExists(tableName, columnName)) {
            return false;
        }
        Utils.debug("dropping column '%s' from table '%s'", columnName,
                tableName);
        PreparedStatement stmt = prepare("ALTER TABLE " + tableName(tableName)
                + " DROP `" + columnName + "`");
        stmt.executeUpdate();
        stmt.close();
        return true;
    }

    private static void doUpdates() throws SQLException {
        PreparedStatement stmt1 = null;
        PreparedStatement stmt2 = null;
        ResultSet rs = null;

        String[][] updates = new String[][]{
            new String[]{"blocksBroken", "totalBlocksBroken", "int"},
            new String[]{"blocksPlaced", "totalBlocksPlaced", "int"},
            new String[]{"itemsDropped", "totalItemsDropped", "int"},
            new String[]{"itemsPickedUp", "totalItemsPickedUp", "int"},
            new String[]{"itemsCrafted", "totalItemsCrafted", "int"},
            new String[]{"travelDistances", "totalDistanceTraveled", "float"}};

        Utils.debug("doing DB updates");

        try {
            dropTable("versions");
            if (tableExists("players")) {
                for (int i = 0; i < updates.length; i++) {
                    String[] data = updates[i];
                    if (addColumn("players", data[1], data[2] + " DEFAULT 0")) {
                        stmt1 = prepare("SELECT id," + data[0] + " FROM "
                                + tableName("players"));
                        rs = stmt1.executeQuery();
                        stmt2 = prepare("UPDATE " + tableName("players")
                                + " SET " + data[1] + "=? WHERE id=?");
                        while (rs.next()) {
                            int id = rs.getInt("id");
                            TypeMap map = (TypeMap) decodeFromJSON(rs.getClob(data[0]));
                            if (data[2].equals("int")) {
                                int count = totalIntegerTypeMap(map);
                                stmt2.setInt(1, count);
                            } else {
                                float count = totalFloatTypeMap(map);
                                stmt2.setFloat(1, count);
                            }
                            stmt2.setInt(2, id);
                            stmt2.executeUpdate();
                        }
                        stmt2.close();
                        stmt2 = null;
                        rs.close();
                        stmt1.close();
                    }
                }
            }

            if (tableExists("players")) {
                if (columnExists("players", "blocksBroken")) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("`id`");
                    for (Statistic stat : PlayerStats.group.getStatistics()) {
                        if (!stat.isMapped()) {
                            continue;
                        }
                        sb.append(",`").append(stat.getName()).append('`');
                    }
                    stmt1 = prepare("SELECT " + sb.toString() + " FROM " + tableName("players"));
                    rs = stmt1.executeQuery();
                    Map<Integer, Object> data = new HashMap<Integer, Object>();
                    while (rs.next()) {
                        int id = rs.getInt("id");
                        TypeMap mappedObjects = new TypeMap();
                        for (Statistic stat : PlayerStats.group.getStatistics()) {
                            if (!stat.isMapped()) {
                                continue;
                            }
                            TypeMap map = (TypeMap) decodeFromJSON(rs.getClob(stat.getName()));
                            mappedObjects.put(stat.getName(), map);
                        }
                        data.put(id, mappedObjects);
                    }
                    rs.close();
                    rs = null;
                    stmt1.close();
                    stmt1 = null;

                    for (Statistic stat : PlayerStats.group.getStatistics()) {
                        if (!stat.isMapped()) {
                            continue;
                        }
                        dropColumn("players", stat.getName());
                    }

                    if (!columnExists("players", Statistic.MappedObjectsColumn)) {
                        addColumn("players", Statistic.MappedObjectsColumn,
                                Statistic.Type.OBJECT.getSQLDef());
                    }

                    stmt2 = prepare("UPDATE " + tableName("players") + " SET `"
                            + Statistic.MappedObjectsColumn
                            + "`=? WHERE `id`=?");
                    Utils.debug("Updating %s players...", data.keySet().size());
                    for (int id : data.keySet()) {
                        stmt2.setClob(1, encodeToJSON((TypeMap) data.get(id)));
                        stmt2.setInt(2, id);
                        Utils.debug("updating player %s", id);
                        stmt2.executeUpdate();
                    }
                    stmt2.close();
                    stmt2 = null;
                }
            }

            if (tableExists("players") && !columnExists("players", "uuid")) {
                addColumn("players", "uuid", "varchar(36)");
            }

            didUpdates = true;
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (stmt1 != null) {
                    stmt1.close();
                }
                if (stmt2 != null) {
                    stmt2.close();
                }
            } catch (SQLException se) {
            }
        }
    }

    private static int totalIntegerTypeMap(TypeMap m) {
        if (m == null) {
            return 0;
        }
        int t = 0;
        for (String key : m.getKeys()) {
            t += m.getInt(key);
        }
        return t;
    }

    private static float totalFloatTypeMap(TypeMap m) {
        if (m == null) {
            return 0;
        }
        float t = 0;
        for (String key : m.getKeys()) {
            t += m.getFloat(key);
        }
        return t;
    }

    public static interface DBListener {

        public void onDBConnected();

        public void onDBDisconnecting();
    }

}
