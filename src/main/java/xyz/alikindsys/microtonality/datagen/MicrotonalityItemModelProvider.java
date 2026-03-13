package xyz.alikindsys.microtonality.datagen;

import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.model.ModelTemplates;
import xyz.alikindsys.microtonality.ModItems;

public class MicrotonalityItemModelProvider extends FabricModelProvider {
    public MicrotonalityItemModelProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockModelGenerators bmg) {

    }

    @Override
    public void generateItemModels(ItemModelGenerators img) {
        // I'll assume its a flat item, and not a handheld item since thats the easiest item type.
        img.generateFlatItem(ModItems.TUNING_FORK, ModelTemplates.FLAT_HANDHELD_ITEM);
    }

    @Override
    public String getName() {
        return "MicrotonalityItemModelProvider";
    }
}
