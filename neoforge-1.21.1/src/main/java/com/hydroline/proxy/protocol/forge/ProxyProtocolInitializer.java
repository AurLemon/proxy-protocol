package com.hydroline.proxy.protocol.forge;

import com.hydroline.proxy.protocol.shared.ProxyProtocolSupport;
import com.hydroline.proxy.protocol.shared.config.Config;
import com.hydroline.proxy.protocol.shared.config.Configuration;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;

/**
 * Forge's main file
 */
@Mod(ProxyProtocolSupport.MODID)
public class ProxyProtocolInitializer {

    public ProxyProtocolInitializer() {
        ProxyProtocolSupport.infoLogger.accept("proxy-protocol initial setup starting...");

        if (FMLEnvironment.dist == Dist.DEDICATED_SERVER) {
            ProxyProtocolSupport.infoLogger.accept("Running on dedicated server, preparing configuration...");
            try {
                Config config = Configuration.loadConfig(FMLPaths.CONFIGDIR.get().toFile());
                ProxyProtocolSupport.initialize(config);
                ProxyProtocolSupport.infoLogger.accept("proxy-protocol initialized successfully.");
            } catch (IOException e) {
                ProxyProtocolSupport.exceptionLogger.accept("Failed to load proxy-protocol configuration.", e);
                throw new RuntimeException(e);
            }
        }
    }
}
