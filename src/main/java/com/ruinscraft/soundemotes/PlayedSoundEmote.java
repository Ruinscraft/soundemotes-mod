package com.ruinscraft.soundemotes;

import java.util.UUID;

public class PlayedSoundEmote {

    private String name;
    private String url;
    private UUID entityId;

    public PlayedSoundEmote(String name, String url, UUID entityId) {
        this.name = name;
        this.url = url;
        this.entityId = entityId;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public UUID getEntityId() {
        return entityId;
    }

}
