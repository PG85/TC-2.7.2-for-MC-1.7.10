package com.khorn.terraincontrol.forge.events;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

import com.khorn.terraincontrol.BiomeIds;
import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.WorldConfig;
import com.khorn.terraincontrol.configuration.WorldSettings;
import com.khorn.terraincontrol.exception.BiomeNotFoundException;
import com.khorn.terraincontrol.forge.util.WorldHelper;
import com.mojang.authlib.GameProfile;

public class TCCommandHandler implements ICommand
{ 
    private final ArrayList aliases;
 
    public TCCommandHandler() 
    { 
        aliases = new ArrayList(); 

        aliases.add("tc"); 

        aliases.add("tc"); 
    } 
  
    @Override 
    public String getCommandName() 
    { 
        return "tc"; 

    } 

    @Override         
    public String getCommandUsage(ICommandSender var1) 
    { 
        //return "worldinfo <text>"; 
    	return "tc";
    } 

    @Override 
    public java.util.List getCommandAliases() 
    { 
        return this.aliases;
    } 

    @Override 
    public void processCommand(ICommandSender sender, String[] argString)
    { 
        World world = sender.getEntityWorld();
        LocalWorld localWorld = WorldHelper.toLocalWorld(world);
        
        if (!world.isRemote && world.provider.dimensionId == 0) // Server side 
        {
    		MinecraftServer server = MinecraftServer.getServer();
    		ServerConfigurationManager serverConfigManager = server.getConfigurationManager();
    		EntityPlayer player = ((EntityPlayer)sender);
    		GameProfile gameProfile = player.getGameProfile();
    		boolean isOpped = serverConfigManager.func_152596_g(gameProfile);//.isPlayerOpped(gameProfile.getName());        	
        	
        	if(argString == null || argString.length == 0)
        	{
        		sender.addChatMessage(new ChatComponentText("-- TerrainControl --"));
				sender.addChatMessage(new ChatComponentText("Commands:"));
				sender.addChatMessage(new ChatComponentText("/tc world - Show author and description information for this world."));
				sender.addChatMessage(new ChatComponentText("/tc biome - Show biome information for any biome at the player's coordinates."));
        	}
        	else if(argString[0].toLowerCase().trim().equals("worldinfo") || argString[0].toLowerCase().trim().equals("world"))
        	{                
    			WorldConfig worldConfig = ((WorldSettings)TerrainControl.getEngine().getWorld(sender.getEntityWorld().getWorldInfo().getWorldName()).getConfigs()).worldConfig;
    			sender.addChatMessage(new ChatComponentText("-- World info --"));
    			sender.addChatMessage(new ChatComponentText("Name: " + localWorld.getName()));
    			sender.addChatMessage(new ChatComponentText("Author: " + worldConfig.author));
    			sender.addChatMessage(new ChatComponentText("Description: " + worldConfig.description)); 
        	}
        	else if(argString[0].toLowerCase().trim().equals("biomeinfo") || argString[0].toLowerCase().trim().equals("biome"))
        	{        		        		        		
                LocalBiome biome = localWorld.getBiome(sender.getPlayerCoordinates().posX, sender.getPlayerCoordinates().posZ);
                BiomeIds biomeIds = biome.getIds();
        		
    			sender.addChatMessage(new ChatComponentText("-- Biome info --"));
    			sender.addChatMessage(new ChatComponentText("Name: " + biome.getName()));
    			sender.addChatMessage(new ChatComponentText("Id: " + biomeIds.getGenerationId()));
    			sender.addChatMessage(new ChatComponentText("Base temp: " + biome.getBiomeConfig().biomeTemperature));
    			sender.addChatMessage(new ChatComponentText("Temp at player: " + biome.getTemperatureAt(sender.getPlayerCoordinates().posX, sender.getPlayerCoordinates().posY, sender.getPlayerCoordinates().posZ)));

                try
                {
                    LocalBiome savedBiome = localWorld.getSavedBiome(sender.getPlayerCoordinates().posX, sender.getPlayerCoordinates().posZ);
                    BiomeIds savedIds = savedBiome.getIds();
        			sender.addChatMessage(new ChatComponentText("Saved name: " + savedBiome.getName()));
        			sender.addChatMessage(new ChatComponentText("Saved id: " + savedIds.getSavedId()));
                }
                catch (BiomeNotFoundException e)
                {
        			sender.addChatMessage(new ChatComponentText("Saved name: Unknown"));
        			sender.addChatMessage(new ChatComponentText("Saved id: Unknown"));
                }    			    	
        	}
        }
    } 

    @Override 
    public boolean canCommandSenderUseCommand(ICommandSender var1) 
    { 
        return true;

    } 

    @Override 
    public boolean isUsernameIndex(String[] var1, int var2) 
    { 
        // TODO Auto-generated method stub 

        return false;

    }

	@Override
	public int compareTo(Object arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List addTabCompletionOptions(ICommandSender arg0, String[] arg1) {
		// TODO Auto-generated method stub
		return null;
	}
}

