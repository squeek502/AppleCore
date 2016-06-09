package squeek.applecore.asm.reference;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.FoodStats;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import squeek.applecore.api.food.FoodValues;
import squeek.applecore.api.hunger.ExhaustionEvent;
import squeek.applecore.api.hunger.HealthRegenEvent;
import squeek.applecore.api.hunger.StarvationEvent;
import squeek.applecore.asm.Hooks;

/*
 * The end result of the changes made by ModuleFoodStats
 */
public class FoodStatsModifications extends FoodStats
{
	// added fields
	EntityPlayer player;
	int starveTimer;

	// added constructor
	public FoodStatsModifications(EntityPlayer player)
	{
		this.player = player;
	}

	// default code wrapped in a conditional
	@Override
	public void addStats(int foodLevel, float foodSaturationModifier)
	{
		if (!Hooks.fireFoodStatsAdditionEvent(player, new FoodValues(foodLevel, foodSaturationModifier)))
		{
			this.foodLevel = Math.min(foodLevel + this.foodLevel, 20);
			this.foodSaturationLevel = Math.min(this.foodSaturationLevel + foodLevel * foodSaturationModifier * 2.0F, this.foodLevel);
		}
	}

	// hooks injected into method
	@Override
	public void addStats(ItemFood food, ItemStack stack)
	{
		// added lines
		FoodValues modifiedFoodValues = Hooks.onFoodStatsAdded(this, food, stack, this.player);
		int prevFoodLevel = this.foodLevel;
		float prevSaturationLevel = this.foodSaturationLevel;

		// this is a default line that has been altered to use the modified food values
		this.addStats(modifiedFoodValues.hunger, modifiedFoodValues.saturationModifier);

		// added lines
		Hooks.onPostFoodStatsAdded(this, food, stack, modifiedFoodValues, this.foodLevel - prevFoodLevel, this.foodSaturationLevel - prevSaturationLevel, this.player);
	}

	@Override
	public void onUpdate(EntityPlayer player)
	{
		// added lines
		if (Hooks.onAppleCoreFoodStatsUpdate(this, player))
			return;

		// the body of the base function
	}

	// start unmodified
	int foodLevel = 20;
	float foodSaturationLevel = 5f;
	int prevFoodLevel = 20;
	int foodTimer;
	float foodExhaustionLevel;
	// end unmodified

}