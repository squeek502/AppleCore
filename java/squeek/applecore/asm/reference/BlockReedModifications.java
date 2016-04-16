package squeek.applecore.asm.reference;

import net.minecraft.block.BlockReed;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import squeek.applecore.asm.Hooks;

import java.util.Random;

public class BlockReedModifications extends BlockReed
{
	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand)
	{
		if (world.getBlockState(pos.down()).getBlock() == Blocks.REEDS || this.checkForDrop(world, pos, state))
		{
			if (world.isAirBlock(pos.up()))
			{
				int i;

				for (i = 1; world.getBlockState(pos.down(i)).getBlock() == this; ++i)
				{
					;
				}

				// changed if (added && ...)
				if (i < 3 && Hooks.fireAllowPlantGrowthEvent(this, world, pos, state, rand) != Result.DENY)
				{
					int j = state.getValue(AGE);

					if (j == 15)
					{
						world.setBlockState(pos.up(), this.getDefaultState());
						world.setBlockState(pos, state.withProperty(AGE, 0), 4);
					}
					else
					{
						world.setBlockState(pos, state.withProperty(AGE, j + 1), 4);
					}

					// added line
					Hooks.fireOnGrowthEvent(this, world, pos, state);
				}
			}
		}
	}
}