package squeek.applecore.example;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import squeek.applecore.api.AppleCoreAPI;
import squeek.applecore.api.food.FoodValues;

import java.text.DecimalFormat;

public class FoodValuesTooltipHandler
{
	public static final DecimalFormat DF = new DecimalFormat("##.##");

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onItemTooltip(ItemTooltipEvent event)
	{
		if (AppleCoreAPI.accessor.isFood(event.getItemStack()))
		{
			ItemStack stack = event.getItemStack();
			PlayerEntity player = event.getPlayer();
			FoodValues unmodifiedValues = AppleCoreAPI.accessor.getUnmodifiedFoodValues(stack);
			FoodValues modifiedValues = AppleCoreAPI.accessor.getFoodValues(stack);
			FoodValues playerValues = AppleCoreAPI.accessor.getFoodValuesForPlayer(stack, player);

			event.getToolTip().add(new StringTextComponent("Food Values [hunger : satMod (+sat)]"));
			event.getToolTip().add(new StringTextComponent("- Player-specific: " + playerValues.hunger + " : " + playerValues.saturationModifier + " (+" + DF.format(playerValues.getSaturationIncrement(player)) + ")"));
			event.getToolTip().add(new StringTextComponent("- Player-agnostic: " + modifiedValues.hunger + " : " + modifiedValues.saturationModifier + " (+" + DF.format(modifiedValues.getSaturationIncrement(player)) + ")"));
			event.getToolTip().add(new StringTextComponent("- Unmodified: " + unmodifiedValues.hunger + " : " + unmodifiedValues.saturationModifier + " (+" + DF.format(unmodifiedValues.getSaturationIncrement(player)) + ")"));
		}
	}
}