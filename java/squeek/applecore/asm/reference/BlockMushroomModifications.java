package squeek.applecore.asm.reference;

import net.minecraft.block.BlockMushroom;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import squeek.applecore.asm.Hooks;

import java.util.Random;

public class BlockMushroomModifications extends BlockMushroom
{
	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand)
	{
		// added line and changed if
		Result allowGrowthResult = Hooks.fireAllowPlantGrowthEvent(this, world, pos, state, rand);
		if (allowGrowthResult == Result.ALLOW || (allowGrowthResult == Result.DEFAULT && rand.nextInt(25) == 0))
		{
			if (rand.nextInt(25) == 0)
			{
				int i = 5;
				int j = 4;

				for (BlockPos blockpos : BlockPos.getAllInBoxMutable(pos.add(-4, -1, -4), pos.add(4, 1, 4)))
				{
					if (world.getBlockState(blockpos).getBlock() == this)
					{
						--i;

						if (i <= 0)
						{
							return;
						}
					}
				}

				BlockPos blockpos1 = pos.add(rand.nextInt(3) - 1, rand.nextInt(2) - rand.nextInt(2), rand.nextInt(3) - 1);

				for (int k = 0; k < j; ++k)
				{
					if (world.isAirBlock(blockpos1) && this.canBlockStay(world, blockpos1, this.getDefaultState()))
					{
						pos = blockpos1;
					}

					blockpos1 = pos.add(rand.nextInt(3) - 1, rand.nextInt(2) - rand.nextInt(2), rand.nextInt(3) - 1);
				}

				if (world.isAirBlock(blockpos1) && this.canBlockStay(world, blockpos1, this.getDefaultState()))
				{
					world.setBlockState(blockpos1, this.getDefaultState(), 2);
				}
			}
			// added line
			Hooks.fireOnGrowthEvent(this, world, pos, state);
		}
	}
}