package com.khorn.terraincontrol.forge.generator;

import static com.khorn.terraincontrol.util.ChunkCoordinate.CHUNK_X_SIZE;
import static com.khorn.terraincontrol.util.ChunkCoordinate.CHUNK_Z_SIZE;

import com.khorn.terraincontrol.configuration.ConfigProvider;
import com.khorn.terraincontrol.configuration.WorldConfig;
import com.khorn.terraincontrol.forge.ForgeWorld;
import com.khorn.terraincontrol.generator.ChunkProviderTC;
import com.khorn.terraincontrol.generator.ObjectSpawner;
import com.khorn.terraincontrol.generator.biome.OutputType;
import com.khorn.terraincontrol.util.ChunkCoordinate;

import net.minecraft.block.BlockSand;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.world.ChunkPosition;
//import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.BiomeGenBase.SpawnListEntry;
import net.minecraft.world.chunk.Chunk;
//import net.minecraft.world.chunk.IChunkGenerator;

import net.minecraft.world.chunk.IChunkProvider;

import java.util.List;

public class ChunkProvider implements IChunkProvider
{

    private ForgeWorld world;
    private World worldHandle;
    private boolean TestMode = false;

    private ChunkProviderTC generator;
    private ObjectSpawner spawner;

    /** 
     * Used in {@link #fillBiomeArray(Chunk)}, to avoid creating
     * new int arrays.
     */
    private int[] biomeIntArray;

    public ChunkProvider(ForgeWorld _world)
    {
        this.world = _world;
        this.worldHandle = _world.getWorld();

        this.TestMode = world.getConfigs().getWorldConfig().ModeTerrain == WorldConfig.TerrainMode.TerrainTest;

        this.generator = new ChunkProviderTC(this.world.getConfigs(), this.world);
        this.spawner = new ObjectSpawner(this.world.getConfigs(), this.world);

    }

    @Override
    public boolean chunkExists(int i, int i1)
    {
        return true;
    }
    
    @Override
    public Chunk provideChunk(int chunkX, int chunkZ)
    {
        ChunkCoordinate chunkCoord = ChunkCoordinate.fromChunkCoords(chunkX, chunkZ);
        ForgeChunkBuffer chunkBuffer = new ForgeChunkBuffer(chunkCoord);
        this.generator.generate(chunkBuffer);

        Chunk chunk = chunkBuffer.toChunk(this.worldHandle);
        fillBiomeArray(chunk);
        chunk.generateSkylightMap();

        return chunk;
    }

    /**
     * Fills the biome array of a chunk with the proper saved ids (no
     * generation ids).
     * @param chunk The chunk to fill the biomes of.
     */
    private void fillBiomeArray(Chunk chunk)
    {
        byte[] chunkBiomeArray = chunk.getBiomeArray();
        ConfigProvider configProvider = world.getConfigs();
        biomeIntArray = world.getBiomeGenerator().getBiomes(biomeIntArray,
                chunk.xPosition * CHUNK_X_SIZE, chunk.zPosition * CHUNK_Z_SIZE,
                CHUNK_X_SIZE, CHUNK_Z_SIZE, OutputType.DEFAULT_FOR_WORLD);

        for (int i = 0; i < chunkBiomeArray.length; i++)
        {
            int generationId = biomeIntArray[i];
            chunkBiomeArray[i] = (byte) configProvider.getBiomeByIdOrNull(generationId).getIds().getSavedId();
        }
    }

    @Override
    public Chunk loadChunk(int i, int i1)
    {
        return provideChunk(i, i1);
    }
    
    @Override
    public void populate(IChunkProvider ChunkProvider, int chunkX, int chunkZ)
    {
        if (this.TestMode)
            return;
        BlockSand.fallInstantly = true;
        this.spawner.populate(ChunkCoordinate.fromChunkCoords(chunkX, chunkZ));
        BlockSand.fallInstantly = false;
    }
    
    @Override
    public boolean saveChunks(boolean b, IProgressUpdate il)
    {
        return true;
    }

    @Override
    public boolean unloadQueuedChunks()
    {
        return false;
    }

    @Override
    public boolean canSave()
    {
        return true;
    }

    @Override
    public String makeString()
    {
        return "TerrainControlLevelSource";
    }

    @Override
    public List<?> getPossibleCreatures(EnumCreatureType paramaca, int x, int y, int z)
    {
        WorldConfig worldConfig = this.world.getConfigs().getWorldConfig();
        BiomeGenBase biomeBase = this.worldHandle.getBiomeGenForCoords(x,z);       

        if (worldConfig.rareBuildingsEnabled)
        {
            if (paramaca == EnumCreatureType.monster && this.world.rareBuildingGen.isSwampHutAtLocation(x,z))
            {
                return this.world.rareBuildingGen.getMonsterSpawnList();
            }
        }
        /*
        if (worldConfig.oceanMonumentsEnabled)
        {
            if (paramaca == EnumCreatureType.monster && this.world.oceanMonumentGen.isPositionInStructure(this.worldHandle, blockPos))
            {
                return this.world.oceanMonumentGen.getMonsterSpawnList();
            }
        }
        */
        return biomeBase.getSpawnableList(paramaca);
    }

    @Override
    public ChunkPosition func_147416_a(World worldIn, String structureName, int x, int y, int z)
    {
        // Gets the nearest stronghold
        if (("Stronghold".equals(structureName)) && (this.world.strongholdGen != null))
        {
            return this.world.strongholdGen.func_151545_a(worldIn, x, y, z);
        }
        return null;
    }

    @Override
    public int getLoadedChunkCount()
    {
        return 0;
    }
    
    @Override
    public void recreateStructures(int chunkX, int chunkZ)
    {
        // recreateStructures
        WorldConfig worldConfig = world.getConfigs().getWorldConfig();
        if (worldConfig.mineshaftsEnabled)
        {
        	world.mineshaftGen.func_151539_a(this, world.getWorld(), chunkX, chunkZ, null);
        }
        if (worldConfig.villagesEnabled)
        {
        	world.villageGen.func_151539_a(this, world.getWorld(), chunkX, chunkZ, null);
        }
        if (worldConfig.strongholdsEnabled)
        {
        	world.strongholdGen.func_151539_a(this, world.getWorld(), chunkX, chunkZ, null);
        }
        if (worldConfig.rareBuildingsEnabled)
        {
        	world.rareBuildingGen.func_151539_a(this, world.getWorld(), chunkX, chunkZ, null);
        }
        if (worldConfig.netherFortressesEnabled)
        {
        	world.netherFortressGen.func_151539_a(this, world.getWorld(), chunkX, chunkZ, null);
        }
        /*
        if (worldConfig.oceanMonumentsEnabled)
        {
            world.oceanMonumentGen.generate(world.getWorld(), chunkX, chunkZ, null);
        }
        */
    }
    
    @Override
    public void saveExtraData()
    {
        // Empty, just like Minecraft's ChunkProviderGenerate
    } 

}
