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
		if (AppleCoreAPI.accessor.isFood(event.itemStack))
		{
			FoodValues unmodifiedValues = AppleCoreAPI.accessor.getUnmodifiedFoodValues(event.itemStack);
			FoodValues modifiedValues = AppleCoreAPI.accessor.getFoodValues(event.itemStack);
			FoodValues playerValues = AppleCoreAPI.accessor.getFoodValuesForPlayer(event.itemStack, event.entityPlayer);

			event.toolTip.add("Food Values [hunger : satMod (+sat)]");
			event.toolTip.add("- Player-specific: " + playerValues.hunger + " : " + playerValues.saturationModifier + " (+" + df.format(playerValues.getSaturationIncrement()) + ")");
			event.toolTip.add("- Player-agnostic: " + modifiedValues.hunger + " : " + modifiedValues.saturationModifier + " (+" + df.format(modifiedValues.getSaturationIncrement()) + ")");
			event.toolTip.add("- Unmodified: " + unmodifiedValues.hunger + " : " + unmodifiedValues.saturationModifier + " (+" + df.format(unmodifiedValues.getSaturationIncrement()) + ")");
		}
	}
}