package squeek.applecore.asm.reference;

import net.minecraft.block.BlockCake;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.stats.StatList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import squeek.applecore.api.food.FoodValues;
import squeek.applecore.asm.Hooks;

public class BlockCakeModifications extends BlockCake
{
	@SuppressWarnings("unused")
	private void eatCake(World world, BlockPos pos, IBlockState state, EntityPlayer player)
	{
		if (player.canEat(false))
		{
			// begin modifications
			FoodValues modifiedFoodValues = Hooks.onBlockFoodEaten(this, world, player);
			int prevFoodLevel = player.getFoodStats().getFoodLevel();
			float prevSaturationLevel = player.getFoodStats().getSaturationLevel();

			player.getFoodStats().addStats(modifiedFoodValues.hunger, modifiedFoodValues.saturationModifier);

			Hooks.onPostBlockFoodEaten(this, modifiedFoodValues, prevFoodLevel, prevSaturationLevel, player);
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
		}
	}
}