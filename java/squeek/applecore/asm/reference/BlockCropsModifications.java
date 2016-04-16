package squeek.applecore.asm.reference;

import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import squeek.applecore.asm.Hooks;

import java.util.Random;

public class BlockCropsModifications extends BlockCrops
{
	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand)
	{
		super.updateTick(world, pos, state, rand);

		// added line and changed if
		Result allowGrowthResult = Hooks.fireAllowPlantGrowthEvent(this, world, pos, state, rand);
		if (allowGrowthResult == Result.ALLOW || (allowGrowthResult == Result.DEFAULT && world.getLightFromNeighbors(pos.up()) >= 9))
		{
			int i = this.getAge(state);

			if (i < this.getMaxAge())
			{
				float f = getGrowthChance(this, world, pos);

				// changed if
				if (allowGrowthResult == Result.ALLOW || (allowGrowthResult == Result.DEFAULT && rand.nextInt((int) (25.0F / f) + 1) == 0))
				{
					world.setBlockState(pos, state.withProperty(AGE, i + 1), 2);
					// added line
					Hooks.fireOnGrowthEvent(this, world, pos, state);
				}
			}
		}
	}

	// dummy to avoid compilation error
	protected static float getGrowthChance(Block blockIn, World worldIn, BlockPos pos)
	{
		return 0f;
	};
}