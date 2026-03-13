package xyz.alikindsys.microtonality.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.alikindsys.microtonality.Microtonality;
import xyz.alikindsys.microtonality.ModItems;

@Mixin(NoteBlock.class)
public class NoteBlockMixin extends Block  {
    @Shadow
    @Final
    public static IntegerProperty NOTE;

    // Reintroduce the old way of injecting block properties.
    // If I looked at the mixin my life would've been so much easier lel
    // Fuck i think it could DFU this as well...
    @Inject(method = "createBlockStateDefinition", at = @At("TAIL"))
    public void microtonality$addProperties(StateDefinition.Builder<Block, BlockState> builder, CallbackInfo ci) {
        builder.add(Microtonality.OCTAVE);
        builder.add(Microtonality.SCALE);
    }

    // Since we doubt that noteblocks will have changed much across these versions
    // Let's port those mixins ~carefully~.

    // Those are just MVs and injects. I like this.
    // Ordinals could be slices. I'd have to learn how to make slices tho.
    // I based on the parameter name of the original mixin, this seems to be the most valid target.
    @ModifyVariable(method = "triggerEvent", at = @At("STORE"))
    private float microtonality$adjustPitch(float f, @Local(argsOnly = true) BlockState state, @Local(ordinal = 0) int i) {
        int scale = state.getValue(Microtonality.SCALE);
        int note = i + 24 * state.getValue(Microtonality.OCTAVE);
        return (float)Math.pow(2.0, (double) (note - scale) / (double) scale);
    }

    // Maybe this is a ModifyArg?
    @ModifyVariable(method = "useWithoutItem", at = @At("STORE"), argsOnly = true)
    private BlockState microtonality$resetTuneAtMax(BlockState state) {
        if(state.getValue(NOTE) == 0 && state.getValue(Microtonality.OCTAVE) == 1) {
            state = state
                    .setValue(NOTE, 0)
                    .setValue(Microtonality.OCTAVE, 0);
        }
        if(state.getValue(Microtonality.SCALE) > 12 && state.getValue(NOTE) == 24 && state.getValue(Microtonality.OCTAVE) == 0) {
            state = state.setValue(Microtonality.OCTAVE, 1).setValue(NOTE, 0);
        }

        return state;
    }

    @Inject(method = "useWithoutItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;cycle(Lnet/minecraft/world/level/block/state/properties/Property;)Ljava/lang/Object;"), cancellable = true)
    private void microtonality$adjustScale(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult blockHitResult, CallbackInfoReturnable<InteractionResult> cir) {
        if (player.getMainHandItem().is(ModItems.TUNING_FORK)) {
            if(state.getValue(Microtonality.SCALE) != 24) {
                state = state.cycle(Microtonality.SCALE);
            } else state = state.setValue(Microtonality.SCALE, 1);
        }

        level.setBlock(pos, state, 3);
        player.displayClientMessage(Component.translatable("item.microtonality.tuning_fork.change_tune", state.getValue(Microtonality.SCALE)), true);
        cir.setReturnValue(InteractionResult.CONSUME);
    }

    public NoteBlockMixin(Properties properties) {
        super(properties);
    }
}
