package squeek.applecore.example;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Optional;
import net.minecraft.block.BlockCake;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
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
		player.getFoodStats().func_151686_a(new ItemFoodProxy(this), itemStack);

		// another possible compatible method:
		// new ItemFoodProxy(this).onEaten(itemStack, player);
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ)
	{
		eat(world, x, y, z, player);
		return true;
	}

	@Override
	public void onBlockClicked(World world, int x, int y, int z, EntityPlayer player)
	{
		eat(world, x, y, z, player);
	}

	private void eat(World world, int x, int y, int z, EntityPlayer player)
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

			int nextMeta = world.getBlockMetadata(x, y, z) + 1;

			if (nextMeta >= 6)
				world.setBlockToAir(x, y, z);
			else
				world.setBlockMetadataWithNotify(x, y, z, nextMeta, 2);
		}
	}
}
