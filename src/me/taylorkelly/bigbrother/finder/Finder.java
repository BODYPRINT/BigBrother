package me.taylorkelly.bigbrother.finder;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.logging.Level;

import me.taylorkelly.bigbrother.BBSettings;
import me.taylorkelly.bigbrother.BigBrother;
import me.taylorkelly.bigbrother.datablock.BBDataBlock;
import me.taylorkelly.bigbrother.datasource.ConnectionManager;

import org.bukkit.*;
import org.bukkit.entity.Player;

public class Finder {
    private Location location;
    private int radius;
    private ArrayList<Player> players;

    public Finder(Location location) {
        this.location = location;
        this.radius = BBSettings.defaultSearchRadius;
        players = new ArrayList<Player>();
    }

    public void setRadius(double radius) {
        this.radius = (int) radius;
    }

    public void addReciever(Player player) {
        players.add(player);
    }

    public void find() {
        mysqlFind(!BBSettings.mysql);
    }

    public void find(String player) {
        mysqlFind(!BBSettings.mysql, player);
    }

    public void find(ArrayList<String> players) {
        // TODO find around player
    }

    // TODO use IN(1,2,3)
    private void mysqlFind(boolean sqlite) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        HashMap<String, Integer> modifications = new HashMap<String, Integer>();
        try {
            conn = ConnectionManager.getConnection();

            // TODO maybe more customizable actions?
            String actionString = "action IN('" + BBDataBlock.BLOCK_BROKEN + "', '" + BBDataBlock.BLOCK_PLACED + "', '" + BBDataBlock.LEAF_DECAY + "', '" + BBDataBlock.TNT_EXPLOSION + "', '" + BBDataBlock.CREEPER_EXPLOSION + "', '" + BBDataBlock.MISC_EXPLOSION + "')";
            ps = conn.prepareStatement("SELECT player, count(player) AS modifications FROM " + BBDataBlock.BBDATA_NAME + " WHERE " + actionString
                    + " AND rbacked = '0' AND x < ? AND x > ? AND y < ? AND y > ? AND z < ? AND z > ? GROUP BY player ORDER BY id DESC");

            ps.setInt(1, location.getBlockX() + radius);
            ps.setInt(2, location.getBlockX() - radius);
            ps.setInt(3, location.getBlockY() + radius);
            ps.setInt(4, location.getBlockY() - radius);
            ps.setInt(5, location.getBlockZ() + radius);
            ps.setInt(6, location.getBlockZ() - radius);
            rs = ps.executeQuery();
            conn.commit();

            int size = 0;
            while (rs.next()) {
                String player = rs.getString("player");
                int mods = rs.getInt("modifications");
                modifications.put(player, mods);
                size++;
            }
            if (size > 0) {
                StringBuilder playerList = new StringBuilder();
                for (Entry<String, Integer> entry : modifications.entrySet()) {
                    playerList.append(entry.getKey());
                    playerList.append(" (");
                    playerList.append(entry.getValue());
                    playerList.append("), ");
                }
                playerList.delete(playerList.lastIndexOf(","), playerList.length());
                for (Player player : players) {
                    player.sendMessage(BigBrother.premessage + size + " player(s) have modified this area:");
                    player.sendMessage(playerList.toString());
                }
            } else {
                for (Player player : players) {
                    player.sendMessage(BigBrother.premessage + "No modifications in this area.");
                }

            }
        } catch (SQLException ex) {
            BigBrother.log.log(Level.SEVERE, "[BBROTHER]: Find SQL Exception", ex);
        } finally {
            try {
                if (rs != null)
                    rs.close();
                if (ps != null)
                    ps.close();
            } catch (SQLException ex) {
                BigBrother.log.log(Level.SEVERE, "[BBROTHER]: Find SQL Exception (on close)");
            }
        }
    }

    private void mysqlFind(boolean sqlite, String playerName) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        HashMap<Integer, Integer> creations = new HashMap<Integer, Integer>();
        HashMap<Integer, Integer> destructions = new HashMap<Integer, Integer>();

        try {
            conn = ConnectionManager.getConnection();

            // TODO maybe more customizable actions?
            String actionString = "action IN('" + BBDataBlock.BLOCK_BROKEN + "', '" + BBDataBlock.BLOCK_PLACED + "', '" + BBDataBlock.LEAF_DECAY + "', '" + BBDataBlock.TNT_EXPLOSION + "', '" + BBDataBlock.CREEPER_EXPLOSION + "', '" + BBDataBlock.MISC_EXPLOSION + "')";
            ps = conn.prepareStatement("SELECT action, type FROM " + BBDataBlock.BBDATA_NAME + " WHERE " + actionString
                    + " AND rbacked = 0 AND x < ? AND x > ? AND y < ? AND y > ?  AND z < ? AND z > ? AND player = ? order by date desc");

            ps.setInt(1, location.getBlockX() + radius);
            ps.setInt(2, location.getBlockX() - radius);
            ps.setInt(3, location.getBlockY() + radius);
            ps.setInt(4, location.getBlockY() - radius);
            ps.setInt(5, location.getBlockZ() + radius);
            ps.setInt(6, location.getBlockZ() - radius);
            ps.setString(7, playerName);
            rs = ps.executeQuery();
            conn.commit();

            int size = 0;
            while (rs.next()) {
                int action = rs.getInt("action");
                int type = rs.getInt("type");

                switch (action) {
                case (BBDataBlock.BLOCK_BROKEN):
                case (BBDataBlock.LEAF_DECAY):
                case (BBDataBlock.TNT_EXPLOSION):
                case (BBDataBlock.CREEPER_EXPLOSION):
                case (BBDataBlock.MISC_EXPLOSION):
                    if (destructions.containsKey(type)) {
                        destructions.put(type, destructions.get(type) + 1);
                        size++;
                    } else {
                        destructions.put(type, 1);
                        size++;
                    }
                    break;
                case (BBDataBlock.BLOCK_PLACED):
                    if (creations.containsKey(type)) {
                        creations.put(type, creations.get(type) + 1);
                        size++;
                    } else {
                        creations.put(type, 1);
                        size++;
                    }
                    break;
                }

            }
            if (size > 0) {
                StringBuilder creationList = new StringBuilder();
                // creationList.append(Color.AQUA);
                creationList.append("Placed Blocks: ");
                // creationList.append(Color.WHITE);
                for (Entry<Integer, Integer> entry : creations.entrySet()) {
                    creationList.append(Material.getMaterial(entry.getKey()));
                    creationList.append(" (");
                    creationList.append(entry.getValue());
                    creationList.append("), ");
                }
                if (creationList.toString().contains(","))
                    creationList.delete(creationList.lastIndexOf(","), creationList.length());
                StringBuilder brokenList = new StringBuilder();
                // brokenList.append(Color.RED);
                brokenList.append("Broken Blocks: ");
                // brokenList.append(Color.WHITE);
                for (Entry<Integer, Integer> entry : destructions.entrySet()) {
                    brokenList.append(Material.getMaterial(entry.getKey()));
                    brokenList.append(" (");
                    brokenList.append(entry.getValue());
                    brokenList.append("), ");
                }
                if (brokenList.toString().contains(","))
                    brokenList.delete(brokenList.lastIndexOf(","), brokenList.length());
                for (Player player : players) {
                    player.sendMessage(BigBrother.premessage + playerName + " has made " + size + " modifications");
                    if (creations.entrySet().size() > 0)
                        player.sendMessage(creationList.toString());
                    if (destructions.entrySet().size() > 0)
                        player.sendMessage(brokenList.toString());
                }
            } else {
                for (Player player : players) {
                    player.sendMessage(BigBrother.premessage + playerName + " has no modifications in this area.");
                }

            }
        } catch (SQLException ex) {
            BigBrother.log.log(Level.SEVERE, "[BBROTHER]: Find SQL Exception", ex);
        } finally {
            try {
                if (rs != null)
                    rs.close();
                if (ps != null)
                    ps.close();
            } catch (SQLException ex) {
                BigBrother.log.log(Level.SEVERE, "[BBROTHER]: Find SQL Exception (on close)");
            }
        }
    }
}
