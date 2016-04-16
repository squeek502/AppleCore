package squeek.applecore.asm.reference;

import net.minecraft.block.Block;
import net.minecraft.block.BlockStem;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import squeek.applecore.asm.Hooks;

import java.util.Random;

public class BlockStemModifications extends BlockStem
{
	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand)
	{
		// added var
		IBlockState previousState = state;

		super.updateTick(world, pos, state, rand);

		// added line and changed if
		Result allowGrowthResult = Hooks.fireAllowPlantGrowthEvent(this, world, pos, state, rand);
		if (allowGrowthResult == Result.ALLOW || (allowGrowthResult == Result.DEFAULT && world.getLightFromNeighbors(pos.up()) >= 9))
		{
			float f = this.getGrowthChance(this, world, pos);

			// changed if
			if (allowGrowthResult == Result.ALLOW || (allowGrowthResult == Result.DEFAULT && rand.nextInt((int) (25.0F / f) + 1) == 0))
			{
				int i = state.getValue(AGE);

				if (i < 7)
				{
					state = state.withProperty(AGE, i + 1);
					world.setBlockState(pos, state, 2);
					// added line
					Hooks.fireOnGrowthEvent(this, world, pos, previousState);
				}
				else
				{
					for (EnumFacing facing : EnumFacing.Plane.HORIZONTAL)
					{
						if (world.getBlockState(pos.offset(facing)).getBlock() == this.crop)
						{
							return;
						}
					}

					pos = pos.offset(EnumFacing.Plane.HORIZONTAL.random(rand));
					IBlockState soil = world.getBlockState(pos.down());
					Block block = soil.getBlock();

					if (world.isAirBlock(pos) && (block.canSustainPlant(soil, world, pos.down(), EnumFacing.UP, this) || block == Blocks.DIRT || block == Blocks.GRASS))
					{
						world.setBlockState(pos, this.crop.getDefaultState());
					}
				}
			}
		}
	}

	// to avoid compilation errors
	Block crop;

	float getGrowthChance(Block block, World world, BlockPos pos)
	{
		return 0f;
	}

	protected BlockStemModifications(Block block)
	{
		super(block);
	}
}