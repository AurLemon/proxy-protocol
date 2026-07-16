package com.hydroline.proxy.protocol.shared.mixin;

import com.hydroline.proxy.protocol.shared.ProxyProtocolSupport;
import com.hydroline.proxy.protocol.shared.impl.ProxyProtocolPipelineSupport;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.local.LocalChannel;
import io.netty.channel.local.LocalServerChannel;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Connection.class)
public abstract class ConnectionPacketHandlerMixin {

    @Inject(method = "configurePacketHandler", at = @At("TAIL"), remap = false)
    private void proxy_protocol$installDetector(ChannelPipeline pipeline, CallbackInfo ci) {
        Connection connection = (Connection) (Object) this;
        if (connection.getReceiving() != PacketFlow.SERVERBOUND) {
            return;
        }

        if (pipeline.channel() instanceof LocalChannel || pipeline.channel() instanceof LocalServerChannel) {
            ProxyProtocolSupport.logDebug("Skipping smart detector for in-memory connection pipeline.");
            return;
        }

        ProxyProtocolPipelineSupport.installSmartDetector(pipeline);
    }
}
