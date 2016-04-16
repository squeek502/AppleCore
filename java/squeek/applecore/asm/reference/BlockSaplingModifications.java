package squeek.applecore.asm.reference;

import net.minecraft.block.BlockSapling;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import squeek.applecore.asm.Hooks;

import java.util.Random;

public class BlockSaplingModifications extends BlockSapling
{
	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand)
	{
		if (!world.isRemote)
		{
			super.updateTick(world, pos, state, rand);

			// added line and changed if
			Result allowGrowthResult = Hooks.fireAllowPlantGrowthEvent(this, world, pos, state, rand);
			if (allowGrowthResult == Result.ALLOW || (allowGrowthResult == Result.DEFAULT && world.getLightFromNeighbors(pos.up()) >= 9 && rand.nextInt(7) == 0))
			{
				this.grow(world, pos, state, rand);
				// added line
				Hooks.fireOnGrowthEvent(this, world, pos, state);
			}
		}
	}
}