package squeek.applecore.example;

import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
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
			FoodValues unmodifiedValues = AppleCoreAPI.accessor.getUnmodifiedFoodValues(event.getItemStack());
			FoodValues modifiedValues = AppleCoreAPI.accessor.getFoodValues(event.getItemStack());
			FoodValues playerValues = AppleCoreAPI.accessor.getFoodValuesForPlayer(event.getItemStack(), event.getEntityPlayer());

			event.getToolTip().add(new StringTextComponent("Food Values [hunger : satMod (+sat)]"));
			event.getToolTip().add(new StringTextComponent("- Player-specific: " + playerValues.hunger + " : " + playerValues.saturationModifier + " (+" + DF.format(playerValues.getSaturationIncrement(event.getEntityPlayer())) + ")"));
			event.getToolTip().add(new StringTextComponent("- Player-agnostic: " + modifiedValues.hunger + " : " + modifiedValues.saturationModifier + " (+" + DF.format(modifiedValues.getSaturationIncrement(event.getEntityPlayer())) + ")"));
			event.getToolTip().add(new StringTextComponent("- Unmodified: " + unmodifiedValues.hunger + " : " + unmodifiedValues.saturationModifier + " (+" + DF.format(unmodifiedValues.getSaturationIncrement(event.getEntityPlayer())) + ")"));
		}
	}
}