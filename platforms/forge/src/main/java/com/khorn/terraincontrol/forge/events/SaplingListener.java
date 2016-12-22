package com.khorn.terraincontrol.forge.events;

import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.LocalMaterialData;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.exception.BiomeNotFoundException;
import com.khorn.terraincontrol.forge.util.WorldHelper;
import com.khorn.terraincontrol.generator.resource.SaplingGen;
import com.khorn.terraincontrol.generator.resource.SaplingType;
import com.khorn.terraincontrol.util.minecraftTypes.DefaultMaterial;

import cpw.mods.fml.common.eventhandler.Event.Result;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
//import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
//import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.BonemealEvent;
import net.minecraftforge.event.terraingen.SaplingGrowTreeEvent;
//import net.minecraftforge.fml.common.eventhandler.Event.Result;
//import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;



import java.util.Random;

public class SaplingListener
{
    private static class SaplingGrower
    {
        private final LocalWorld world;
        private final LocalMaterialData material;
        private final SaplingType saplingType;
        
        int x;
        int y;
        int z;

        private SaplingGrower(LocalWorld world, int x, int y, int z)
        {
        	this.x = x;
        	this.y = y;
        	this.z = z;
            this.world = world;
            this.material = world.getMaterial(this.x, this.y, this.z);
            //this.blockPos = blockPos;

            // Check whether block is a sapling
            if (!this.material.isMaterial(DefaultMaterial.SAPLING))
            {
                this.saplingType = null;
                return;
            }

            // Try to find big (2x2) sapling
            SaplingType bigSaplingType = getBigSaplingType(this.material.getBlockData());
            if (bigSaplingType != null)
            {
                if (findFourSaplings())
                {
                    this.saplingType = bigSaplingType;
                    return;
                }
            }

            // Try to find small sapling
            this.saplingType = getSmallSaplingType(this.material.getBlockData());
        }

        /**
         * Gets whether the saplings are placed in a 2x2 pattern. If successful,
         * it adjusts {@link SaplingGrower#x} and {@link SaplingGrower#z} to
         * represent the top left sapling (with the lowest x and z).
         * 
         * @return Whether the saplings are placed in a 2x2 pattern.
         */
        private boolean findFourSaplings()
        {
            for (int treeOffsetX = 0; treeOffsetX >= -1; --treeOffsetX)
            {
                for (int treeOffsetZ = 0; treeOffsetZ >= -1; --treeOffsetZ)
                {
                    if (isSameSapling(material, world.getMaterial(this.x + treeOffsetX, this.y, this.z + treeOffsetZ))
                            && isSameSapling(material, world.getMaterial(this.x + treeOffsetX + 1, this.y, this.z + treeOffsetZ))
                            && isSameSapling(material, world.getMaterial(this.x + treeOffsetX, this.y, this.z + treeOffsetZ + 1))
                            && isSameSapling(material, world.getMaterial(this.x + treeOffsetX + 1, this.y, this.z + treeOffsetZ + 1)))
                    {
                        // Found! Adjust internal position
                        //blockPos = blockPos.add(treeOffsetX, 0, treeOffsetZ);
                    	x += treeOffsetX;
                    	z += treeOffsetZ;
                        return true;
                    }
                }
            }
            return false;
        }
        
        /**
         * Checks if the sapling types are the same, ignoring growth stages.
         * @param sapling1 The first material to compare.
         * @param sapling2 The second material to compare.
         * @return True if both materials are saplings and are of the same type.
         */
        private boolean isSameSapling(LocalMaterialData sapling1, LocalMaterialData sapling2)
        {
            return sapling1.isMaterial(DefaultMaterial.SAPLING) 
                    && sapling2.isMaterial(DefaultMaterial.SAPLING) 
                    &&  sapling1.getBlockData() % 8 == sapling2.getBlockData() % 8;
        }

        /**
         * Gets the sapling type, based on the assumption that the sapling is
         * not placed in a 2x2 pattern.
         * 
         * @param data
         *            The block data of the sapling block.
         * @return The sapling type, or null if not found.
         */
        private SaplingType getSmallSaplingType(int data)
        {
            switch (data % 8)
            { // % 8 makes it ignore growth stage
                case 0:
                    return SaplingType.Oak;
                case 1:
                    return SaplingType.Redwood;
                case 2:
                    return SaplingType.Birch;
                case 3:
                    return SaplingType.SmallJungle;
                case 4:
                    return SaplingType.Acacia;
            }
            return null;
        }

        /**
         * Gets the sapling type, based on the assumption that the saplings must
         * be placed in a 2x2 pattern. Will never return one of the smaller
         * sapling types.
         * 
         * @param data
         *            The block data of the sapling block.
         * @return The sapling type, or null if not found.
         */
        private SaplingType getBigSaplingType(int data)
        {
            switch (data % 8)
            { // % 8 makes it ignore growth stage
                case 1:
                    return SaplingType.HugeRedwood;
                case 3:
                    return SaplingType.BigJungle;
                case 5:
                    return SaplingType.DarkOak;
            }
            return null;
        }
    }

