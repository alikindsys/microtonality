package xyz.alikindsys.microtonality;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.registry.RegistryIdRemapCallback;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import xyz.alikindsys.vendor.microstatement.StateRefresher;

public class Microtonality implements ModInitializer {

    @Override
    public void onInitialize() {
        RegistryIdRemapCallback.event(BuiltInRegistries.BLOCK)
                .register(s ->
                        StateRefresher.INSTANCE.reorderStates(BuiltInRegistries.BLOCK, Block.BLOCK_STATE_REGISTRY, Block::getStateDefinition));
    }
}
