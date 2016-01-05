package squeek.applecore.example;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Optional;
import squeek.applecore.api.food.FoodValues;
import squeek.applecore.api.food.IEdible;
import squeek.applecore.api.food.ItemFoodProxy;

@Optional.Interface(iface = "squeek.applecore.api.food.IEdible", modid = "AppleCore")
public class ItemNonStandardFood extends Item implements IEdible
{
	public ItemNonStandardFood()
	{
	}

	@Optional.Method(modid = "AppleCore")
	@Override
	public FoodValues getFoodValues(ItemStack itemStack)
	{
		return new FoodValues(1, 1f);
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
	public ItemStack onItemUseFinish(ItemStack itemStack, World world, EntityPlayer player)
	{
		--itemStack.stackSize;

		if (Loader.isModLoaded("AppleCore"))
		{
			onEatenCompatibility(itemStack, player);
		}
		else
		{
			// this method is not compatible with AppleCore
			player.getFoodStats().addStats(1, 1f);
		}

		world.playSoundAtEntity(player, "random.burp", 0.5F, world.rand.nextFloat() * 0.1F + 0.9F);
		return itemStack;
	}

	@Override
	public EnumAction getItemUseAction(ItemStack itemStack)
	{
		return EnumAction.EAT;
	}

	@Override
	public int getMaxItemUseDuration(ItemStack itemStack)
	{
		return 32;
	}

	@Override
	public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer player)
	{
		if (player.canEat(true))
		{
			player.setItemInUse(itemStack, getMaxItemUseDuration(itemStack));
		}

		return itemStack;
	}
}