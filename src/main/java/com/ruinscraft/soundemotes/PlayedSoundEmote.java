package com.ruinscraft.soundemotes;

public class PlayedSoundEmote {

    private String name;
    private String url;
    private String world;
    private int x;
    private int y;
    private int z;

    public PlayedSoundEmote(String name, String url, String world, int x, int y, int z) {
        this.name = name;
        this.url = url;
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public String getWorld() {
        return world;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

}
