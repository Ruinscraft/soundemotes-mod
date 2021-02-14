package com.ruinscraft.soundemotes;

import net.fabricmc.api.ModInitializer;

public class SoundEmotesMod implements ModInitializer {

    @Override
    public void onInitialize() {
        NetworkUtil.init();
        SoundUtil.init();
    }

}
