package me.taylorkelly.bigbrother;

import me.taylorkelly.bigbrother.datablock.*;

import org.bukkit.*;
import org.bukkit.event.player.*;

public class BBPlayerListener extends PlayerListener {
	private BigBrother plugin;

	public BBPlayerListener(BigBrother plugin) {
		this.plugin = plugin;
	}

	public void onPlayerCommand(PlayerChatEvent event) {
		String[] split = event.getMessage().split(" ");
		Player player = event.getPlayer();

		if (split[0].equalsIgnoreCase("/watchplayer")) {
			if (split.length == 2) {
				Player watchee = plugin.getServer().getPlayer(split[1]);
				// TODO Change to matchPlayer
				String playerName = (watchee == null) ? split[1] : watchee
						.getName();

				if (plugin.toggleWatch(playerName)) {
					String status = (watchee == null) ? " (offline)"
							: " (online)";
					player.sendMessage(BigBrother.premessage + "Now watching "
							+ playerName + status);
				} else {
					String status = (watchee == null) ? " (offline)"
							: " (online)";
					player.sendMessage(BigBrother.premessage
							+ "No longer watching " + playerName + status);
				}
			} else {
				player.sendMessage(BigBrother.premessage + "usage is "
						+ Color.RED + "/watchplayer <player>");
			}
			event.setCancelled(true);
		} else if (split[0].equalsIgnoreCase("/watched")) {
			player.sendMessage(BigBrother.premessage + "Now watching:");
			player.sendMessage(plugin.getWatchedPlayers());
			event.setCancelled(true);
		} else if (split[0].equalsIgnoreCase("/unwatched")) {
			player.sendMessage(BigBrother.premessage + "Not watching:");
			player.sendMessage(plugin.getUnwatchedPlayers());
			event.setCancelled(true);
		} else if (split[0].equalsIgnoreCase("/rollback")) {
			if (split.length > 1) {
				Player rollbacker = plugin.getServer().getPlayer(split[1]);
				// TODO matchplayer
				String playerName = (rollbacker == null) ? split[1]
						: rollbacker.getName();

				Rollback rollback = new Rollback(playerName);
				rollback.addReciever(player);
				rollback.rollback();
			} else {
				player.sendMessage(BigBrother.premessage + "usage is "
						+ Color.RED + "/rollback <player>");
			}
			event.setCancelled(true);
		} else if (split[0].equalsIgnoreCase("/bbtp")) {
			if (split.length == 4 && isNumber(split[1]) && isNumber(split[2])
					&& isNumber(split[3])) {
				Teleporter tp = new Teleporter(player, split[1], split[2],
						split[3]);
				tp.teleport();
			} else {
				player.sendMessage(BigBrother.premessage + "usage is "
						+ Color.RED + "/bbtp <x> <y> <z>");
			}
			event.setCancelled(true);
		} else if (split[0].equalsIgnoreCase("/bboptout")) {
			if (BBSettings.optedOut.contains(player.getName())) {
				BBSettings.optedOut.remove(player.getName());
				player.sendMessage(BigBrother.premessage
						+ "You'll be notified of activity.");
			} else {
				BBSettings.optedOut.add(player.getName());
				player.sendMessage(BigBrother.premessage
						+ "You'll no longer receive updates.");
			}
			event.setCancelled(true);
		} else if (split[0].equalsIgnoreCase("/bbhere")) {
			if (split.length == 1) {
				Finder finder = new Finder(player.getLocation());
				finder.setRadius(BBSettings.defaultRadius);
				finder.setReciever(player);
				finder.find();
			} else if (isNumber(split[1])) {
				Finder finder = new Finder(player.getLocation());
				finder.setRadius(Double.parseDouble(split[1]));
				finder.setReciever(player);
				finder.find();
			} else {
				player.sendMessage(BigBrother.premessage + "usage is "
						+ Color.RED + "/bbhere");
				player.sendMessage("or " + Color.RED + "/bbhere <radius>");
			}
			// TODO more bbhere stuff
			event.setCancelled(true);

		} else if (split[0].equalsIgnoreCase("/bbfind")) {
			if (split.length == 4 && isNumber(split[1]) && isNumber(split[2])
					&& isNumber(split[3])) {
				World currentWorld = plugin.getServer().getWorlds()[0];
				// TODO better World Support
				Location loc = new Location(currentWorld,
						Double.parseDouble(split[1]),
						Double.parseDouble(split[2]),
						Double.parseDouble(split[3]));
				Finder finder = new Finder(loc);
				finder.setRadius(BBSettings.defaultRadius);
				finder.setReciever(player);
				finder.find();
			} else if (split.length == 5 && isNumber(split[1])
					&& isNumber(split[2]) && isNumber(split[3])
					&& isNumber(split[4])) {
				World currentWorld = plugin.getServer().getWorlds()[0];
				// TODO better World Support
				Location loc = new Location(currentWorld,
						Double.parseDouble(split[1]),
						Double.parseDouble(split[2]),
						Double.parseDouble(split[3]));
				Finder finder = new Finder(loc);
				finder.setRadius(Double.parseDouble(split[1]));
				finder.setReciever(player);
				finder.find();
			} else {
				player.sendMessage(BigBrother.premessage + "usage is "
						+ Color.RED + "/bbfind <x> <y> <z>");
				player.sendMessage("or " + Color.RED
						+ "/bbfind <x> <y> <z> <radius>");
			}
			event.setCancelled(true);
		}
		if (BBSettings.commands && plugin.watching(player)) {
			Command dataBlock = new Command(player, event.getMessage());
			dataBlock.send();
		}
	}

	public void onPlayerJoin(PlayerEvent event) {
		if (!plugin.haveSeen(event.getPlayer())) {
			plugin.markSeen(event.getPlayer());
			if (BBSettings.autoWatch) {
				plugin.watchPlayer(event.getPlayer());
			}
		}

		if (BBSettings.login && plugin.watching(event.getPlayer())) {
			Login dataBlock = new Login(event.getPlayer());
			dataBlock.send();
		}
	}

	public void onPlayerQuit(PlayerEvent event) {
		if (BBSettings.logout && plugin.watching(event.getPlayer())) {
			Disconnect dataBlock = new Disconnect(event.getPlayer());
			dataBlock.send();
		}
	}

	public void onPlayerTeleport(PlayerMoveEvent event) {
		if (BBSettings.position && plugin.watching(event.getPlayer())) {
			Teleport dataBlock = new Teleport(event.getPlayer(), event.getTo());
			dataBlock.send();
		}
	}

	public void onPlayerChat(PlayerChatEvent event) {
		if (BBSettings.chat && plugin.watching(event.getPlayer())) {
			Chat dataBlock = new Chat(event.getPlayer(), event.getMessage());
			dataBlock.send();
		}
	}

	public static boolean isInteger(String string) {
		try {
			Integer.parseInt(string);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	public static boolean isNumber(String string) {
		try {
			Double.parseDouble(string);
		} catch (Exception e) {
			return false;
		}
		return true;
	}
}
