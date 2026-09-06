package com.victorgponce.hardcorerecreation;

import net.fabricmc.api.ModInitializer;

public class Hardcorerecreation implements ModInitializer {

    @Override
    public void onInitialize() {
        MobNaming.register();
        ResetTrigger.register();
    }
}