    @SubscribeEvent
    public void onSaplingGrow(SaplingGrowTreeEvent event)
    {
        World world = event.world;
        LocalWorld localWorld = WorldHelper.toLocalWorld(world);
        //BlockPos blockPos = event.pos;

        int x = event.x;
        int y = event.y;
        int z = event.z;
        
        if (localWorld == null)
        {
            // World not managed by Terrain Control
            return;
        }

        SaplingGrower saplingGrower = new SaplingGrower(localWorld, x, y, z);
        
        if (saplingGrower.saplingType == null)
        {
            // Unsupported sapling
            return;
        }

        // Get the sapling generator
        SaplingGen saplingGen = this.getSaplingGen(localWorld, saplingGrower.saplingType, saplingGrower.x, saplingGrower.y, saplingGrower.z);
        if (saplingGen == null)
        {
            // No sapling generator set for this sapling
            return;
        }

        // When we have reached this point, we know that we have to handle the
        // event ourselves
        // So cancel it
        event.setResult(Result.DENY);

        // Remove saplings
        //IBlockState air = Blocks.air.getDefaultState();
        boolean wideTrunk = saplingGrower.saplingType.requiresFourSaplings();
        if (wideTrunk)
        {
            world.setBlockToAir(saplingGrower.x, saplingGrower.y, saplingGrower.z);
            world.setBlockToAir(saplingGrower.x + 1, saplingGrower.y, saplingGrower.z);
            world.setBlockToAir(saplingGrower.x, saplingGrower.y, saplingGrower.z + 1);
            world.setBlockToAir(saplingGrower.x + 1, saplingGrower.y, saplingGrower.z + 1);
        } else {
            world.setBlockToAir(saplingGrower.x, saplingGrower.y, saplingGrower.z);
        }

        // Try ten times to grow sapling
        boolean saplingGrown = false;
        Random random = new Random();
        for (int i = 0; i < 10; i++)
        {
            if (saplingGen.growSapling(localWorld, random, wideTrunk, saplingGrower.x,
                    saplingGrower.y, saplingGrower.z))
            {
                saplingGrown = true;
                break;
            }
        }

        if (!saplingGrown)
        {
            // Restore sapling
            int saplingX = saplingGrower.x;
            int saplingY = saplingGrower.y;
            int saplingZ = saplingGrower.z;
            if (saplingGrower.saplingType.requiresFourSaplings())
            {
                localWorld.setBlock(saplingX, saplingY, saplingZ, saplingGrower.material);
                localWorld.setBlock(saplingX + 1, saplingY, saplingZ, saplingGrower.material);
                localWorld.setBlock(saplingX, saplingY, saplingZ + 1, saplingGrower.material);
                localWorld.setBlock(saplingX + 1, saplingY, saplingZ + 1, saplingGrower.material);
            } else {
                localWorld.setBlock(saplingX, saplingY, saplingZ, saplingGrower.material);
            }
        }

    }

    @SubscribeEvent
    public void onBonemealUse(BonemealEvent event)
    {
        LocalWorld localWorld = WorldHelper.toLocalWorld(event.world);
        if (localWorld == null)
        {
            // World not managed by Terrain Control
            return;
        }

        // Get sapling gen
        SaplingGen gen = null;
        if (event.block == Blocks.red_mushroom_block)
        {
            gen = getSaplingGen(localWorld, SaplingType.RedMushroom, event.x, event.y, event.z);
        } else if (event.block == Blocks.brown_mushroom_block)
        {
            gen = getSaplingGen(localWorld, SaplingType.BrownMushroom, event.x, event.y, event.z);
        }
        if (gen == null)
        {
            // No sapling gen specified for this type
            return;
        }

        // Generate mushroom
        event.setResult(Result.ALLOW);
        event.world.setBlockToAir(event.x, event.y, event.z);

        boolean mushroomGrown = false;
        Random random = new Random();
        for (int i = 0; i < 10; i++)
        {
            if (gen.growSapling(localWorld, random, false, event.x, event.y, event.z))
            {
                mushroomGrown = true;
                break;
            }
        }
        if (!mushroomGrown)
        {
            // Restore mushroom
            event.world.setBlock(event.x, event.y, event.z, event.block);
        }
    }

    // Can return null
    public SaplingGen getSaplingGen(LocalWorld world, SaplingType type, int x, int y, int z)
    {
        try
        {
            LocalBiome biome = world.getSavedBiome(x, z);
            return biome.getBiomeConfig().getSaplingGen(type);
        }
        catch (BiomeNotFoundException e)
        {
            return null;
        }
    }
}
