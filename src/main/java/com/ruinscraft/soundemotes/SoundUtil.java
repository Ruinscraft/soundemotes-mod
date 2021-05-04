package com.ruinscraft.soundemotes;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.*;
import net.minecraft.entity.Entity;

import java.lang.reflect.Field;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public final class SoundUtil {

    private static boolean init;
    private static Map<String, StaticSound> soundCache;
    private static SoundEngine soundEngine;

    public static boolean init() {
        if (MinecraftClient.getInstance().world == null) {
            return false;
        }

        if (init) {
            return true;
        }

        soundCache = new ConcurrentHashMap<>();

        try {
            soundEngine = getSoundEngine();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return init = true;
    }

    private static CompletableFuture<StaticSound> fetchStaticSound(PlayedSoundEmote emote) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                URL url = new URL(emote.getUrl());
                OggAudioStream oggStream = new OggAudioStream(url.openStream());
                ByteBuffer byteBuffer = oggStream.getBuffer();
                return new StaticSound(byteBuffer, oggStream.getFormat());
            } catch (Exception e) {
                System.out.println("Could not fetch sound emote: " + emote.getUrl());
                return null;
            }
        });
    }

    public static void playSoundEmote(PlayedSoundEmote emote) {
        if (!init()) {
            return;
        }

        if (soundCache.containsKey(emote.getName())) {
            Entity tracked = null;

            for (Entity entity : MinecraftClient.getInstance().world.getEntities()) {
                if (entity.getUuid().equals(emote.getEntityId())) {
                    tracked = entity;
                }
            }

            // Could not find entity
            if (tracked == null) {
                return;
            }

            try {
                playStaticSound(soundCache.get(emote.getName()), tracked);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            fetchStaticSound(emote).thenAccept(staticSound -> {
                if (staticSound == null) return;
                soundCache.put(emote.getName(), staticSound);
                playSoundEmote(emote);
            });
        }
    }

    private static void playStaticSound(StaticSound staticSound, Entity tracked) {
        Source source = soundEngine.createSource(SoundEngine.RunMode.STATIC);
        source.setBuffer(staticSound);
        // Set initial position, will continuously update later
        source.setPosition(tracked.getPos());
        source.play();

        CompletableFuture.runAsync(() -> {
            while (!source.isStopped()) {
                // Update location
                source.setPosition(tracked.getPos());

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            soundEngine.release(source);
        });
    }

    private static SoundEngine getSoundEngine() throws Exception {
        MinecraftClient client = MinecraftClient.getInstance();
        SoundManager soundManager = client.getSoundManager();
        if (soundManager == null) return null;
        Field soundSystemField = soundManager.getClass().getDeclaredField("field_5590");
//        Field soundSystemField = soundManager.getClass().getDeclaredField("soundSystem");
        soundSystemField.setAccessible(true);
        SoundSystem soundSystem = (SoundSystem) soundSystemField.get(soundManager);
        Field soundEngineField = soundSystem.getClass().getDeclaredField("field_18945");
//        Field soundEngineField = soundSystem.getClass().getDeclaredField("soundEngine");
        soundEngineField.setAccessible(true);
        SoundEngine soundEngine = (SoundEngine) soundEngineField.get(soundSystem);
        return soundEngine;
    }

}
