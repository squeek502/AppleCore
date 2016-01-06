package squeek.applecore.asm.reference;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.FoodStats;
import net.minecraft.world.EnumDifficulty;
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

	// heavily modified method
	@SuppressWarnings("unused")
	@Override
	public void onUpdate(EntityPlayer player)
	{
		EnumDifficulty enumdifficulty = player.worldObj.getDifficulty();
		this.prevFoodLevel = this.foodLevel;

		Result allowExhaustionResult = Hooks.fireAllowExhaustionEvent(player);
		float maxExhaustion = Hooks.fireExhaustionTickEvent(player, foodExhaustionLevel);
		if (allowExhaustionResult == Result.ALLOW || (allowExhaustionResult == Result.DEFAULT && this.foodExhaustionLevel >= maxExhaustion))
		{
			ExhaustionEvent.Exhausted exhaustedEvent = Hooks.fireExhaustionMaxEvent(player, maxExhaustion, foodExhaustionLevel);

			this.foodExhaustionLevel += exhaustedEvent.deltaExhaustion;
			if (!exhaustedEvent.isCanceled())
			{
				this.foodSaturationLevel = Math.max(this.foodSaturationLevel + exhaustedEvent.deltaSaturation, 0.0F);
				this.foodLevel = Math.max(this.foodLevel + exhaustedEvent.deltaHunger, 0);
			}
		}

		Result allowRegenResult = Hooks.fireAllowRegenEvent(player);
		if (allowRegenResult == Result.ALLOW || (allowRegenResult == Result.DEFAULT && player.worldObj.getGameRules().getBoolean("naturalRegeneration") && this.foodLevel >= 18 && player.shouldHeal()))
		{
			++this.foodTimer;

			if (this.foodTimer >= Hooks.fireRegenTickEvent(player))
			{
				HealthRegenEvent.Regen regenEvent = Hooks.fireRegenEvent(player);
				if (!regenEvent.isCanceled())
				{
					player.heal(regenEvent.deltaHealth);
					this.addExhaustion(regenEvent.deltaExhaustion);
				}
				this.foodTimer = 0;
			}
		}
		else
		{
			this.foodTimer = 0;
		}

		Result allowStarvationResult = Hooks.fireAllowStarvation(player);
		if (allowStarvationResult == Result.ALLOW || (allowStarvationResult == Result.DEFAULT && this.foodLevel <= 0))
		{
			++this.starveTimer;

			if (this.starveTimer >= Hooks.fireStarvationTickEvent(player))
			{
				StarvationEvent.Starve starveEvent = Hooks.fireStarveEvent(player);
				if (!starveEvent.isCanceled())
				{
					player.attackEntityFrom(DamageSource.starve, starveEvent.starveDamage);
				}
				this.starveTimer = 0;
			}
		}
		else
		{
			this.starveTimer = 0;
		}
	}

	// start unmodified
	int foodLevel = 20;
	float foodSaturationLevel = 5f;
	int prevFoodLevel = 20;
	int foodTimer;
	float foodExhaustionLevel;
	// end unmodified

}