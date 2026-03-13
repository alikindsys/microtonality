package xyz.alikindsys.microtonality.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.core.HolderLookup;

import java.util.concurrent.CompletableFuture;

public class MicrotonalityEnglishLangProvider extends FabricLanguageProvider {
    protected MicrotonalityEnglishLangProvider(FabricDataOutput dataOutput, CompletableFuture<HolderLookup.Provider> registryLookup) {
        super(dataOutput, "en_us", registryLookup);
    }

    @Override
    public void generateTranslations(HolderLookup.Provider provider, TranslationBuilder translationBuilder) {
        translationBuilder.add("item.microtonality.tuning_fork.change_tune", "Changed tuning to %d-TET");
        translationBuilder.add("item.microtonality.tuning_fork", "Tuning Fork");
    }
}
