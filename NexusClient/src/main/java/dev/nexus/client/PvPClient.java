package dev.nexus.client;

import net.fabricmc.api.ClientModInitializer;

public class PvPClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ModuleManager.init();
    }
}
