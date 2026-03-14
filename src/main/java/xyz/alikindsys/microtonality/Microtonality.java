package xyz.alikindsys.microtonality;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.world.item.CreativeModeTabs;

public class Microtonality implements ModInitializer {

    // I'm sad I can't get 31-TET... Maybe I'll make a modded block that adds this.
    // Honestly patching in vanilla mechanics is silly.
    public static final int MAX_SCALE = 24;
    public static final String MOD_ID = "microtonality";


    @Override
    public void onInitialize() {
        ModItems.init();

        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES)
                .register((ig) -> ig.accept(ModItems.TUNING_FORK));
    }
}
