package squeek.applecore.asm.reference;

import net.minecraft.block.BlockNetherWart;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import squeek.applecore.asm.Hooks;

import java.util.Random;

public class BlockNetherWartModifications extends BlockNetherWart
{
	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand)
	{
		// added var
		IBlockState previousState = state;
		// added line
		Result allowGrowthResult = Hooks.fireAllowPlantGrowthEvent(this, world, pos, state, rand);

		int i = (state.getValue(AGE)).intValue();
		
		// changed if
		if (i < 3 && (allowGrowthResult == Result.ALLOW || (allowGrowthResult == Result.DEFAULT && rand.nextInt(10) == 0)))
		{
			state = state.withProperty(AGE, Integer.valueOf(i + 1));
			world.setBlockState(pos, state, 2);

			// added line
			Hooks.fireOnGrowthEvent(this, world, pos, previousState);
		}

		super.updateTick(world, pos, state, rand);
	}
}