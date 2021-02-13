package com.ruinscraft.soundemotes;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.nio.charset.StandardCharsets;

public final class NetworkUtil {

    private static final JsonParser JSON_PARSER;
    private static final Identifier CHANNEL_PLAYED_SOUND_EMOTE;

    static {
        JSON_PARSER = new JsonParser();
        CHANNEL_PLAYED_SOUND_EMOTE = new Identifier("soundemotes", "played_sound_emote");
    }

    private static String readString(PacketByteBuf buf) {
        int len = buf.readShort();
        byte data[] = new byte[len];
        buf.readBytes(data, 0, len);
        return new String(data, StandardCharsets.UTF_8);
    }

    public static void init() {
        ClientPlayNetworking.registerGlobalReceiver(CHANNEL_PLAYED_SOUND_EMOTE, (client, handler, buf, responseSender) -> {
            String playedSoundEmoteJsonRaw = readString(buf);

            client.submit(() -> {
                try {
                    JsonObject playedSoundEmoteJson = JSON_PARSER.parse(playedSoundEmoteJsonRaw).getAsJsonObject();
                    PlayedSoundEmote playedSoundEmote = deserializePlayedSoundEmote(playedSoundEmoteJson);
                    SoundUtil.playSound(playedSoundEmote);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        });
    }

    public static PlayedSoundEmote deserializePlayedSoundEmote(JsonObject playedSoundEmoteJson) {
        String soundEmoteName = playedSoundEmoteJson.get("sound_emote").getAsJsonObject().get("name").getAsString();
        String soundEmoteUrl = playedSoundEmoteJson.get("sound_emote").getAsJsonObject().get("url").getAsString();
        String world = playedSoundEmoteJson.get("world").getAsString();
        int x = playedSoundEmoteJson.get("x").getAsInt();
        int y = playedSoundEmoteJson.get("y").getAsInt();
        int z = playedSoundEmoteJson.get("z").getAsInt();
        return new PlayedSoundEmote(soundEmoteName, soundEmoteUrl, world, x, y, z);
    }

}
