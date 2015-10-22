package squeek.applecore.asm.reference;

import java.util.Random;
import net.minecraft.block.BlockCocoa;
import net.minecraft.world.World;
import squeek.applecore.asm.Hooks;
import cpw.mods.fml.common.eventhandler.Event.Result;

public class BlockCocoaModifications extends BlockCocoa
{
	@Override
	public void updateTick(World p_149674_1_, int p_149674_2_, int p_149674_3_, int p_149674_4_, Random p_149674_5_)
	{
		// added line
		Result allowGrowthResult = Hooks.fireAllowPlantGrowthEvent(this, p_149674_1_, p_149674_2_, p_149674_3_, p_149674_4_, p_149674_5_);

		if (!this.canBlockStay(p_149674_1_, p_149674_2_, p_149674_3_, p_149674_4_))
		{
			this.dropBlockAsItem(p_149674_1_, p_149674_2_, p_149674_3_, p_149674_4_, p_149674_1_.getBlockMetadata(p_149674_2_, p_149674_3_, p_149674_4_), 0);
			p_149674_1_.setBlock(p_149674_2_, p_149674_3_, p_149674_4_, getBlockById(0), 0, 2);
		}
		// changed else if
		else if (allowGrowthResult == Result.ALLOW || (allowGrowthResult == Result.DEFAULT && p_149674_1_.rand.nextInt(5) == 0))
		{
			int l = p_149674_1_.getBlockMetadata(p_149674_2_, p_149674_3_, p_149674_4_);
			int i1 = func_149987_c(l);

			if (i1 < 2)
			{
				++i1;
				p_149674_1_.setBlockMetadataWithNotify(p_149674_2_, p_149674_3_, p_149674_4_, i1 << 2 | getDirection(l), 2);

				// added line
				Hooks.fireOnGrowthEvent(this, p_149674_1_, p_149674_2_, p_149674_3_, p_149674_4_, l);
			}
		}
	}
}
