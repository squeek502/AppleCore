package squeek.applecore.asm.reference;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.FoodStats;
import squeek.applecore.api.food.FoodValues;
import squeek.applecore.asm.Hooks;

import javax.annotation.Nonnull;

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
			// 20 replaced with getMaxHunger
			this.foodLevel = Math.min(foodLevel + this.foodLevel, Hooks.getMaxHunger(this));
			this.foodSaturationLevel = Math.min(this.foodSaturationLevel + foodLevel * foodSaturationModifier * 2.0F, this.foodLevel);
		}
	}

	// hooks injected into method
	@Override
	public void addStats(@Nonnull ItemFood food, @Nonnull ItemStack stack)
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

	@Override
	public void addExhaustion(float exhaustion)
	{
		exhaustion = Hooks.onExhaustionAdded(this, exhaustion);

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