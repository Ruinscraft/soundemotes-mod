package com.ruinscraft.soundemotes;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.*;
import net.minecraft.util.math.Vec3d;

import java.lang.reflect.Field;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public final class SoundUtil {

    private static Map<String, StaticSound> soundCache;
    private static SoundEngine soundEngine;

    public static void init() {
        soundCache = new ConcurrentHashMap<>();
    }

    public static void playSound(PlayedSoundEmote emote) {
        if (soundEngine == null) {
            if (MinecraftClient.getInstance().world != null) {
                try {
                    soundEngine = getSoundEngine();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                return;
            }
        }

        if (soundCache.containsKey(emote.getName())) {
            try {
                playStaticSound(soundCache.get(emote.getName()), emote.getVec3d());
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            fetchSound(emote).thenAccept(staticSound -> {
                if (staticSound == null) return;
                soundCache.put(emote.getName(), staticSound);
                playSound(emote);
            });
        }
    }

    private static void playStaticSound(StaticSound staticSound, Vec3d position) {
        if (soundEngine == null) return;
        Source source = soundEngine.createSource(SoundEngine.RunMode.STATIC);
        source.setBuffer(staticSound);
        source.setPosition(position);
        source.play();
        CompletableFuture.runAsync(() -> {
            while (!source.isStopped()) {
                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            soundEngine.release(source);
        });
    }

    private static CompletableFuture<StaticSound> fetchSound(PlayedSoundEmote emote) {
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

    private static SoundEngine getSoundEngine() throws Exception {
        MinecraftClient client = MinecraftClient.getInstance();
        SoundManager soundManager = client.getSoundManager();
        if (soundManager == null) return null;
//        Field soundSystemField = soundManager.getClass().getDeclaredField("field_5590");
        Field soundSystemField = soundManager.getClass().getDeclaredField("soundSystem");
        soundSystemField.setAccessible(true);
        SoundSystem soundSystem = (SoundSystem) soundSystemField.get(soundManager);
//        Field soundEngineField = soundSystem.getClass().getDeclaredField("field_18945");
        Field soundEngineField = soundSystem.getClass().getDeclaredField("soundEngine");
        soundEngineField.setAccessible(true);
        SoundEngine soundEngine = (SoundEngine) soundEngineField.get(soundSystem);
        return soundEngine;
    }

}
