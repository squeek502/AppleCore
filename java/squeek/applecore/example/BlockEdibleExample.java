package squeek.applecore.example;

import net.minecraft.block.BlockCake;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Optional;
import squeek.applecore.api.food.FoodValues;
import squeek.applecore.api.food.IEdibleBlock;
import squeek.applecore.api.food.ItemFoodProxy;

@Optional.Interface(iface = "squeek.applecore.api.food.IEdibleBlock", modid = "AppleCore")
public class BlockEdibleExample extends BlockCake implements IEdibleBlock
{
	private boolean isEdibleAtMaxHunger = false;

	@Optional.Method(modid = "AppleCore")
	@Override
	public void setEdibleAtMaxHunger(boolean value)
	{
		isEdibleAtMaxHunger = value;
	}

	@Optional.Method(modid = "AppleCore")
	@Override
	public FoodValues getFoodValues(ItemStack itemStack)
	{
		return new FoodValues(2, 0.1f);
	}

	// This needs to be abstracted into an Optional method,
	// otherwise the ItemFoodProxy reference will cause problems
	@Optional.Method(modid = "AppleCore")
	public void onEatenCompatibility(ItemStack itemStack, EntityPlayer player)
	{
		// one possible compatible method
		player.getFoodStats().addStats(new ItemFoodProxy(this), itemStack);

		// another possible compatible method:
		// new ItemFoodProxy(this).onEaten(itemStack, player);
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		eat(world, pos, state, player);
		return true;
	}

	@Override
	public void onBlockClicked(World world, BlockPos pos, EntityPlayer player)
	{
		eat(world, pos, world.getBlockState(pos), player);
	}

	private void eat(World world, BlockPos pos, IBlockState state, EntityPlayer player)
	{
		if (player.canEat(isEdibleAtMaxHunger))
		{
			if (Loader.isModLoaded("AppleCore"))
			{
				onEatenCompatibility(new ItemStack(this), player);
			}
			else
			{
				// this method is not compatible with AppleCore
				player.getFoodStats().addStats(2, 0.1f);
			}

			int bites = state.getValue(BITES);
			if(bites < 6) {
				world.setBlockState(pos, state.withProperty(BITES, bites + 1), 3);
			} else {
				world.setBlockToAir(pos);
			}
		}
	}
}
