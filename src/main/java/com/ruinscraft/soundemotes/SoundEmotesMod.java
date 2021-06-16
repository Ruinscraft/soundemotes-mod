package com.ruinscraft.soundemotes;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.util.Identifier;

import java.util.UUID;

public class SoundEmotesMod implements ModInitializer {

    private static final Identifier CHANNEL_PLAYED_SOUND_EMOTE = new Identifier("soundemotes", "played_sound_emote");

    @Override
    public void onInitialize() {
        ClientPlayNetworking.registerGlobalReceiver(CHANNEL_PLAYED_SOUND_EMOTE, (client, handler, buf, responseSender) -> {
            PlayedSoundEmote soundEmote = new PlayedSoundEmote(buf.readString(), buf.readString(), buf.readUuid());
            client.submit(() -> SoundUtil.playSoundEmote(soundEmote));
        });
    }

    public static class PlayedSoundEmote {
        public final String name;
        public final String url;
        public final UUID entityId;

        public PlayedSoundEmote(String name, String url, UUID entityId) {
            this.name = name;
            this.url = url;
            this.entityId = entityId;
        }
    }

}
