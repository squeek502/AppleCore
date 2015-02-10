package squeek.applecore.asm.reference;

import static net.minecraftforge.common.util.ForgeDirection.UP;
import java.util.Random;
import squeek.applecore.asm.Hooks;
import cpw.mods.fml.common.eventhandler.Event.Result;
import net.minecraft.block.Block;
import net.minecraft.block.BlockStem;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

public class BlockStemModifications extends BlockStem
{
	@Override
	public void updateTick(World p_149674_1_, int p_149674_2_, int p_149674_3_, int p_149674_4_, Random p_149674_5_)
	{
		super.updateTick(p_149674_1_, p_149674_2_, p_149674_3_, p_149674_4_, p_149674_5_);

		// added line and changed if
		Result allowGrowthResult = Hooks.fireAllowPlantGrowthEvent(this, p_149674_1_, p_149674_2_, p_149674_3_, p_149674_4_, p_149674_5_);
		if (allowGrowthResult == Result.ALLOW || (allowGrowthResult == Result.DEFAULT && p_149674_1_.getBlockLightValue(p_149674_2_, p_149674_3_ + 1, p_149674_4_) >= 9))
		{
			float f = this.func_149875_n(p_149674_1_, p_149674_2_, p_149674_3_, p_149674_4_);

			// changed if
			if (allowGrowthResult == Result.ALLOW || (allowGrowthResult == Result.DEFAULT && p_149674_5_.nextInt((int) (25.0F / f) + 1) == 0))
			{
				int l = p_149674_1_.getBlockMetadata(p_149674_2_, p_149674_3_, p_149674_4_);
				// added var
				int previousMetadata = l;

				if (l < 7)
				{
					++l;
					p_149674_1_.setBlockMetadataWithNotify(p_149674_2_, p_149674_3_, p_149674_4_, l, 2);
					// added line
					Hooks.fireOnGrowthEvent(this, p_149674_1_, p_149674_2_, p_149674_3_, p_149674_4_, previousMetadata);
				}
				else
				{
					if (p_149674_1_.getBlock(p_149674_2_ - 1, p_149674_3_, p_149674_4_) == this.field_149877_a)
					{
						return;
					}

					if (p_149674_1_.getBlock(p_149674_2_ + 1, p_149674_3_, p_149674_4_) == this.field_149877_a)
					{
						return;
					}

					if (p_149674_1_.getBlock(p_149674_2_, p_149674_3_, p_149674_4_ - 1) == this.field_149877_a)
					{
						return;
					}

					if (p_149674_1_.getBlock(p_149674_2_, p_149674_3_, p_149674_4_ + 1) == this.field_149877_a)
					{
						return;
					}

					int i1 = p_149674_5_.nextInt(4);
					int j1 = p_149674_2_;
					int k1 = p_149674_4_;

					if (i1 == 0)
					{
						j1 = p_149674_2_ - 1;
					}

					if (i1 == 1)
					{
						++j1;
					}

					if (i1 == 2)
					{
						k1 = p_149674_4_ - 1;
					}

					if (i1 == 3)
					{
						++k1;
					}

					Block block = p_149674_1_.getBlock(j1, p_149674_3_ - 1, k1);

					if (p_149674_1_.isAirBlock(j1, p_149674_3_, k1) && (block.canSustainPlant(p_149674_1_, j1, p_149674_3_ - 1, k1, UP, this) || block == Blocks.dirt || block == Blocks.grass))
					{
						p_149674_1_.setBlock(j1, p_149674_3_, k1, this.field_149877_a);
					}
				}
			}
		}
	}

	// to avoid compilation errors
	Block field_149877_a;

	float func_149875_n(World world, int x, int y, int z)
	{
		return 0f;
	}

	protected BlockStemModifications(Block p_i45430_1_)
	{
		super(p_i45430_1_);
		// TODO Auto-generated constructor stub
	}
}
