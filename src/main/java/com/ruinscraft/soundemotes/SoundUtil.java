package com.ruinscraft.soundemotes;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.*;
import net.minecraft.util.math.Vec3d;

import java.lang.reflect.Field;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class SoundUtil {

    private static Map<String, StaticSound> soundCache;
    private static List<Source> activeSources;

    static {
        soundCache = new ConcurrentHashMap<>();
        activeSources = new ArrayList<>();

        new Thread(() -> {
            MinecraftClient client = MinecraftClient.getInstance();

            // Source cleanup thread
            while (client.isRunning()) {
                Iterator<Source> iterator = activeSources.iterator();
                while (iterator.hasNext()) {
                    Source source = iterator.next();
                    if (client.world == null || source.isStopped()) {
                        try {
                            getSoundEngine().release(source);
                            source.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        iterator.remove();
                    }
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static void playSound(PlayedSoundEmote playedSoundEmote) {
        Vec3d position = new Vec3d(playedSoundEmote.getX(), playedSoundEmote.getY(), playedSoundEmote.getZ());

        if (!soundCache.containsKey(playedSoundEmote.getName())) {
            new Thread(() -> {
                try {
                    URL url = new URL(playedSoundEmote.getUrl());
                    OggAudioStream oggStream = new OggAudioStream(url.openStream());
                    ByteBuffer byteBuffer = oggStream.getBuffer();
                    StaticSound staticSound = new StaticSound(byteBuffer, oggStream.getFormat());
                    soundCache.put(playedSoundEmote.getName(), staticSound);

                    MinecraftClient.getInstance().submit(() -> {
                        try {
                            playStaticSound(staticSound, position);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        } else {
            try {
                playStaticSound(soundCache.get(playedSoundEmote.getName()), position);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void playStaticSound(StaticSound staticSound, Vec3d position) throws Exception {
        Source source = getSoundEngine().createSource(SoundEngine.RunMode.STATIC);
        source.setBuffer(staticSound);
        source.setPosition(position);
        source.play();
        activeSources.add(source);
    }

    public static SoundEngine getSoundEngine() throws Exception {
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
