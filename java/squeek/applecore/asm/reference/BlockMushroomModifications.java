package squeek.applecore.asm.reference;

import java.util.Random;
import squeek.applecore.asm.Hooks;
import cpw.mods.fml.common.eventhandler.Event.Result;
import net.minecraft.block.BlockMushroom;
import net.minecraft.world.World;

public class BlockMushroomModifications extends BlockMushroom
{
	@Override
	public void updateTick(World p_149674_1_, int p_149674_2_, int p_149674_3_, int p_149674_4_, Random p_149674_5_)
	{
		// added line and changed if
		Result allowGrowthResult = Hooks.fireAllowPlantGrowthEvent(this, p_149674_1_, p_149674_2_, p_149674_3_, p_149674_4_, p_149674_5_);
		if (allowGrowthResult == Result.ALLOW || (allowGrowthResult == Result.DEFAULT && p_149674_5_.nextInt(25) == 0))
		{
			byte b0 = 4;
			int l = 5;
			int i1;
			int j1;
			int k1;

			for (i1 = p_149674_2_ - b0; i1 <= p_149674_2_ + b0; ++i1)
			{
				for (j1 = p_149674_4_ - b0; j1 <= p_149674_4_ + b0; ++j1)
				{
					for (k1 = p_149674_3_ - 1; k1 <= p_149674_3_ + 1; ++k1)
					{
						if (p_149674_1_.getBlock(i1, k1, j1) == this)
						{
							--l;

							if (l <= 0)
							{
								return;
							}
						}
					}
				}
			}

			i1 = p_149674_2_ + p_149674_5_.nextInt(3) - 1;
			j1 = p_149674_3_ + p_149674_5_.nextInt(2) - p_149674_5_.nextInt(2);
			k1 = p_149674_4_ + p_149674_5_.nextInt(3) - 1;

			for (int l1 = 0; l1 < 4; ++l1)
			{
				if (p_149674_1_.isAirBlock(i1, j1, k1) && this.canBlockStay(p_149674_1_, i1, j1, k1))
				{
					p_149674_2_ = i1;
					p_149674_3_ = j1;
					p_149674_4_ = k1;
				}

				i1 = p_149674_2_ + p_149674_5_.nextInt(3) - 1;
				j1 = p_149674_3_ + p_149674_5_.nextInt(2) - p_149674_5_.nextInt(2);
				k1 = p_149674_4_ + p_149674_5_.nextInt(3) - 1;
			}

			if (p_149674_1_.isAirBlock(i1, j1, k1) && this.canBlockStay(p_149674_1_, i1, j1, k1))
			{
				p_149674_1_.setBlock(i1, j1, k1, this, 0, 2);
			}

			// added line
			Hooks.fireOnGrowthWithoutMetadataChangeEvent(this, p_149674_1_, p_149674_2_, p_149674_3_, p_149674_4_);
		}
	}
}
