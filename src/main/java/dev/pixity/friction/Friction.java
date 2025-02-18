package dev.pixity.friction;

import com.google.inject.Inject;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import org.slf4j.Logger;

@Plugin(id = "friction", name = "Friction", version = "1.0.0-SNAPSHOT", description = "GrindMC Friction, Transfer your velocity players the right way.", url = "pixity.dev", authors = {"Pixityy"})
public class Friction {

    @Inject
    private Logger logger;

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
    }
}
