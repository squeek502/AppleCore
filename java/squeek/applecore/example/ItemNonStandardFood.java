package squeek.applecore.example;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import squeek.applecore.api.food.FoodValues;
import squeek.applecore.api.food.IEdible;
import squeek.applecore.api.food.ItemFoodProxy;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Optional;

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

	@Override
	public ItemStack onEaten(ItemStack itemStack, World world, EntityPlayer player)
	{
		--itemStack.stackSize;

		if (Loader.isModLoaded("AppleCore"))
		{
			// one possible compatible method
			player.getFoodStats().func_151686_a(new ItemFoodProxy(this), itemStack);

			// another possible compatible method:
			// new ItemFoodProxy(this).onEaten(itemStack, player);
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
		return EnumAction.eat;
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
