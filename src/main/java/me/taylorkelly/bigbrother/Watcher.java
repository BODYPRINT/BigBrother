package me.taylorkelly.bigbrother;

import java.io.*;
import java.util.ArrayList;
import org.bukkit.*;
import org.bukkit.entity.Player;

public class Watcher {
	private ArrayList<String> watchList;
	private ArrayList<String> seenList;
	private Server server;
	private File dataFolder;

	public Watcher(ArrayList<String> watchList, ArrayList<String> seenList, Server server, File dataFolder) {
		this.watchList = watchList;
		this.seenList = seenList;
		this.server = server;
		this.dataFolder = dataFolder;
	}

	public boolean watching(Player player) {
		return watchList.contains(player.getName());
	}

	public boolean toggleWatch(String player) {
		boolean watching = false;
		if (watchList.contains(player)) {
			watchList.remove(player);
			saveWatchList();
		} else {
			watchPlayer(player);
			watching = true;
		}
		return watching;
	}

	public String getWatchedPlayers() {
		StringBuilder list = new StringBuilder();
		for (String name : watchList) {
			list.append(name);
			list.append(", ");
		}
		if (list.toString().contains(","))
			list.delete(list.lastIndexOf(","), list.length());
		return list.toString();
	}

	public boolean haveSeen(Player player) {
		return seenList.contains(player.getName());
	}

	public void markSeen(Player player) {
		if (!seenList.contains(player.getName())) {
			seenList.add(player.getName());
			saveSeenList();
		}
	}

	public void watchPlayer(Player player) {
		watchPlayer(player.getName());
	}

	public void watchPlayer(String player) {
		if (!watchList.contains(player)) {
			watchList.add(player);
			saveWatchList();
		}
	}

	public String getUnwatchedPlayers() {
		Player[] playerList = server.getOnlinePlayers();
		StringBuilder list = new StringBuilder();
		for (Player player : playerList) {
			if (!watchList.contains(player.getName())) {
				list.append(player.getName());
				list.append(", ");
			}
		}
		if (list.toString().contains(","))
			list.delete(list.lastIndexOf(","), list.length());
		return list.toString();
	}

	private void saveWatchList() {
		store("WatchedPlayers.txt", watchList);
	}

	private void saveSeenList() {
		store("SeenPlayers.txt", seenList);
	}

	private void store(String fileName, ArrayList<String> playerList) {
		File file = new File(dataFolder, fileName);
		BufferedWriter bwriter = null;
		FileWriter fwriter = null;
		try {
			fwriter = new FileWriter(file);
			bwriter = new BufferedWriter(fwriter);
			for (String name : playerList) {
				bwriter.write(name);
				bwriter.newLine();
			}
			bwriter.flush();
		} catch (IOException e) {
			BBLogging.severe("IO Exception (" + fileName + ")");
		} finally {
			try {
				if (bwriter != null) {
					bwriter.flush();
					bwriter.close();
				}
				if (fwriter != null)
					fwriter.close();
			} catch (IOException e) {
				BBLogging.severe("IO Exception (on close) (" + fileName + ")");
			}
		}
	}

}
