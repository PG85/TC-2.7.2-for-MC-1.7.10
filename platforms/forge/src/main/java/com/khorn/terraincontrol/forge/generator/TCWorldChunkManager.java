package com.khorn.terraincontrol.forge.generator;

import com.khorn.terraincontrol.forge.ForgeBiome;
import com.khorn.terraincontrol.forge.ForgeWorld;
import com.khorn.terraincontrol.generator.biome.BiomeGenerator;
import com.khorn.terraincontrol.generator.biome.OutputType;
import com.khorn.terraincontrol.util.minecraftTypes.DefaultBiome;

import net.minecraft.world.ChunkPosition;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.WorldChunkManager;
import net.minecraft.world.gen.structure.MapGenVillage;

import java.util.List;
import java.util.Random;

/**
 * Minecraft's biome generator class is {@link BiomeProvider}, we use
 * {@link BiomeGenerator}. This class provides a bridge between the two,
 * allowing us to use custom biome generators.
 */
public class TCWorldChunkManager extends WorldChunkManager
{
    private final BiomeGenerator biomeGenerator;
    private final ForgeWorld localWorld;

    public TCWorldChunkManager(ForgeWorld world, BiomeGenerator biomeGenerator)
    {
        this.localWorld = world;
        this.biomeGenerator = biomeGenerator;
    }

    @Override
    public BiomeGenBase getBiomeGenAt(int x, int z)
    {
    	return localWorld.getBiomeById(biomeGenerator.getBiome(x, z)).getHandle();
        //return localWorld.getBiomeById(biomeGenerator.getBiome(blockPos.getX(), blockPos.getZ())).getHandle();
    }

    /*
    @Override
    public BiomeGenBase getBiomeGenerator(BlockPos pos, BiomeGenBase defaultOption)
    {
        ForgeBiome biome = localWorld.getBiomeByIdOrNull(biomeGenerator.getBiome(pos.getX(), pos.getZ()));
        if (biome != null)
        {
            return biome.getHandle();
        }
        return defaultOption;
    }
    */

    @Override
    public BiomeGenBase[] getBiomesForGeneration(BiomeGenBase[] paramArrayOfBiomeBase, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
    {
        int[] arrayOfInt = this.biomeGenerator.getBiomesUnZoomed(null, paramInt1, paramInt2, paramInt3, paramInt4,
                OutputType.DEFAULT_FOR_WORLD);
        if (paramArrayOfBiomeBase == null || paramArrayOfBiomeBase.length < arrayOfInt.length)
        {
            paramArrayOfBiomeBase = new BiomeGenBase[arrayOfInt.length];
        }

        for (int i = 0; i < paramInt3 * paramInt4; i++)
        {
            paramArrayOfBiomeBase[i] = localWorld.getBiomeById(arrayOfInt[i]).getHandle();
        }

        return paramArrayOfBiomeBase;
    }

    @Override
    public BiomeGenBase[] getBiomeGenAt(BiomeGenBase[] paramArrayOfBiomeBase, int paramInt1, int paramInt2, int paramInt3, int paramInt4, boolean paramBoolean)
    {
        int[] arrayOfInt = this.biomeGenerator.getBiomes(null, paramInt1, paramInt2, paramInt3, paramInt4, OutputType.DEFAULT_FOR_WORLD);
        if (paramArrayOfBiomeBase == null || paramArrayOfBiomeBase.length < arrayOfInt.length)
        {
            paramArrayOfBiomeBase = new BiomeGenBase[arrayOfInt.length];
        }

        for (int i = 0; i < paramInt3 * paramInt4; i++)
        {
            paramArrayOfBiomeBase[i] = localWorld.getBiomeById(arrayOfInt[i]).getHandle();
        }

        return paramArrayOfBiomeBase;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean areBiomesViable(int paramInt1, int paramInt2, int paramInt3, List paramList)
    {
        // Hack for StructureVillagePieces.getNextComponentVillagePath(..)
        // (The alternative would be to completely override the village spawn
        // code)
        if (paramList == MapGenVillage.villageSpawnBiomes)
        {
            paramList = localWorld.villageGen.villageSpawnBiomes;
        }

        int i = paramInt1 - paramInt3 >> 2;
        int j = paramInt2 - paramInt3 >> 2;
        int k = paramInt1 + paramInt3 >> 2;
        int m = paramInt2 + paramInt3 >> 2;

        int n = k - i + 1;
        int i1 = m - j + 1;
        BiomeGenBase[] arrayOfInt = this.getBiomesForGeneration(null, i, j, n, i1);
        for (int i2 = 0; i2 < n * i1; i2++)
        {
            BiomeGenBase localBiomeBase = arrayOfInt[i2];
            if (!paramList.contains(localBiomeBase))
                return false;
        }

        return true;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public ChunkPosition findBiomePosition(int paramInt1, int paramInt2, int paramInt3, List paramList, Random paramRandom)
    {
        int i = paramInt1 - paramInt3 >> 2;
        int j = paramInt2 - paramInt3 >> 2;
        int k = paramInt1 + paramInt3 >> 2;
        int m = paramInt2 + paramInt3 >> 2;

        int n = k - i + 1;
        int i1 = m - j + 1;
        int[] arrayOfInt = this.biomeGenerator.getBiomesUnZoomed(null, i, j, n, i1, OutputType.DEFAULT_FOR_WORLD);
        ChunkPosition blockPos = null;
        int i2 = 0;
        for (int i3 = 0; i3 < arrayOfInt.length; i3++)
        {
            if (arrayOfInt[i3] >= DefaultBiome.values().length)
                continue;
            int i4 = i + i3 % n << 2;
            int i5 = j + i3 / n << 2;
            BiomeGenBase localBiomeBase = BiomeGenBase.getBiome(arrayOfInt[i3]);
            if ((!paramList.contains(localBiomeBase)) || ((blockPos != null) && (paramRandom.nextInt(i2 + 1) != 0)))
                continue;
            blockPos = new ChunkPosition(i4, 0, i5);
            i2++;
        }

        return blockPos;
    }

    @Override
    public void cleanupCache()
    {
        this.biomeGenerator.cleanupCache();
    }
}