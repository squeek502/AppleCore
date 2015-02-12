package squeek.applecore.asm.reference;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.IGrowable;
import net.minecraft.block.material.Material;
import net.minecraft.world.World;
import squeek.applecore.asm.Hooks;
import cpw.mods.fml.common.eventhandler.Event;

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

	// fertilize
	@Override
	public void func_149853_b(World p_149853_1_, Random p_149853_2_, int p_149853_3_, int p_149853_4_, int p_149853_5_)
	{
		// added at start
		int previousMetadata = p_149853_1_.getBlockMetadata(p_149853_3_, p_149853_4_, p_149853_5_);
		Event.Result fertilizeResult = Hooks.fireFertilizeEvent(this, p_149853_1_, p_149853_3_, p_149853_4_, p_149853_5_, p_149853_2_, previousMetadata);
		
		if (fertilizeResult == Event.Result.DENY)
			return;
		
		// wrap the default implementation in a conditional
		if (fertilizeResult == Event.Result.DEFAULT)
		{
			// default implementation
		}

		// added at end
		Hooks.fireFertilizedEvent(this, p_149853_1_, p_149853_3_, p_149853_4_, p_149853_5_, previousMetadata);
	}

}
