package com.khorn.terraincontrol.forge.generator;

import com.khorn.terraincontrol.LocalMaterialData;
import com.khorn.terraincontrol.forge.ForgeMaterialData;
import com.khorn.terraincontrol.generator.ChunkBuffer;
import com.khorn.terraincontrol.util.ChunkCoordinate;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
//import net.minecraft.block.state.IBlockState;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
//import net.minecraft.world.chunk.ChunkPrimer;

/**
 * Implementation of {@link ChunkBuffer}. This implementation supports block
 * data, as well as extended ids. It uses a {@code Block[]} array to store
 * blocks internally, just like Minecraft does for chunk generation.
 *
 */
class ForgeChunkBuffer implements ChunkBuffer
{
    private final ChunkCoordinate chunkCoord;
    //private final ChunkPrimer chunkPrimer;
    
    private final Block[] blocks = new Block[ChunkCoordinate.CHUNK_X_SIZE * ChunkCoordinate.CHUNK_Y_SIZE * ChunkCoordinate.CHUNK_Z_SIZE];
    private final byte[] blockData = new byte[ChunkCoordinate.CHUNK_X_SIZE * ChunkCoordinate.CHUNK_Y_SIZE * ChunkCoordinate.CHUNK_Z_SIZE];

    ForgeChunkBuffer(ChunkCoordinate chunkCoord)
    {
        this.chunkCoord = chunkCoord;
        //this.chunkPrimer = new ChunkPrimer();
    }

    @Override
    public ChunkCoordinate getChunkCoordinate()
    {
        return chunkCoord;
    }

    @Override
    public void setBlock(int blockX, int blockY, int blockZ, LocalMaterialData material)
    {
        int arrayPos = (blockX * ChunkCoordinate.CHUNK_X_SIZE + blockZ) * ChunkCoordinate.CHUNK_Y_SIZE + blockY;
        blocks[arrayPos] = ((ForgeMaterialData) material).internalBlock();
        blockData[arrayPos] = material.getBlockData();
    	
        //chunkPrimer.setBlockState(blockX, blockY, blockZ, ((ForgeMaterialData) material).internalBlock());
    }

    @Override
    public LocalMaterialData getBlock(int blockX, int blockY, int blockZ)
    {
        int arrayPos = (blockX * ChunkCoordinate.CHUNK_X_SIZE + blockZ) * ChunkCoordinate.CHUNK_Y_SIZE + blockY;
        Block block = blocks[arrayPos];
        if (block == null)
        {
            block = Blocks.air;
        }
        byte data = blockData[arrayPos];
        return ForgeMaterialData.ofMinecraftBlock(block, data);
    	
    	/*
        IBlockState blockState = chunkPrimer.getBlockState(blockX, blockY, blockZ);
        return ForgeMaterialData.ofMinecraftBlockState(blockState);
        */
    }

    /**
     * Creates a Minecraft chunk of the data of this chunk buffer.
     *
     * @param world
     *            The world the chunk will be in.
     * @return The chunk.
     */
    Chunk toChunk(World world)
    {
    	return new Chunk(world, blocks, blockData, chunkCoord.getChunkX(), chunkCoord.getChunkZ());
    	
        //return new Chunk(world, chunkPrimer, chunkCoord.getChunkX(), chunkCoord.getChunkZ());
    }

}
