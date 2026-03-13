package xyz.alikindsys.microtonality.datagen;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class MicrotonalityDataGenerator implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fdg) {
        FabricDataGenerator.Pack pack = fdg.createPack();
        pack.addProvider(MicrotonalityItemModelProvider::new);
        pack.addProvider(MicrotonalityEnglishLangProvider::new);
        pack.addProvider(MicrotonalityRecipeProvider::new);
    }
}
