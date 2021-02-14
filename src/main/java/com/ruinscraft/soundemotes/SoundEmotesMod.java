package com.ruinscraft.soundemotes;

import net.fabricmc.api.ModInitializer;
import net.minecraft.client.MinecraftClient;

import java.util.concurrent.CompletableFuture;

public class SoundEmotesMod implements ModInitializer {

    @Override
    public void onInitialize() {
        NetworkUtil.init();
        SoundUtil.init();

        CompletableFuture.runAsync(() -> {
            while (true) {
                if (MinecraftClient.getInstance().world != null) {
                    MinecraftClient.getInstance().player.sendChatMessage("uh no");
                }

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

    }

}
