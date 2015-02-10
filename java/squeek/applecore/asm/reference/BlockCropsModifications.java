package squeek.applecore.asm.reference;

import java.util.Random;
import net.minecraft.block.BlockCrops;
import net.minecraft.world.World;
import squeek.applecore.asm.Hooks;
import cpw.mods.fml.common.eventhandler.Event.Result;

public class BlockCropsModifications extends BlockCrops
{
	@Override
	public void updateTick(World p_149674_1_, int p_149674_2_, int p_149674_3_, int p_149674_4_, Random p_149674_5_)
	{
		super.updateTick(p_149674_1_, p_149674_2_, p_149674_3_, p_149674_4_, p_149674_5_);

		// added line and changed if
		Result allowGrowthResult = Hooks.fireAllowPlantGrowthEvent(this, p_149674_1_, p_149674_2_, p_149674_3_, p_149674_4_, p_149674_5_);
		if (allowGrowthResult == Result.ALLOW || (allowGrowthResult == Result.DEFAULT && p_149674_1_.getBlockLightValue(p_149674_2_, p_149674_3_ + 1, p_149674_4_) >= 9))
		{
			int l = p_149674_1_.getBlockMetadata(p_149674_2_, p_149674_3_, p_149674_4_);
			// added var
			int previousMetadata = l;

			if (l < 7)
			{
				float f = this.func_149864_n(p_149674_1_, p_149674_2_, p_149674_3_, p_149674_4_);

				// changed if
				if (allowGrowthResult == Result.ALLOW || (allowGrowthResult == Result.DEFAULT && p_149674_5_.nextInt((int) (25.0F / f) + 1) == 0))
				{
					++l;
					p_149674_1_.setBlockMetadataWithNotify(p_149674_2_, p_149674_3_, p_149674_4_, l, 2);
					// added line
					Hooks.fireOnGrowthEvent(this, p_149674_1_, p_149674_2_, p_149674_3_, p_149674_4_, previousMetadata);
				}
			}
		}
	}

	// dummy to avoid compilation error
	public float func_149864_n(World world, int x, int y, int z)
	{
		return 0f;
	};
}
