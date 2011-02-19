package me.taylorkelly.bigbrother.datablock;

import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class ChestOpen extends BBDataBlock {

    public ChestOpen(Player player, Block block) {
        // TODO Better World support
        super(player.getName(), Action.OPEN_CHEST, 0, block.getX(), block.getY(), block.getZ(), 54, "");
    }

    private ChestOpen(String player, int world, int x, int y, int z, int type, String data) {
        super(player, Action.OPEN_CHEST, world, x, y, z, type, data);
    }

    public void rollback(Server server) {
    }

    public void redo(Server server) {
    }

    public static BBDataBlock getBBDataBlock(String player, int world, int x, int y, int z, int type, String data) {
        return new ChestOpen(player, world, x, y, z, type, data);
    }

}
