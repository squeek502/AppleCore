package squeek.applecore.asm.util;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

/**
 * Dummy interface to allow calling the copied fertilize method without using reflection.
 * This avoidance of reflection in turn avoids @SideOnly issues on dedicated servers
 */
public interface IAppleCoreFertilizable
{
	void AppleCore_fertilize(World world, Random random, BlockPos pos, IBlockState state);
}