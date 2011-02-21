package me.taylorkelly.bigbrother.datablock.explosions;

import java.util.List;

import me.taylorkelly.bigbrother.datablock.BBDataBlock;

import org.bukkit.Location;
import org.bukkit.block.Block;

public class TNTExplosion extends Explosion {

    public TNTExplosion(String player, Block block, String world) {
        super(Action.TNT_EXPLOSION, player, block, world);
    }

    public TNTExplosion(Block block, String world) {
        super(Action.TNT_EXPLOSION, ENVIRONMENT, block, world);
    }

    protected Explosion newInstance(String player, Block block) {
        return new TNTExplosion(player, block, block.getWorld().getName());
    }

    public static void create(Location location, List<Block> blockList, String world) {
        for(Block block: blockList) {
            BBDataBlock dataBlock = new TNTExplosion(ENVIRONMENT, block, world);
            dataBlock.send();
        }
    }

    private TNTExplosion(String player, String world, int x, int y, int z, int type, String data) {
        super(player, Action.TNT_EXPLOSION, world, x, y, z, type, data);
    }

    public static BBDataBlock getBBDataBlock(String player, String world, int x, int y, int z, int type, String data) {
        return new TNTExplosion(player, world, x, y, z, type, data);
    }

    public static void create(List<Block> blockList, String world) {
        for(Block block: blockList) {
            BBDataBlock dataBlock = new TNTExplosion(ENVIRONMENT, block, world);
            dataBlock.send();
        }
    }

}
