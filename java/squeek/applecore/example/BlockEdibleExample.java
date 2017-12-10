package squeek.applecore.example;

import net.minecraft.block.BlockCake;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Optional;
import squeek.applecore.api.food.FoodValues;
import squeek.applecore.api.food.IEdibleBlock;
import squeek.applecore.api.food.ItemFoodProxy;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Optional.Interface(iface = "squeek.applecore.api.food.IEdibleBlock", modid = "applecore")
public class BlockEdibleExample extends BlockCake implements IEdibleBlock
{
	private boolean isEdibleAtMaxHunger = false;

	@Optional.Method(modid = "applecore")
	@Override
	public void setEdibleAtMaxHunger(boolean value)
	{
		isEdibleAtMaxHunger = value;
	}

	@Optional.Method(modid = "applecore")
	@Override
	public FoodValues getFoodValues(@Nonnull ItemStack itemStack)
	{
		return new FoodValues(2, 0.1f);
	}

	// This needs to be abstracted into an Optional method,
	// otherwise the ItemFoodProxy reference will cause problems
	@Optional.Method(modid = "applecore")
	public void onEatenCompatibility(ItemStack itemStack, EntityPlayer player)
	{
		// one possible compatible method
		player.getFoodStats().addStats(new ItemFoodProxy(this), itemStack);

		// another possible compatible method:
		// new ItemFoodProxy(this).onEaten(itemStack, player);
	}

	@Override
	public boolean onBlockActivated(@Nullable World world, @Nullable BlockPos pos, @Nullable IBlockState state, EntityPlayer player, @Nullable EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		if (!world.isRemote)
		{
			return this.eat(world, pos, state, player);
		}
		else
		{
			ItemStack itemstack = player.getHeldItem(hand);
			return this.eat(world, pos, state, player) || itemstack.isEmpty();
		}
	}

	private boolean eat(World world, BlockPos pos, IBlockState state, EntityPlayer player)
	{
		if (!player.canEat(isEdibleAtMaxHunger))
		{
			return false;
		}
		else
		{
			if (Loader.isModLoaded("applecore"))
			{
				onEatenCompatibility(new ItemStack(this), player);
			}
			else
			{
				// this method is not compatible with AppleCore
				player.getFoodStats().addStats(2, 0.1f);
			}

			int bites = state.getValue(BITES);
			if (bites < 6)
			{
				world.setBlockState(pos, state.withProperty(BITES, bites + 1), 3);
			}
			else
			{
				world.setBlockToAir(pos);
			}

			return true;
		}
	}
}
