package com.khorn.terraincontrol.forge.generator;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.generator.biome.OutputType;
import com.khorn.terraincontrol.generator.biome.VanillaBiomeGenerator;

//import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.BiomeGenBase;
//import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.biome.WorldChunkManager;

/**
 * A biome generator that gets its information from Mojang's WorldChunkManager.
 *
 * <p>
 * This can be somewhat dangerous, because a subclass for WorldChunkManager,
 * {@link TCBiomeProvider}, gets its information from a BiomeGenerator. This
 * would cause infinite recursion. To combat this, a check has been added to
 * {@link #setBiomeProvider(BiomeProvider)}.
 *
 */
public class ForgeVanillaBiomeGenerator extends VanillaBiomeGenerator {

    private BiomeGenBase[] biomeGenBaseArray;
    private WorldChunkManager worldChunkManager;

    public ForgeVanillaBiomeGenerator(LocalWorld world)
    {
        super(world);
    }

    @Override
    public int[] getBiomesUnZoomed(int[] biomeArray, int x, int z, int x_size, int z_size, OutputType outputType)
    {
        biomeGenBaseArray = worldChunkManager.getBiomesForGeneration(biomeGenBaseArray, x, z, x_size, z_size);
        if (biomeArray == null || biomeArray.length < x_size * z_size)
            biomeArray = new int[x_size * z_size];
        for (int i = 0; i < x_size * z_size; i++)
        	biomeArray[i] = biomeGenBaseArray[i].biomeID;
        return biomeArray;
    }

    @Override
    public float[] getRainfall(float[] paramArrayOfFloat, int x, int z, int x_size, int z_size)
    {
        return worldChunkManager.getRainfall(paramArrayOfFloat, x, z, x_size, z_size);
    }
    
    @Override
    public int[] getBiomes(int[] biomeArray, int x, int z, int x_size, int z_size, OutputType outputType)
    {
        biomeGenBaseArray = worldChunkManager.getBiomeGenAt(biomeGenBaseArray, x, z, x_size, z_size, true);
        if (biomeArray == null || biomeArray.length < x_size * z_size)
            biomeArray = new int[x_size * z_size];
        for (int i = 0; i < x_size * z_size; i++)
            biomeArray[i] = biomeArray[i] = biomeGenBaseArray[i].biomeID;
        return biomeArray;
    }

    @Override
    public int getBiome(int x, int z)
    {
    	return worldChunkManager.getBiomeGenAt(x, z).biomeID;
    }

    @Override
    public void cleanupCache()
    {
        worldChunkManager.cleanupCache();
    }

    @Override
    public boolean canGenerateUnZoomed()
    {
        return true;
    }

    /**
     * Sets the vanilla {@link BiomeProvider}. Must be called before generating
     * any biomes.
     *
     * @param biomeProvider The vanilla {@link BiomeProvider}.
     */
    public void setWorldChunkManager(WorldChunkManager biomeProvider)
    {
        if (biomeProvider instanceof TCWorldChunkManager)
        {
            // TCBiomeProvider is unusable, as it just asks the
            // BiomeGenerator for the biomes, creating an infinite loop
            throw new IllegalArgumentException(getClass() + " expects a vanilla BiomeProvider, " + biomeProvider.getClass() + " given");
        }
        this.worldChunkManager = biomeProvider;
    }

}
