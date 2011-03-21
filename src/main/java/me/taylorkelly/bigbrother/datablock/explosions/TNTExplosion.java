package me.taylorkelly.bigbrother.datablock.explosions;

import java.util.List;

import me.taylorkelly.bigbrother.datablock.BBDataBlock;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

public class TNTExplosion extends Explosion {

    public TNTExplosion(String player, Block block, String world) {
        super(Action.TNT_EXPLOSION, player, block, world);
    }

    public TNTExplosion(Block block, String world) {
        super(Action.TNT_EXPLOSION, ENVIRONMENT, block, world);
    }

    @Override
    protected Explosion newInstance(String player, Block block) {
        return new TNTExplosion(player, block, block.getWorld().getName());
    }

    private TNTExplosion(String player, String world, int x, int y, int z, int type, String data) {
        super(player, Action.TNT_EXPLOSION, world, x, y, z, type, data);
    }

    public static BBDataBlock getBBDataBlock(String player, String world, int x, int y, int z, int type, String data) {
        return new TNTExplosion(player, world, x, y, z, type, data);
    }
}
