package squeek.applecore.asm.reference;

import net.minecraft.block.BlockCake;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import squeek.applecore.api.food.FoodValues;
import squeek.applecore.api.food.IEdibleBlock;
import squeek.applecore.asm.Hooks;

public class BlockCakeModifications extends BlockCake /* implemented by AppleCore */ implements IEdibleBlock
{
	@SuppressWarnings("unused")
	private void eatCake(World world, BlockPos pos, IBlockState state, EntityPlayer player)
	{
		if (player.canEat(AppleCore_isEdibleAtMaxHunger)) // modified (changed false to AppleCore_isEdibleAtMaxHunger
		{
			// begin modifications
			Hooks.onBlockFoodEaten(this, world, player);
			// end modifications

			player.triggerAchievement(StatList.field_181724_H);

			int i = (state.getValue(BITES)).intValue();

			if (i < 6)
			{
				world.setBlockState(pos, state.withProperty(BITES, Integer.valueOf(i + 1)), 3);
			}
			else
			{
				world.setBlockToAir(pos);
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
