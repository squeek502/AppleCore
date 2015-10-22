package squeek.applecore.asm.reference;

import java.util.Random;
import squeek.applecore.asm.Hooks;
import cpw.mods.fml.common.eventhandler.Event.Result;
import net.minecraft.block.BlockReed;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

public class BlockReedModifications extends BlockReed
{
	@Override
	public void updateTick(World p_149674_1_, int p_149674_2_, int p_149674_3_, int p_149674_4_, Random p_149674_5_)
	{
		if (p_149674_1_.getBlock(p_149674_2_, p_149674_3_ - 1, p_149674_4_) == Blocks.reeds || this.func_150170_e(p_149674_1_, p_149674_2_, p_149674_3_, p_149674_4_))
		{
			if (p_149674_1_.isAirBlock(p_149674_2_, p_149674_3_ + 1, p_149674_4_))
			{
				int l;

				for (l = 1; p_149674_1_.getBlock(p_149674_2_, p_149674_3_ - l, p_149674_4_) == this; ++l)
				{
					;
				}

				// changed if (added && ...)
				if (l < 3 && Hooks.fireAllowPlantGrowthEvent(this, p_149674_1_, p_149674_2_, p_149674_3_, p_149674_4_, p_149674_5_) != Result.DENY)
				{
					int i1 = p_149674_1_.getBlockMetadata(p_149674_2_, p_149674_3_, p_149674_4_);

					if (i1 == 15)
					{
						p_149674_1_.setBlock(p_149674_2_, p_149674_3_ + 1, p_149674_4_, this);
						p_149674_1_.setBlockMetadataWithNotify(p_149674_2_, p_149674_3_, p_149674_4_, 0, 4);
					}
					else
					{
						p_149674_1_.setBlockMetadataWithNotify(p_149674_2_, p_149674_3_, p_149674_4_, i1 + 1, 4);
					}

					// added line
					Hooks.fireOnGrowthEvent(this, p_149674_1_, p_149674_2_, p_149674_3_, p_149674_4_, i1);
				}
			}
		}
	}
}
