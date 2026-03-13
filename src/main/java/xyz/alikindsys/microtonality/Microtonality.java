package xyz.alikindsys.microtonality;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class Microtonality implements ModInitializer {
    public static final IntegerProperty SCALE = IntegerProperty.create("scale" , 1, 24);
    public static final IntegerProperty OCTAVE = IntegerProperty.create("octave", 0 ,1);
    public static final String MOD_ID = "microtonality";


    @Override
    public void onInitialize() {
        ModItems.init();

        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES)
                .register((ig) -> ig.accept(ModItems.TUNING_FORK));
    }
}
