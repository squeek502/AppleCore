package squeek.applecore.example;

import java.text.DecimalFormat;
import net.minecraft.item.ItemFood;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import squeek.applecore.api.food.FoodValues;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class FoodValuesTooltipHandler
{
	public static final DecimalFormat df = new DecimalFormat("##.##");

	@SubscribeEvent(priority=EventPriority.LOWEST)
	public void onItemTooltip(ItemTooltipEvent event)
	{
		if (event.itemStack.getItem() instanceof ItemFood)
		{
			FoodValues unmodifiedValues = FoodValues.getUnmodified(event.itemStack);
			FoodValues modifiedValues = FoodValues.get(event.itemStack);
			FoodValues playerValues = FoodValues.get(event.itemStack, event.entityPlayer);

			event.toolTip.add("Food Values [hunger : satMod (+sat)]");
			event.toolTip.add("- Player-specific: " + playerValues.hunger + " : " + playerValues.saturationModifier + " (+" + df.format(playerValues.getSaturationIncrement()) + ")");
			event.toolTip.add("- Player-agnostic: " + modifiedValues.hunger + " : " + modifiedValues.saturationModifier + " (+" + df.format(modifiedValues.getSaturationIncrement()) + ")");
			event.toolTip.add("- Unmodified: " + unmodifiedValues.hunger + " : " + unmodifiedValues.saturationModifier + " (+" + df.format(unmodifiedValues.getSaturationIncrement()) + ")");
		}
	}
}
