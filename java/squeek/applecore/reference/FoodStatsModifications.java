package squeek.applecore.reference;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.FoodStats;
import net.minecraft.world.EnumDifficulty;
import squeek.applecore.Hooks;
import squeek.applecore.api.food.FoodValues;
import squeek.applecore.api.hunger.ExhaustionEvent;
import squeek.applecore.api.hunger.HealthRegenEvent;
import squeek.applecore.api.hunger.StarvationEvent;
import cpw.mods.fml.common.eventhandler.Event.Result;

/*
 * The end result of the changes made by ModuleFoodStats
 */
public class FoodStatsModifications extends FoodStats
{
	// added fields
	// actually is EntityPlayer
	EntityPlayer player;
	int starveTimer;

	// added constructor
	// actually is EntityPlayer
	public FoodStatsModifications(EntityPlayer player)
	{
		this.player = player;
	}

	// hook injected into method
	@Override
	public void func_151686_a(ItemFood p_151686_1_, ItemStack p_151686_2_)
	{
		FoodValues modifiedFoodValues;
		if ((modifiedFoodValues = Hooks.onFoodStatsAdded(this, p_151686_1_, p_151686_2_, this.player)) != null)
		{
			int prevFoodLevel = this.foodLevel;
			float prevSaturationLevel = this.foodSaturationLevel;

			this.addStats(modifiedFoodValues.hunger, modifiedFoodValues.saturationModifier);

			Hooks.onPostFoodStatsAdded(this, p_151686_1_, p_151686_2_, modifiedFoodValues, this.foodLevel - prevFoodLevel, this.foodSaturationLevel - prevSaturationLevel, this.player);
			return;
		}

		// default code
	}

	// heavily modified method
	@Override
	@SuppressWarnings("unused")
	public void onUpdate(EntityPlayer player)
	{
		EnumDifficulty enumdifficulty = player.worldObj.difficultySetting;
		this.prevFoodLevel = this.foodLevel;

		ExhaustionEvent.Tick exhaustionTickEvent = Hooks.fireExhaustionTickEvent(player, foodExhaustionLevel);
		this.foodExhaustionLevel = exhaustionTickEvent.exhaustionLevel;
		if (!exhaustionTickEvent.isCanceled() && this.foodExhaustionLevel >= exhaustionTickEvent.maxExhaustionLevel)
		{
			ExhaustionEvent.MaxReached exhaustionMaxEvent = Hooks.fireExhaustionMaxEvent(player, exhaustionTickEvent.maxExhaustionLevel, foodExhaustionLevel);

			if (!exhaustionMaxEvent.isCanceled())
			{
				this.foodExhaustionLevel += exhaustionMaxEvent.deltaExhaustion;
				this.foodSaturationLevel = Math.max(this.foodSaturationLevel + exhaustionMaxEvent.deltaSaturation, 0.0F);
				this.foodLevel = Math.max(this.foodLevel + exhaustionMaxEvent.deltaHunger, 0);
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
	int foodLevel;
	float foodSaturationLevel;
	int prevFoodLevel;
	int foodTimer;
	float foodExhaustionLevel;
	// end unmodified

}
