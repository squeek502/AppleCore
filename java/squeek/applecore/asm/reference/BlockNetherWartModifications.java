package squeek.applecore.asm.reference;

import java.util.Random;
import net.minecraft.block.BlockNetherWart;
import net.minecraft.world.World;
import squeek.applecore.asm.Hooks;
import cpw.mods.fml.common.eventhandler.Event.Result;

public class BlockNetherWartModifications extends BlockNetherWart
{
	@Override
	public void updateTick(World p_149674_1_, int p_149674_2_, int p_149674_3_, int p_149674_4_, Random p_149674_5_)
	{
		// added line
		Result allowGrowthResult = Hooks.fireAllowPlantGrowthEvent(this, p_149674_1_, p_149674_2_, p_149674_3_, p_149674_4_, p_149674_5_);
		int l = p_149674_1_.getBlockMetadata(p_149674_2_, p_149674_3_, p_149674_4_);
		// added var
		int previousMetadata = l;
		
		// changed if
		if (l < 3 && (allowGrowthResult == Result.ALLOW || (allowGrowthResult == Result.DEFAULT && p_149674_5_.nextInt(10) == 0)))
		{
			++l;
			p_149674_1_.setBlockMetadataWithNotify(p_149674_2_, p_149674_3_, p_149674_4_, l, 2);

			// added line
			Hooks.fireOnGrowthEvent(this, p_149674_1_, p_149674_2_, p_149674_3_, p_149674_4_, previousMetadata);
		}

		super.updateTick(p_149674_1_, p_149674_2_, p_149674_3_, p_149674_4_, p_149674_5_);
	}
}
