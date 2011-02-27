package me.taylorkelly.bigbrother;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import me.taylorkelly.bigbrother.datasource.ConnectionManager;
import me.taylorkelly.util.Time;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

class Cleanser {

    static boolean needsCleaning() {
        return BBSettings.cleanseAge != -1 || BBSettings.maxRecords != -1;
    }

    static void clean() {
        if (BBSettings.cleanseAge != -1) {
            cleanByAge();
        }
        
        //if(BBSettings.maxRecords != -1) {
        //    cleanByNumber();
        //}
    }

    private static void cleanByAge() {
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = ConnectionManager.getConnection();
            stmt = conn.createStatement();
            int amount = stmt.executeUpdate("DELETE FROM `bbdata` WHERE date < " + Long.valueOf(Time.ago(BBSettings.cleanseAge)) + ";");
            BBLogging.info("Cleaned out " + Integer.valueOf(amount) + " records because of age");
            conn.commit();
        } catch (SQLException ex) {
            BBLogging.severe("Cleanse SQL exception (by age)", ex);
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ex) {
                BBLogging.severe("Cleanse SQL exception (by age) (on close)", ex);
            }
        }
    }

    @SuppressWarnings("unused") // Unused
	private static void cleanByNumber() {
        if (BBSettings.mysql) {
        	if(BBSettings.maxRecords<0)
        	{
        		// Fix exception caused when trying to delete -1 records.
        		BBLogging.info("Skipping; max-records is negative.");
        		return;
        	}
            Connection conn = null;
            Statement statement = null;
            ResultSet set = null;
            int id = -1;
            try {
                conn = ConnectionManager.getConnection();
                statement = conn.createStatement();
                // Probably should do a COUNT, then delete maxrecords - count.
                set = statement.executeQuery("SELECT * FROM `bbdata` ORDER BY `id` DESC LIMIT " + Long.valueOf(BBSettings.maxRecords) + ";");
                set.afterLast();
                if (set.previous()) {
                    id = set.getInt("id");
                }
            } catch (SQLException ex) {
                BBLogging.severe("Cleanse SQL Exception (on # inspect)", ex);
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
                    BBLogging.severe("Cleanse SQL Exception (on # inspect) (on close)", ex);
                }
            }

            if (id != -1) {
                conn = null;
                Statement stmt = null;
                try {
                    conn = ConnectionManager.getConnection();
                    stmt = conn.createStatement();
                    int amount = stmt.executeUpdate("DELETE FROM `bbdata` WHERE id < " + Integer.valueOf(id) + ";");
                    BBLogging.info("Cleaned out " + Integer.valueOf(amount) + " records because there are too many");
                    conn.commit();
                } catch (SQLException ex) {
                    BBLogging.severe("Cleanse SQL exception (by #)", ex);
                } finally {
                    try {
                        if (stmt != null) {
                            stmt.close();
                        }
                        if (conn != null) {
                            conn.close();
                        }
                    } catch (SQLException ex) {
                        BBLogging.severe("Cleanse SQL exception (by #) (on close)", ex);
                    }
                }
            }
        } else {
            BBLogging.info("SQLite can't cleanse by # of records.");
        }
    }

    static void clean(Player player) {
        if (BBSettings.cleanseAge != -1) {
            cleanByAge(player);
        }
        //cleanByNumber(player);


    }

    private static void cleanByAge(Player player) {
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = ConnectionManager.getConnection();
            stmt = conn.createStatement();
            int amount = stmt.executeUpdate("DELETE FROM `bbdata` WHERE date < " + Long.valueOf(Time.ago(BBSettings.cleanseAge)) + ";");
            player.sendMessage(ChatColor.BLUE + "Cleaned out " + Integer.valueOf(amount) + " records because of age");
            conn.commit();
        } catch (SQLException ex) {
            BBLogging.severe("Cleanse SQL exception (by age)", ex);
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ex) {
                BBLogging.severe("Cleanse SQL exception (by age) (on close)", ex);
            }
        }
    }

    @SuppressWarnings("unused")
	private static void cleanByNumber(Player player) {
        if (BBSettings.mysql) {
        	if(BBSettings.maxRecords<0)
        	{
        		// Fix exception caused when trying to delete -1 records.
        		BBLogging.info("Skipping; max-records is negative.");
        		return;
        	}
            Connection conn = null;
            Statement statement = null;
            ResultSet set = null;
            int id = -1;
            try {
                conn = ConnectionManager.getConnection();
                statement = conn.createStatement();
                set = statement.executeQuery("SELECT * FROM `bbdata` ORDER BY `id` DESC LIMIT " + Long.valueOf(BBSettings.maxRecords) + ";");
                set.afterLast();
                if (set.previous()) {
                    id = set.getInt("id");
                }
            } catch (SQLException ex) {
                BBLogging.severe("Cleanse SQL Exception (on # inspect)", ex);
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
                    BBLogging.severe("Cleanse SQL Exception (on # inspect) (on close)", ex);
                }
            }

            if (id != -1) {
                conn = null;
                Statement stmt = null;
                try {
                    conn = ConnectionManager.getConnection();
                    stmt = conn.createStatement();
                    int amount = stmt.executeUpdate("DELETE FROM `bbdata` WHERE id < " + Integer.valueOf(id) + ";");
                    player.sendMessage(ChatColor.BLUE + "Cleaned out " + Integer.valueOf(amount) + " records because there are too many");
                    conn.commit();
                } catch (SQLException ex) {
                    BBLogging.severe("Cleanse SQL exception (by #)", ex);
                } finally {
                    try {
                        if (stmt != null) {
                            stmt.close();
                        }
                        if (conn != null) {
                            conn.close();
                        }
                    } catch (SQLException ex) {
                        BBLogging.severe("Cleanse SQL exception (by #) (on close)", ex);
                    }
                }
            }
        } else {
            player.sendMessage(ChatColor.RED + "SQLite can't cleanse by # of records.");
        }
    }
}
