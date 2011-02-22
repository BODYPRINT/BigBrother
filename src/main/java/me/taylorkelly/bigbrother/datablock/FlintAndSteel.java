package me.taylorkelly.bigbrother.datablock;

import org.bukkit.Server;
import org.bukkit.block.Block;

public class FlintAndSteel extends BBDataBlock {

    public FlintAndSteel(String player, Block block, String world) {
        super(player, Action.FLINT_AND_STEEL, world, block.getX(), block.getY(), block.getZ(), block.getTypeId(), "");
    }

    private FlintAndSteel(String player, String world, int x, int y, int z, int type, String data) {
        super(player, Action.FLINT_AND_STEEL, world, x, y, z, type, data);
    }

    public void rollback(Server server) {
    }

    public void redo(Server server) {
    }

    public static BBDataBlock getBBDataBlock(String player, String world, int x, int y, int z, int type, String data) {
        return new FlintAndSteel(player, world, x, y, z, type, data);
    }

}
