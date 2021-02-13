package com.ruinscraft.soundemotes;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.*;
import net.minecraft.util.math.Vec3d;

import java.lang.reflect.Field;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class SoundUtil {

    private static Map<String, StaticSound> soundCache;

    static {
        soundCache = new ConcurrentHashMap<>();
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
        MinecraftClient client = MinecraftClient.getInstance();
        SoundManager soundManager = client.getSoundManager();
        if (soundManager == null) return;
        Field soundSystemField = soundManager.getClass().getDeclaredField("field_5590");
        soundSystemField.setAccessible(true);
        SoundSystem soundSystem = (SoundSystem) soundSystemField.get(soundManager);
        Field soundEngineField = soundSystem.getClass().getDeclaredField("field_18945");
        soundEngineField.setAccessible(true);
        SoundEngine soundEngine = (SoundEngine) soundEngineField.get(soundSystem);
        Source source = soundEngine.createSource(SoundEngine.RunMode.STATIC);
        source.setBuffer(staticSound);
        source.setPosition(position);
        source.play();
    }

}
