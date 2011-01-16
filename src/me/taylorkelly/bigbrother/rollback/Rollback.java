package me.taylorkelly.bigbrother.rollback;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.logging.Level;

import me.taylorkelly.bigbrother.BBSettings;
import me.taylorkelly.bigbrother.BigBrother;
import me.taylorkelly.bigbrother.datablock.BBDataBlock;

import org.bukkit.*;
import org.bukkit.entity.Player;

public class Rollback {
    Server server;
    ArrayList<Player> recievers;
    ArrayList<String> players;
    boolean rollbackAll;
    long time;
    ArrayList<Integer> blockTypes;

    private LinkedList<BBDataBlock> listBlocks;
    private static LinkedList<BBDataBlock> lastRollback = new LinkedList<BBDataBlock>();
    private static String undoRollback = null;

    public Rollback(Server server) {
        this.rollbackAll = false;
        this.server = server;
        this.time = 0;
        blockTypes = new ArrayList<Integer>();
        players = new ArrayList<String>();
        recievers = new ArrayList<Player>();

        listBlocks = new LinkedList<BBDataBlock>();
    }

    public void addReciever(Player player) {
        recievers.add(player);
    }

    public void rollback() {
        switch (BBSettings.dataDest) {
        case MYSQL:
        case MYSQL_AND_FLAT:
            mysqlRollback(false);
            break;
        case SQLITE:
        case SQLITE_AND_FLAT:
            mysqlRollback(true);
            break;
        }
    }

    private void mysqlRollback(boolean sqlite) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet set = null;
        try {
            if (sqlite) {
                Class.forName("org.sqlite.JDBC");
                conn = DriverManager.getConnection(BBSettings.liteDb);
            } else {
                Class.forName("com.mysql.jdbc.Driver");
                conn = DriverManager.getConnection(BBSettings.mysqlDB, BBSettings.mysqlUser, BBSettings.mysqlPass);
            }

            ps = conn.prepareStatement(RollbackPreparedStatement.create(this));

            set = ps.executeQuery();

            int size = 0;
            while (set.next()) {
                listBlocks.addLast(BBDataBlock.getBBDataBlock(set.getString("player"), set.getInt("action"), set.getInt("world"), set.getInt("x"),
                        set.getInt("y"), set.getInt("z"), set.getInt("type"), set.getString("data")));
                size++;
            }
            if (size > 0) {
                for (Player player : recievers) {
                    player.sendMessage(BigBrother.premessage + "Rolling back " + size + " edits.");
                    String players = (rollbackAll) ? "All Players" : getSimpleString(this.players);
                    player.sendMessage(ChatColor.BLUE + "Player(s): " + ChatColor.WHITE + players);
                    if (blockTypes.size() > 0) {
                        player.sendMessage(ChatColor.BLUE + "Block Type(s): " + ChatColor.WHITE + getSimpleString(this.blockTypes));
                    }
                    if (time != 0) {
                        Calendar cal = Calendar.getInstance();
                        String DATE_FORMAT = "kk:mm:ss 'on' MMM d";
                        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
                        cal.setTimeInMillis(time*1000);
                        player.sendMessage(ChatColor.BLUE + "Since: " + ChatColor.WHITE + sdf.format(cal.getTime()));
                    }

                }
                try {
                    ps.close();
                    rollbackBlocks();
                    ps = conn.prepareStatement(RollbackPreparedStatement.update(this));
                    ps.execute();
                    for (Player player : recievers) {
                        player.sendMessage(BigBrother.premessage + "Successfully rollback'd.");
                    }
                    undoRollback = RollbackPreparedStatement.undoStatement(this);
                } catch (SQLException ex) {
                    BigBrother.log.log(Level.SEVERE, "[BBROTHER]: Rollback edit SQL Exception", ex);
                }
            } else {
                for (Player player : recievers) {
                    player.sendMessage(BigBrother.premessage + "Nothing to rollback.");
                }
            }
        } catch (SQLException ex) {
            BigBrother.log.log(Level.SEVERE, "[BBROTHER]: Rollback get SQL Exception", ex);
        } catch (ClassNotFoundException e) {
            BigBrother.log.log(Level.SEVERE, "[BBROTHER]: Rollback SQL Exception (cnf)" + ((sqlite) ? "sqlite" : "mysql"));
        } finally {

            try {
                if (set != null)
                    set.close();
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                BigBrother.log.log(Level.SEVERE, "[BBROTHER]: Rollback get SQL Exception (on close)");
            }
        }
    }

    private String getSimpleString(ArrayList<?> list) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            builder.append(list.get(i).toString());
            if (i + 1 < list.size())
                builder.append(", ");
        }
        return builder.toString();
    }

    public void rollbackAll() {
        rollbackAll = true;
    }

    public void addPlayers(ArrayList<String> playerList) {
        players.addAll(playerList);
    }

    public void setTime(long l) {
        this.time = l;
    }

    public void addTypes(ArrayList<Integer> blockTypes) {
        this.blockTypes.addAll(blockTypes);
    }

    private void rollbackBlocks() {
        lastRollback.clear();
        while (listBlocks.size() > 0) {
            BBDataBlock dataBlock = listBlocks.removeFirst();
            if (dataBlock != null) {
                lastRollback.addFirst(dataBlock);
                dataBlock.rollback(server);
            }
        }
    }

    public static boolean canUndo() {
        if (lastRollback != null) {
            return lastRollback.size() > 0;
        } else
            return false;
    }

    public static int undoSize() {
        if (lastRollback != null) {
            return lastRollback.size();
        } else
            return 0;
    }

    public static void undo(Server server, Player player) {
        int i = 0;
        while (lastRollback.size() > 0) {
            BBDataBlock dataBlock = lastRollback.removeFirst();
            if (dataBlock != null) {
                dataBlock.redo(server);
                i++;
            }
        }
        if(undoRollback != null) {
            Connection conn = null;
            PreparedStatement ps = null;
            ResultSet set = null;
            boolean sqlite = false;
            switch (BBSettings.dataDest) {
            case MYSQL:
            case MYSQL_AND_FLAT:
                sqlite = false;
                break;
            case SQLITE:
            case SQLITE_AND_FLAT:
                sqlite = true;
                break;
            }
            try {
                if (sqlite) {
                    Class.forName("org.sqlite.JDBC");
                    conn = DriverManager.getConnection(BBSettings.liteDb);
                } else {
                    Class.forName("com.mysql.jdbc.Driver");
                    conn = DriverManager.getConnection(BBSettings.mysqlDB, BBSettings.mysqlUser, BBSettings.mysqlPass);
                }
                ps = conn.prepareStatement(undoRollback);
                ps.execute();
                undoRollback = null;
                player.sendMessage(ChatColor.AQUA + "Successfully undid a rollback of " + i + " edits");
            } catch (SQLException ex) {
                BigBrother.log.log(Level.SEVERE, "[BBROTHER]: Rollback undo SQL Exception", ex);
            } catch (ClassNotFoundException e) {
                BigBrother.log.log(Level.SEVERE, "[BBROTHER]: Rollback Undo (Class not found)" + ((sqlite) ? "sqlite" : "mysql"));
            } finally {
                try {
                    if (set != null)
                        set.close();
                    if (ps != null)
                        ps.close();
                    if (conn != null)
                        conn.close();
                } catch (SQLException ex) {
                    BigBrother.log.log(Level.SEVERE, "[BBROTHER]: Rollback undo (on close)");
                }
            }
        }
    }
}
