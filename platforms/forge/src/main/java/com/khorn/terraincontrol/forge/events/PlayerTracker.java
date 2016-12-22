package com.khorn.terraincontrol.forge.events;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.ConfigProvider;
import com.khorn.terraincontrol.configuration.standard.PluginStandardValues;
import com.khorn.terraincontrol.forge.util.WorldHelper;
import com.khorn.terraincontrol.logging.LogMarker;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.S3FPacketCustomPayload;

import java.io.DataOutput;
import java.io.IOException;

public class PlayerTracker
{

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event)
    {
        // Server-side - called whenever a player logs in
        // I couldn't find a way to detect if the client has TerrainControl,
        // so for now the configs are sent anyway.

        // Get the config
        if (!(event.player instanceof EntityPlayerMP))
        {
            return;
        }

        EntityPlayerMP player = (EntityPlayerMP) event.player;
        
        LocalWorld worldTC = WorldHelper.toLocalWorld(player.getEntityWorld());
        if (worldTC == null)
        {
            // World not loaded
            return;
        }
        ConfigProvider configs = worldTC.getConfigs();

        // Serialize it
        ByteBuf nettyBuffer = Unpooled.buffer();
        PacketBuffer mojangBuffer = new PacketBuffer(nettyBuffer);

        DataOutput stream = new ByteBufOutputStream(nettyBuffer);
        try
        {
            stream.writeInt(PluginStandardValues.ProtocolVersion);
            configs.writeToStream(stream);
        } catch (IOException e)
        {
            TerrainControl.printStackTrace(LogMarker.FATAL, e);
        }

        // Make the packet
        S3FPacketCustomPayload packet = new S3FPacketCustomPayload(PluginStandardValues.ChannelName, mojangBuffer);

        // Send the packet
        player.playerNetServerHandler.sendPacket(packet);
    }

}
