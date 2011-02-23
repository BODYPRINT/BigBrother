package me.taylorkelly.bigbrother;

import java.sql.Statement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import me.taylorkelly.bigbrother.datasource.ConnectionManager;

public class WorldManager {

    public final static String WORLD_TABLE_NAME = "bbworlds";
    private final static String WORLD_TABLE_SQL =
            "CREATE TABLE `bbworlds` ("
            + "`id` INTEGER PRIMARY KEY,"
            + "`name` varchar(50) NOT NULL DEFAULT 'world');";

    private HashMap<String, Integer> worldMap;

    public WorldManager() {
        if (!worldTableExists()) {
            createWorldTable();
        }
        worldMap = loadWorlds();
    }

    /**
     * Returns the BB index of the world to use (starts at 0 and goes up).
     * If BB has seen it before, it will use the key that it already had paired.
     * Otherwise it will designate a new key, and save that key to bbworlds for
     * later usage
     * @param world The name of the world
     * @return The index of the world
     */
    public int getWorld(String world) {
        if(worldMap.containsKey(world)) {
            return worldMap.get(world);
        } else {
            int nextKey = 0;
            if(!worldMap.isEmpty()) {
                nextKey = getMax(worldMap.values()) + 1;
            }
            saveWorld(world, nextKey);
            worldMap.put(world, nextKey);
            return nextKey;
        }
    }

    /**
     * Generic max finder for a collection. Only works with positive numbers
     * (which we'd be dealing with)
     * @param values Collection of values
     * @return The max of those numbers (or -1 if it's empty)
     */
    public static int getMax(Collection<Integer> values) {
        int max = -1;
        for(Integer value : values) {
            if(value > max) {
                max = value;
            }
        }
        return max;
    }

    private boolean saveWorld(String world, int index) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = ConnectionManager.getConnection();
            ps = conn.prepareStatement("INSERT INTO bbworlds (id, name) VALUES (?,?)");
            ps.setInt(1, index);
            ps.setString(2, world);
            ps.executeUpdate();
            conn.commit();
            return true;
        } catch (SQLException ex) {
            BBLogging.severe("World Insert Exception", ex);
            return false;
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ex) {
                BBLogging.severe("World Insert Exception (on close)", ex);
            }
        }
    }

    private HashMap<String, Integer> loadWorlds() {
        HashMap<String, Integer> ret = new HashMap<String, Integer>();
        Connection conn = null;
        Statement statement = null;
        ResultSet set = null;
        try {
            conn = ConnectionManager.getConnection();

            statement = conn.createStatement();
            set = statement.executeQuery("SELECT * FROM `bbworlds`;");
            int size = 0;
            while (set.next()) {
                size++;
                int index = set.getInt("id");
                String name = set.getString("name");
                ret.put(name, index);
            }
        } catch (SQLException ex) {
            BBLogging.severe("World Load Exception", ex);
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
                if (set != null) {
                    set.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ex) {
                BBLogging.severe("World Load Exception (on close)", ex);
            }
        }
        return ret;
    }

    private static boolean worldTableExists() {
        Connection conn = null;
        ResultSet rs = null;
        try {
            conn = ConnectionManager.getConnection();
            DatabaseMetaData dbm = conn.getMetaData();
            rs = dbm.getTables(null, null, WORLD_TABLE_NAME, null);
            if (!rs.next()) {
                return false;
            }
            return true;
        } catch (SQLException ex) {
            BBLogging.severe("World Table Check SQL Exception", ex);
            return false;
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ex) {
                BBLogging.severe("World Table Check SQL Exception (on closing)");
            }
        }
    }

    private static void createWorldTable() {
        Connection conn = null;
        Statement st = null;
        try {
            conn = ConnectionManager.getConnection();
            st = conn.createStatement();
            st.executeUpdate(WORLD_TABLE_SQL);
            conn.commit();
        } catch (SQLException e) {
            BBLogging.severe("Create World Table SQL Exception", e);
        } finally {
            try {
                if (st != null) {
                    st.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                BBLogging.severe("Could not create the world table (on close)");
            }
        }
    }
}
