package squeek.applecore.asm.reference;

import java.util.Random;
import squeek.applecore.asm.Hooks;
import cpw.mods.fml.common.eventhandler.Event.Result;
import net.minecraft.block.BlockSapling;
import net.minecraft.world.World;

public class BlockSaplingModifications extends BlockSapling
{
	@Override
	public void updateTick(World p_149674_1_, int p_149674_2_, int p_149674_3_, int p_149674_4_, Random p_149674_5_)
	{
		if (!p_149674_1_.isRemote)
		{
			super.updateTick(p_149674_1_, p_149674_2_, p_149674_3_, p_149674_4_, p_149674_5_);

			// added line and changed if
			Result allowGrowthResult = Hooks.fireAllowPlantGrowthEvent(this, p_149674_1_, p_149674_2_, p_149674_3_, p_149674_4_, p_149674_5_);
			if (allowGrowthResult == Result.ALLOW || (allowGrowthResult == Result.DEFAULT && p_149674_1_.getBlockLightValue(p_149674_2_, p_149674_3_ + 1, p_149674_4_) >= 9 && p_149674_5_.nextInt(7) == 0))
			{
				this.func_149879_c(p_149674_1_, p_149674_2_, p_149674_3_, p_149674_4_, p_149674_5_);
				// added line
				Hooks.fireOnGrowthWithoutMetadataChangeEvent(this, p_149674_1_, p_149674_2_, p_149674_3_, p_149674_4_);
			}
		}
	}
}
