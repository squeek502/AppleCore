package squeek.applecore.asm.reference;

import net.minecraft.block.BlockCake;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import squeek.applecore.api.food.FoodValues;
import squeek.applecore.api.food.IEdibleBlock;
import squeek.applecore.asm.Hooks;

public class BlockCakeModifications extends BlockCake /* implemented by AppleCore */ implements IEdibleBlock
{
	@SuppressWarnings("unused")
	private void func_150036_b(World p_150036_1_, int p_150036_2_, int p_150036_3_, int p_150036_4_, EntityPlayer p_150036_5_)
	{
		if (p_150036_5_.canEat(AppleCore_isEdibleAtMaxHunger)) // modified (changed false to AppleCore_isEdibleAtMaxHunger
		{
			// begin modifications
			Hooks.onBlockFoodEaten(this, p_150036_1_, p_150036_5_);
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

	// All of the following added by AppleCore
	private boolean AppleCore_isEdibleAtMaxHunger;

	@Override
	public void setEdibleAtMaxHunger(boolean value)
	{
		AppleCore_isEdibleAtMaxHunger = value;
	}

	@Override
	public FoodValues getFoodValues(ItemStack itemStack)
	{
		return new FoodValues(2, 0.1f);
	}
}
