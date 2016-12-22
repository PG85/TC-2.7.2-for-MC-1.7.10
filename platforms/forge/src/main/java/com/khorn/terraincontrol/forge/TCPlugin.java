package com.khorn.terraincontrol.forge;

import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.standard.PluginStandardValues;
import com.khorn.terraincontrol.events.EventPriority;
import com.khorn.terraincontrol.forge.events.EventManager;
import com.khorn.terraincontrol.forge.events.PacketHandler;
import com.khorn.terraincontrol.forge.events.PlayerTracker;
import com.khorn.terraincontrol.forge.events.SaplingListener;
import com.khorn.terraincontrol.forge.events.TCCommandHandler;
import com.khorn.terraincontrol.forge.events.WorldCreationMenuHandler;
import com.khorn.terraincontrol.forge.generator.ForgeVanillaBiomeGenerator;
import com.khorn.terraincontrol.forge.generator.structure.RareBuildingStart;
import com.khorn.terraincontrol.forge.generator.structure.VillageStart;
import com.khorn.terraincontrol.generator.biome.VanillaBiomeGenerator;
import com.khorn.terraincontrol.util.minecraftTypes.StructureNames;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.network.FMLEventChannel;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.world.gen.structure.MapGenStructureIO;
import net.minecraftforge.common.MinecraftForge;

import java.io.File;

@Mod(modid = "TerrainControl", name = "TerrainControl 2.7.2 for Forge 1.7.10 v7", acceptableRemoteVersions = "*")
public class TCPlugin
{
    @Instance("TerrainControl")
    public static TCPlugin instance;

    public File terrainControlDirectory;

    @EventHandler
    public void load(FMLInitializationEvent event)
    {
        // This is the place where the mod starts loading

        // Start TerrainControl engine, and Register world type
        TerrainControl.setEngine(new ForgeEngine(new TCWorldType("TerrainControl")));

        // Register Default biome generator
        TerrainControl.getBiomeModeManager().register(VanillaBiomeGenerator.GENERATOR_NAME, ForgeVanillaBiomeGenerator.class);

        // Register village and rare building starts
        MapGenStructureIO.registerStructure(RareBuildingStart.class, StructureNames.RARE_BUILDING);
        MapGenStructureIO.registerStructure(VillageStart.class, StructureNames.VILLAGE);

        // Register listening channel for listening to received configs.
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
        {
            FMLEventChannel eventDrivenChannel = NetworkRegistry.INSTANCE.newEventDrivenChannel(PluginStandardValues.ChannelName);
            eventDrivenChannel.register(new PacketHandler());
        }

        // Register player tracker, for sending configs.
        FMLCommonHandler.instance().bus().register(new PlayerTracker());

        // Register sapling tracker, for custom tree growth.
        SaplingListener saplingListener = new SaplingListener();
        MinecraftForge.TERRAIN_GEN_BUS.register(saplingListener);
        MinecraftForge.EVENT_BUS.register(saplingListener);

        MinecraftForge.EVENT_BUS.register(new WorldCreationMenuHandler());
        
        // Register to our own events, so that they can be fired again as
        // Forge events.
        TerrainControl.registerEventHandler(new EventManager(), EventPriority.CANCELABLE);
    }
    
    @EventHandler
    public void serverLoad(FMLServerStartingEvent event)
    {
    	event.registerServerCommand(new TCCommandHandler());
    }     
}