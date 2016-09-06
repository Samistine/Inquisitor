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

import java.sql.Clob;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */
public class Job {

    private String tableName;
    private Map<String, Object> data;
    private Object keyValue;
    private String keyColumn;
    private boolean committed = false;

    public Job(String tableName, Map<String, Object> data, Object keyValue) {
        this(tableName, data, keyValue, null);
    }

    public Job(String tableName, Map<String, Object> data, Object keyValue, String keyColumn) {
        this.tableName = tableName;
        this.data = data;
        this.keyValue = keyValue;
        this.keyColumn = keyColumn;
    }

    @Override
    public String toString() {
        return "Job[" + tableName + "," + keyValue + "]";
    }

    public boolean isCommitted() {
        return committed;
    }

    void commit() {
        PreparedStatement stmt = null;
        StringBuilder sql = new StringBuilder();
        List<String> cols = new ArrayList<>(data.keySet());
        try {
            if (keyColumn == null) {
                // insert
                sql.append("INSERT INTO ").append(DB.tableName(tableName)).append(" (");
                for (String col : cols) {
                    sql.append('`').append(col).append("`,");
                }
                sql.deleteCharAt(sql.length() - 1);
                sql.append(") VALUES (");
                for (String col : cols) {
                    sql.append("?,");
                }
                sql.deleteCharAt(sql.length() - 1);
                sql.append(")");
                stmt = DB.prepare(sql.toString());
                int colNum = 1;
                for (String col : cols) {
                    setParameter(stmt, colNum++, data.get(col));
                }
            } else {
                // update
                sql.append("UPDATE ").append(DB.tableName(tableName)).append(" SET ");
                for (String col : cols) {
                    sql.append('`').append(col).append("`=?,");
                }
                sql.deleteCharAt(sql.length() - 1);
                sql.append(" WHERE `").append(keyColumn).append("`=?");
                stmt = DB.prepare(sql.toString());
                int colNum = 1;
                for (String col : cols) {
                    setParameter(stmt, colNum++, data.get(col));
                }
                setParameter(stmt, colNum++, keyValue);
            }
            stmt.executeUpdate();

        } catch (SQLException se) {
            Utils.severe("SQLException while committing statistics for '%s' in '%s': %s", keyValue, tableName, se.getMessage());
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException se) {
            }
        }
        committed = true;
        synchronized (this) {
            notifyAll();
        }
    }

    private void setParameter(PreparedStatement stmt, int colNum, Object val) throws SQLException {
        if (val == null) {
            stmt.setString(colNum++, null);
        } else if (val instanceof String) {
            stmt.setString(colNum++, (String) val);
        } else if (val instanceof Boolean) {
            stmt.setInt(colNum++, ((Boolean) val) ? 1 : 0);
        } else if (val instanceof Byte) {
            stmt.setByte(colNum++, (Byte) val);
        } else if (val instanceof Short) {
            stmt.setShort(colNum++, (Short) val);
        } else if (val instanceof Integer) {
            stmt.setInt(colNum++, (Integer) val);
        } else if (val instanceof Long) {
            stmt.setLong(colNum++, (Long) val);
        } else if (val instanceof Float) {
            stmt.setFloat(colNum++, (Float) val);
        } else if (val instanceof Double) {
            stmt.setDouble(colNum++, (Double) val);
        } else if (val instanceof Date) {
            stmt.setTimestamp(colNum++, new Timestamp(((Date) val).getTime()));
        } else if (val instanceof Clob) {
            stmt.setClob(colNum++, (Clob) val);
        } else {
            throw new SQLException(val.getClass().getName() + " is an unsupported parameter type");
        }
    }

}
