package me.taylorkelly.bigbrother.datablock.explosions;

import java.util.List;

import me.taylorkelly.bigbrother.datablock.BBDataBlock;

import org.bukkit.Location;
import org.bukkit.block.Block;

public class MiscExplosion extends Explosion {

    public MiscExplosion(String player, Block block, String world) {
        super(Action.MISC_EXPLOSION, player, block, world);
    }

    public MiscExplosion(Block block, String world) {
        super(Action.MISC_EXPLOSION, ENVIRONMENT, block, world);
    }

    protected Explosion newInstance(String player, Block block) {
        return new MiscExplosion(player, block, block.getWorld().getName());
    }

    public static void create(Location location, List<Block> blockList, String world) {
        for(Block block: blockList) {
            BBDataBlock dataBlock = new MiscExplosion(ENVIRONMENT, block, world);
            dataBlock.send();
        }
    }

    private MiscExplosion(String player, String world, int x, int y, int z, int type, String data) {
        super(player, Action.MISC_EXPLOSION, world, x, y, z, type, data);
    }

    public static BBDataBlock getBBDataBlock(String player, String world, int x, int y, int z, int type, String data) {
        return new MiscExplosion(player, world, x, y, z, type, data);
    }

}
