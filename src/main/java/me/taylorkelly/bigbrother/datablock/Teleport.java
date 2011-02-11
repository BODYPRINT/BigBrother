package me.taylorkelly.bigbrother.datablock;

import org.bukkit.*;
import org.bukkit.entity.Player;

public class Teleport extends BBDataBlock {
	public Teleport(Player player, Location to) {
		//TODO Better World support
		super(player.getName(), TELEPORT, 0, to.getBlockX(), to.getBlockY(), to.getBlockZ(), 0, "");
	}
	
	public static BBDataBlock getBBDataBlock(String player, int world, int x, int y, int z, int type, String data) {
		return new Teleport(player, world, x, y, z, type, data);
	}

	private Teleport(String player, int world, int x, int y, int z, int type, String data) {
		super(player, TELEPORT, world, x, y, z, type, data);
	}

	public void rollback(Server server) {}
	public void redo(Server server) {}
}
