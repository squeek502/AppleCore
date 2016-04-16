package squeek.applecore.asm.reference;

import net.minecraft.block.Block;
import net.minecraft.block.IGrowable;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import squeek.applecore.asm.Hooks;

import java.util.Random;

public class IGrowableModifications extends Block implements IGrowable
{
	protected IGrowableModifications(Material material)
	{
		super(material);
	}

	// is not fully grown
	// untouched
	@Override
	public boolean canGrow(World world, BlockPos pos, IBlockState state, boolean isClient)
	{
		return false;
	}

	// can fertilize
	@Override
	public boolean canUseBonemeal(World world, Random rand, BlockPos pos, IBlockState state)
	{
		return true;
	}
	
	// copy/rename the original grow to here
	public void AppleCore_fertilize(World world, Random random, int x, int y, int z)
	{
		// grow's implementation
	}

	// fertilize
	@Override
	public void grow(World world, Random rand, BlockPos pos, IBlockState state)
	{
		// method instructions replaced with only the following
		Hooks.fireAppleCoreFertilizeEvent(this, world, pos, state, rand);
		return;
	}
}