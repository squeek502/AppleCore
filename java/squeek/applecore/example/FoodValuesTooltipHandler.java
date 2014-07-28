package squeek.applecore.example;

import java.text.DecimalFormat;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import squeek.applecore.api.AppleCoreAccessor;
import squeek.applecore.api.food.FoodValues;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class FoodValuesTooltipHandler
{
	public static final DecimalFormat df = new DecimalFormat("##.##");

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onItemTooltip(ItemTooltipEvent event)
	{
		if (AppleCoreAccessor.get().isFood(event.itemStack))
		{
			FoodValues unmodifiedValues = AppleCoreAccessor.get().getUnmodifiedFoodValues(event.itemStack);
			FoodValues modifiedValues = AppleCoreAccessor.get().getFoodValues(event.itemStack);
			FoodValues playerValues = AppleCoreAccessor.get().getFoodValuesForPlayer(event.itemStack, event.entityPlayer);

			event.toolTip.add("Food Values [hunger : satMod (+sat)]");
			event.toolTip.add("- Player-specific: " + playerValues.hunger + " : " + playerValues.saturationModifier + " (+" + df.format(playerValues.getSaturationIncrement()) + ")");
			event.toolTip.add("- Player-agnostic: " + modifiedValues.hunger + " : " + modifiedValues.saturationModifier + " (+" + df.format(modifiedValues.getSaturationIncrement()) + ")");
			event.toolTip.add("- Unmodified: " + unmodifiedValues.hunger + " : " + unmodifiedValues.saturationModifier + " (+" + df.format(unmodifiedValues.getSaturationIncrement()) + ")");
		}
	}
}
