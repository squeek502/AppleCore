package squeek.applecore.asm.reference;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.IGrowable;
import net.minecraft.block.material.Material;
import net.minecraft.world.World;
import squeek.applecore.asm.Hooks;

public class IGrowableModifications extends Block implements IGrowable
{
	protected IGrowableModifications(Material p_i45394_1_)
	{
		super(p_i45394_1_);
	}

	// is not fully grown
	// untouched
	@Override
	public boolean func_149851_a(World p_149851_1_, int p_149851_2_, int p_149851_3_, int p_149851_4_, boolean p_149851_5_)
	{
		return false;
	}

	// can fertilize
	@Override
	public boolean func_149852_a(World p_149852_1_, Random p_149852_2_, int p_149852_3_, int p_149852_4_, int p_149852_5_)
	{
		return true;
	}
	
	// copy/rename the original func_149853_b implementation to here
	public void AppleCore_fertilize(World world, Random random, int x, int y, int z)
	{
		// func_149853_b's implementation
	}

	// fertilize
	@Override
	public void func_149853_b(World p_149853_1_, Random p_149853_2_, int p_149853_3_, int p_149853_4_, int p_149853_5_)
	{
		// method instructions replaced with only the following
		Hooks.fireAppleCoreFertilizeEvent(this, p_149853_1_, p_149853_3_, p_149853_4_, p_149853_5_, p_149853_2_);
		return;
	}

}
