package com.khorn.terraincontrol.forge.util;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;

import net.minecraft.world.World;

public abstract class WorldHelper
{

    /**
     * Returns the LocalWorld of the Minecraft world. Returns null if there is
     * no world.
     *
     * @param world The Minecraft world.
     * @return The LocalWorld, or null if there is none.
     */
    public static LocalWorld toLocalWorld(World world)
    {
        String worldName = world.getSaveHandler().getWorldDirectoryName();
        return TerrainControl.getWorld(worldName);
    }

    private WorldHelper()
    {
    }

}
