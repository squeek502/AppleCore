package squeek.applecore.asm.reference;

import net.minecraft.block.BlockCake;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import squeek.applecore.api.food.FoodValues;
import squeek.applecore.api.food.IEdibleBlock;
import squeek.applecore.asm.Hooks;

public class BlockCakeModifications extends BlockCake /* implemented by AppleCore */ implements IEdibleBlock
{
	@SuppressWarnings("unused")
	private boolean eatCake(World world, BlockPos pos, IBlockState state, EntityPlayer player)
	{
		if (!player.canEat(AppleCore_isEdibleAtMaxHunger)) // modified (changed false to AppleCore_isEdibleAtMaxHunger
		{
			return false;
		}
		else
		{
			// begin modifications
			Hooks.onBlockFoodEaten(this, world, player);
			// end modifications

			player.addStat(StatList.CAKE_SLICES_EATEN);

			int i = state.getValue(BITES);

			if (i < 6)
			{
				world.setBlockState(pos, state.withProperty(BITES, i + 1), 3);
			}
			else
			{
				world.setBlockToAir(pos);
			}

			return true;
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
