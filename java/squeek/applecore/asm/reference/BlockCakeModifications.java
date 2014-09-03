package squeek.applecore.asm.reference;

import net.minecraft.block.BlockCake;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import squeek.applecore.api.food.FoodValues;
import squeek.applecore.asm.Hooks;

public class BlockCakeModifications extends BlockCake
{
	@SuppressWarnings("unused")
	private void func_150036_b(World p_150036_1_, int p_150036_2_, int p_150036_3_, int p_150036_4_, EntityPlayer p_150036_5_)
	{
		if (p_150036_5_.canEat(false))
		{
			// begin modifications
			FoodValues modifiedFoodValues = Hooks.onBlockFoodEaten(this, p_150036_1_, p_150036_5_);
			int prevFoodLevel = p_150036_5_.getFoodStats().getFoodLevel();
			float prevSaturationLevel = p_150036_5_.getFoodStats().getSaturationLevel();

			p_150036_5_.getFoodStats().addStats(modifiedFoodValues.hunger, modifiedFoodValues.saturationModifier);

			Hooks.onPostBlockFoodEaten(this, modifiedFoodValues, prevFoodLevel, prevSaturationLevel, p_150036_5_);
			// end modifications

			int l = p_150036_1_.getBlockMetadata(p_150036_2_, p_150036_3_, p_150036_4_) + 1;

			if (l >= 6)
			{
				p_150036_1_.setBlockToAir(p_150036_2_, p_150036_3_, p_150036_4_);
			}
			else
			{
				p_150036_1_.setBlockMetadataWithNotify(p_150036_2_, p_150036_3_, p_150036_4_, l, 2);
			}
		}
	}
}
