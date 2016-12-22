package com.khorn.terraincontrol.forge.generator.structure;

import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.forge.util.WorldHelper;
import com.khorn.terraincontrol.util.minecraftTypes.StructureNames;

import net.minecraft.world.gen.structure.MapGenStructure;
import net.minecraft.world.gen.structure.StructureMineshaftStart;
import net.minecraft.world.gen.structure.StructureStart;

public class MineshaftGen extends MapGenStructure
{
    @Override
    protected boolean canSpawnStructureAtCoords(int chunkX, int chunkZ)
    {
        if (rand.nextInt(80) < Math.max(Math.abs(chunkX), Math.abs(chunkZ)))
        {
            LocalWorld world = WorldHelper.toLocalWorld(this.worldObj);
            LocalBiome biome = world.getBiome(chunkX * 16 + 8, chunkZ * 16 + 8);
            if (rand.nextDouble() * 100.0 < biome.getBiomeConfig().mineshaftsRarity)
            {
                return true;
            }
        }

        return false;
    }

    @Override
    protected StructureStart getStructureStart(int par1, int par2)
    {
        return new StructureMineshaftStart(this.worldObj, this.rand, par1, par2);
    }

    @Override
    public String func_143025_a()
    {
        return StructureNames.MINESHAFT;
    }
}
