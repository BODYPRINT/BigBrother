package me.taylorkelly.bigbrother.datablock;

import java.util.ArrayList;

import org.bukkit.*;
import org.bukkit.craftbukkit.CraftWorld;

public class PlacedBlock extends BBDataBlock {
    private ArrayList<BBDataBlock> bystanders;

    public PlacedBlock(Player player, Block block) {
        // TODO Better World support
        super(player.getName(), BLOCK_PLACED, 0, block.getX(), block.getY(), block.getZ(), block.getTypeID(), block.getData() + "");
        bystanders = new ArrayList<BBDataBlock>();
        // TODO snow check once it gets fixed
    }

    public void send() {
        for (BBDataBlock block : bystanders) {
            block.send();
        }
        super.send();
    }

    private PlacedBlock(String player, int world, int x, int y, int z, int type, String data) {
        super(player, BLOCK_PLACED, world, x, y, z, type, data);
    }

    public void rollback(Server server) {
        World worldy = server.getWorlds()[world];
        if (!((CraftWorld) worldy).getHandle().A.a(x >> 4, z >> 4)) {
            ((CraftWorld) worldy).getHandle().A.d(x >> 4, z >> 4);
        }

        worldy.getBlockAt(x, y, z).setTypeID(0);
    }

    public void redo(Server server) {
        World worldy = server.getWorlds()[world];
        if (!((CraftWorld) worldy).getHandle().A.a(x >> 4, z >> 4)) {
            ((CraftWorld) worldy).getHandle().A.d(x >> 4, z >> 4);
        }

        byte blockData = Byte.parseByte(data);
        worldy.getBlockAt(x, y, z).setTypeID(type);
        worldy.getBlockAt(x, y, z).setData(blockData);
    }

    public static BBDataBlock getBBDataBlock(String player, int world, int x, int y, int z, int type, String data) {
        return new PlacedBlock(player, world, x, y, z, type, data);
    }

}
