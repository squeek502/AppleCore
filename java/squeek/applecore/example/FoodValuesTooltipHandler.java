package squeek.applecore.example;

import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import squeek.applecore.api.AppleCoreAPI;
import squeek.applecore.api.food.FoodValues;

import java.text.DecimalFormat;

public class FoodValuesTooltipHandler
{
	public static final DecimalFormat df = new DecimalFormat("##.##");

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onItemTooltip(ItemTooltipEvent event)
	{
		if (AppleCoreAPI.accessor.isFood(event.getItemStack()))
		{
			FoodValues unmodifiedValues = AppleCoreAPI.accessor.getUnmodifiedFoodValues(event.getItemStack());
			FoodValues modifiedValues = AppleCoreAPI.accessor.getFoodValues(event.getItemStack());
			FoodValues playerValues = AppleCoreAPI.accessor.getFoodValuesForPlayer(event.getItemStack(), event.getEntityPlayer());

			event.getToolTip().add("Food Values [hunger : satMod (+sat)]");
			event.getToolTip().add("- Player-specific: " + playerValues.hunger + " : " + playerValues.saturationModifier + " (+" + df.format(playerValues.getSaturationIncrement()) + ")");
			event.getToolTip().add("- Player-agnostic: " + modifiedValues.hunger + " : " + modifiedValues.saturationModifier + " (+" + df.format(modifiedValues.getSaturationIncrement()) + ")");
			event.getToolTip().add("- Unmodified: " + unmodifiedValues.hunger + " : " + unmodifiedValues.saturationModifier + " (+" + df.format(unmodifiedValues.getSaturationIncrement()) + ")");
		}
	}
}