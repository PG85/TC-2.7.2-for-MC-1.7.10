// Note for PG: this is the old (1.7.10) code

package com.khorn.terraincontrol.forge;

import com.khorn.terraincontrol.BiomeIds;
import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.configuration.standard.PluginStandardValues;
import com.khorn.terraincontrol.forge.generator.BiomeGenCustom;
import com.khorn.terraincontrol.util.helpers.StringHelper;
import net.minecraft.util.ResourceLocation;
//import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.BiomeGenBase;
//import net.minecraftforge.fml.common.registry.FMLControlledNamespacedRegistry;
//import net.minecraftforge.fml.common.registry.GameData;

public class ForgeBiome implements LocalBiome
{
    private final BiomeGenCustom biomeBase;
    private final BiomeIds biomeIds;
    private final BiomeConfig biomeConfig;

    /**
     * Creates a new biome with the given name and id. Also registers it in
     * Minecraft's biome array, but only when the biome is not virtual.
     * 
     * @param biomeConfig The config of the biome.
     * @param biomeIds    The ids of the biome.
     * @return The registered biome.
     */
    public static ForgeBiome createBiome(BiomeConfig biomeConfig, BiomeIds biomeIds)
    {
        // Store the previous biome in a variable
        BiomeGenBase previousBiome = BiomeGenBase.getBiome(biomeIds.getSavedId());

        // Register new biome
        ForgeBiome biome = new ForgeBiome(biomeConfig, new BiomeGenCustom(biomeConfig, biomeIds));

        // Restore settings of the previous biome
        if (previousBiome != null)
        {
            biome.biomeBase.copyBiome(previousBiome);
        }

        return biome;
    }

    private ForgeBiome(BiomeConfig biomeConfig, BiomeGenCustom biome)
    {
        this.biomeBase = biome;
        this.biomeIds = new BiomeIds(biome.generationId, biome.biomeID);
        this.biomeConfig = biomeConfig;
    }

    @Override
    public void setEffects()
    {
        biomeBase.setEffects(biomeConfig);
    }
    
    @Override
    public boolean isCustom()
    {
        return true;
    }

    @Override
    public String getName()
    {
        return biomeBase.biomeName;
    }

    public BiomeGenCustom getHandle()
    {
        return biomeBase;
    }

    @Override
    public BiomeIds getIds()
    {
        return biomeIds;
    }

    @Override
    public float getTemperatureAt(int x, int y, int z)
    {
    	return biomeBase.getFloatTemperature(x, y, z);
    }

    @Override
    public BiomeConfig getBiomeConfig()
    {
        return biomeConfig;
    }
    
    @Override
    public String toString()
    {
        return getName() + "[" + biomeIds + "]";
    }
}
