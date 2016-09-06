package com.frdfsnlght.inquisitor;

import static com.frdfsnlght.inquisitor.DB.addColumn;
import static com.frdfsnlght.inquisitor.DB.columnExists;
import static com.frdfsnlght.inquisitor.DB.decodeFromJSON;
import static com.frdfsnlght.inquisitor.DB.dropColumn;
import static com.frdfsnlght.inquisitor.DB.dropTable;
import static com.frdfsnlght.inquisitor.DB.encodeToJSON;
import static com.frdfsnlght.inquisitor.DB.prepare;
import static com.frdfsnlght.inquisitor.DB.tableExists;
import static com.frdfsnlght.inquisitor.DB.tableName;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Samuel
 */
public class Updates {

    public boolean doUpdates() throws SQLException {

        String[][] updates = new String[][]{
            new String[]{"blocksBroken", "totalBlocksBroken", "int"},
            new String[]{"blocksPlaced", "totalBlocksPlaced", "int"},
            new String[]{"itemsDropped", "totalItemsDropped", "int"},
            new String[]{"itemsPickedUp", "totalItemsPickedUp", "int"},
            new String[]{"itemsCrafted", "totalItemsCrafted", "int"},
            new String[]{"travelDistances", "totalDistanceTraveled", "float"}};

        Utils.debug("doing DB updates");

        dropTable("versions");

        if (tableExists("players")) {

            for (String[] data : updates) {
                if (addColumn("players", data[1], data[2] + " DEFAULT 0")) {
                    try (PreparedStatement stmt1 = prepare("SELECT id," + data[0] + " FROM " + tableName("players"));
                            ResultSet rs = stmt1.executeQuery()) {
                        try (PreparedStatement stmt2 = prepare("UPDATE " + tableName("players") + " SET " + data[1] + "=? WHERE id=?")) {
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
                        }
                    }
                }
            }

            if (columnExists("players", "blocksBroken")) {
                StringBuilder sb = new StringBuilder("`id`");
                for (Statistic stat : PlayerStats.group.getStatistics()) {
                    if (stat.isMapped()) {
                        sb.append(",`").append(stat.getName()).append('`');
                    }
                }
                try (PreparedStatement stmt1 = prepare("SELECT " + sb.toString() + " FROM " + tableName("players"));
                        ResultSet rs = stmt1.executeQuery()) {
                    Map<Integer, Object> data = new HashMap<>();
                    while (rs.next()) {
                        int id = rs.getInt("id");
                        TypeMap mappedObjects = new TypeMap();
                        for (Statistic stat : PlayerStats.group.getStatistics()) {
                            if (stat.isMapped()) {
                                TypeMap map = (TypeMap) decodeFromJSON(rs.getClob(stat.getName()));
                                mappedObjects.put(stat.getName(), map);
                            }
                        }
                        data.put(id, mappedObjects);
                    }

                    for (Statistic stat : PlayerStats.group.getStatistics()) {
                        if (stat.isMapped()) {
                            dropColumn("players", stat.getName());
                        }
                    }

                    if (!columnExists("players", Statistic.MappedObjectsColumn)) {
                        addColumn("players", Statistic.MappedObjectsColumn, Statistic.Type.OBJECT.getSQLDef());
                    }

                    try (PreparedStatement stmt2 = prepare("UPDATE " + tableName("players") + " SET `" + Statistic.MappedObjectsColumn + "`=? WHERE `id`=?")) {
                        Utils.debug("Updating %s players...", data.keySet().size());
                        for (int id : data.keySet()) {
                            stmt2.setClob(1, encodeToJSON((TypeMap) data.get(id)));
                            stmt2.setInt(2, id);
                            Utils.debug("updating player %s", id);
                            stmt2.executeUpdate();
                        }
                    }
                }
            }

            if (!columnExists("players", "uuid")) {
                addColumn("players", "uuid", "varchar(36)");
            }
        }

        return true;
    }

    private int totalIntegerTypeMap(TypeMap m) {
        if (m == null) return 0;
        return m.getKeys().stream().mapToInt(m::getInt).sum();
    }

    private float totalFloatTypeMap(TypeMap m) {
        if (m == null) return 0;
        return m.getKeys().stream().map(m::getFloat).reduce(0f, Float::sum);
    }
}
