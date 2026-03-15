package xyz.alikindsys.microtonality.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.alikindsys.microtonality.Microtonality;
import xyz.alikindsys.microtonality.ModItems;

@Mixin(NoteBlock.class)
public abstract class NoteBlockMixin extends Block  {
    @Unique
    private static final IntegerProperty SCALE = IntegerProperty.create("scale" , 0, Microtonality.MAX_SCALE);
    @Unique
    private static final BooleanProperty OCTAVE = BooleanProperty.create("octave");

    @Shadow
    @Final
    public static IntegerProperty NOTE;

    @Inject(method = "createBlockStateDefinition", at = @At("TAIL"))
    public void microtonality$addProperties(StateDefinition.Builder<Block, BlockState> builder, CallbackInfo ci) {
        builder.add(OCTAVE, SCALE);
    }


    @ModifyExpressionValue(method = "triggerEvent", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/NoteBlock;getPitchFromNote(I)F"))
    private float microtonality$adjustPitch(float f, @Local(argsOnly = true) BlockState state) {
        int scale = state.getValue(SCALE);
        int idx = state.getValue(NOTE);

        int note = idx + scale * (state.getValue(OCTAVE) ? 1 : 0) ;

        return (float)Math.pow(2.0, (double) (note - scale) / (double) scale);
    }

    // Maybe this is a ModifyArg?
    @ModifyVariable(method = "useWithoutItem", at = @At("STORE"), argsOnly = true)
    private BlockState microtonality$resetTuneAtMax(BlockState state) {
        if(state.getValue(NOTE).equals(state.getValue(SCALE))) {
            state = state
                    .setValue(NOTE, 0)
                    .setValue(OCTAVE, !state.getValue(OCTAVE));
        }
        return state;
    }

    @Inject(method = "useItemOn", at = @At(value = "HEAD"), cancellable = true)
    private void microtonality$adjustScale(ItemStack item, BlockState state, Level level, BlockPos pos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult, CallbackInfoReturnable<InteractionResult> cir) {
        if (item.is(ModItems.TUNING_FORK)) {
            if(state.getValue(SCALE) != Microtonality.MAX_SCALE) {
                state = state.cycle(SCALE);
            } else state = state.setValue(SCALE, 1);


            level.setBlock(pos, state, 3);
            player.displayClientMessage(Component.translatable("item.microtonality.tuning_fork.change_tune", state.getValue(SCALE)), true);
            cir.setReturnValue(InteractionResult.CONSUME);
        }
    }

    // Injection so that the default state is 12 tet, no octave.
    @ModifyArg(method = "<init>", at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/world/level/block/NoteBlock;registerDefaultState(Lnet/minecraft/world/level/block/state/BlockState;)V")
    )
    private BlockState microtonality$setDefaultState(BlockState par1) {
        return par1.setValue(OCTAVE, false).setValue(SCALE, 12);
    }

    public NoteBlockMixin(Properties properties) {
        super(properties);
    }
}
