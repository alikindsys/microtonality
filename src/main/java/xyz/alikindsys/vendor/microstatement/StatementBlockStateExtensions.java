package xyz.alikindsys.vendor.microstatement;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public interface StatementBlockStateExtensions extends StatementStateExtensions<BlockState>
{
    Block statement_getBlock();
}
