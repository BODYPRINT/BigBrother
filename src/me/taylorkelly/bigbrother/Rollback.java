package me.taylorkelly.bigbrother;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.logging.Level;

import me.taylorkelly.bigbrother.datablock.BBDataBlock;

import org.bukkit.*;

public class Rollback {
	private Server server;
	private String playerName;
	private ArrayList<Player> players;
	private LinkedList<RollbackDataBlock> list;

	public Rollback(Server server, String playerName) {
		this.server = server;
		this.playerName = playerName;
		players = new ArrayList<Player>();
		list = new LinkedList<RollbackDataBlock>();
	}

	public void addReciever(Player player) {
		players.add(player);
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
				conn.setAutoCommit(false);
			}

			// TODO maybe more customizable actions?
			String actionString = "action = " + BBDataBlock.BLOCK_BROKEN + " or action = " + BBDataBlock.BLOCK_PLACED + " or action = "
					+ BBDataBlock.DELTA_CHEST + " or action = " + BBDataBlock.SIGN_TEXT;
			ps = conn.prepareStatement("SELECT * from " + BBDataBlock.BBDATA_NAME + " where (" + actionString
					+ ") player = ? and rbacked = 0 order by date desc", Statement.RETURN_GENERATED_KEYS);

			ps.setString(1, playerName);
			set = ps.executeQuery();

			if (!set.next()) {
				for (Player player : players) {
					player.sendMessage(BigBrother.premessage + "Nothing to rollback.");
				}
			} else {
				set.last();
				int size = set.getRow();
				set.first();
				for (Player player : players) {
					player.sendMessage(BigBrother.premessage + "Rolling back " + size + " edits.");
				}
				list.addLast(new RollbackDataBlock(set.getString("player"), set.getInt("action"), set.getInt("world"), set.getInt("x"), set.getInt("y"), set
						.getInt("z"), set.getString("data")));

				try {
					rollbackBlocks();
					ps = conn.prepareStatement("UPDATE " + BBDataBlock.BBDATA_NAME + " set rbacked = 1 where (" + actionString
							+ ") player = ? and rbacked = 0 order by date desc");
					ps.setString(1, playerName);
					ps.execute();
					for (Player player : players) {
						player.sendMessage(BigBrother.premessage + "Successfully rollback'd.");
					}
				} catch (SQLException ex) {
					BigBrother.log.log(Level.SEVERE, "[BBROTHER]: Rollback edit SQL Exception");
				}
			}
		} catch (SQLException ex) {
			BigBrother.log.log(Level.SEVERE, "[BBROTHER]: Rollback get SQL Exception");
		} catch (ClassNotFoundException e) {
			BigBrother.log.log(Level.SEVERE, "[BBROTHER]: Rollback SQL Exception (cnf)"  + ((sqlite)?"sqlite":"mysql"));
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

	private void rollbackBlocks() {
		// TODO Auto-generated method stub
		
	}

	private class RollbackDataBlock {
		private String player;
		private int action;
		private Location location;
		private String data;

		public RollbackDataBlock(String player, int action, int world, int x, int y, int z, String data) {
			this.player = player;
			this.action = action;
			this.location = new Location(server.getWorlds()[world], x, y, z);
			this.data = data;
		}

	}
}
