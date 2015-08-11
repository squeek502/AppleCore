package squeek.applecore.asm.reference;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.FoodStats;
import net.minecraft.world.EnumDifficulty;
import squeek.applecore.api.food.FoodValues;
import squeek.applecore.api.hunger.ExhaustionEvent;
import squeek.applecore.api.hunger.HealthRegenEvent;
import squeek.applecore.api.hunger.StarvationEvent;
import squeek.applecore.asm.Hooks;
import cpw.mods.fml.common.eventhandler.Event.Result;

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
	public void addStats(int p_75122_1_, float p_75122_2_)
	{
		if (!Hooks.fireFoodStatsAdditionEvent(player, new FoodValues(p_75122_1_, p_75122_2_)))
		{
			this.foodLevel = Math.min(p_75122_1_ + this.foodLevel, 20);
			this.foodSaturationLevel = Math.min(this.foodSaturationLevel + p_75122_1_ * p_75122_2_ * 2.0F, this.foodLevel);
		}
	}

	// hooks injected into method
	@Override
	public void func_151686_a(ItemFood p_151686_1_, ItemStack p_151686_2_)
	{
		// added lines
		FoodValues modifiedFoodValues = Hooks.onFoodStatsAdded(this, p_151686_1_, p_151686_2_, this.player);
		int prevFoodLevel = this.foodLevel;
		float prevSaturationLevel = this.foodSaturationLevel;

		// this is a default line that has been altered to use the modified food values
		this.addStats(modifiedFoodValues.hunger, modifiedFoodValues.saturationModifier);

		// added lines
		Hooks.onPostFoodStatsAdded(this, p_151686_1_, p_151686_2_, modifiedFoodValues, this.foodLevel - prevFoodLevel, this.foodSaturationLevel - prevSaturationLevel, this.player);
	}

	// heavily modified method
	@SuppressWarnings("unused")
	@Override
	public void onUpdate(EntityPlayer player)
	{
		EnumDifficulty enumdifficulty = player.worldObj.difficultySetting;
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
		if (allowRegenResult == Result.ALLOW || (allowRegenResult == Result.DEFAULT && player.worldObj.getGameRules().getGameRuleBooleanValue("naturalRegeneration") && this.foodLevel >= 18 && player.shouldHeal()))
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
