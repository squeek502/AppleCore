package squeek.applecore.asm;

import net.minecraft.block.Block;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.UseAction;
import net.minecraft.util.DamageSource;
import net.minecraft.util.FoodStats;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event.Result;
import squeek.applecore.api.AppleCoreAPI;
import squeek.applecore.api.food.FoodEvent;
import squeek.applecore.api.food.FoodValues;
import squeek.applecore.api.hunger.ExhaustionEvent;
import squeek.applecore.api.hunger.HealthRegenEvent;
import squeek.applecore.api.hunger.HungerRegenEvent;
import squeek.applecore.api.hunger.StarvationEvent;
import squeek.applecore.asm.util.IAppleCoreFoodStats;

import javax.annotation.Nonnull;

public class Hooks
{
	private static void verifyFoodStats(FoodStats foodStats, PlayerEntity player)
	{
		if (!(foodStats instanceof IAppleCoreFoodStats))
		{
			String playerName = player != null ? player.getName().getString() : "<unknown>";
			String className = foodStats.getClass().getName();
			throw new RuntimeException("FoodStats does not implement IAppleCoreFoodStats on player '" + playerName + "' (class = " + className + "). This likely means that AppleCore has failed catastrophically in some way.");
		}
		if (((IAppleCoreFoodStats) foodStats).getPlayer() == null)
		{
			// attempt to salvage the situation by setting the field here
			if (player != null)
			{
				((IAppleCoreFoodStats) foodStats).setPlayer(player);
				return;
			}
			String playerName = "<unknown>";
			String className = foodStats.getClass().getName();
			throw new RuntimeException("FoodStats has a null player field (this field is added by AppleCore at runtime) on player '" + playerName + "' (class = " + className + "). This likely means that some mod has overloaded FoodStats, which is incompatible with AppleCore.");
		}
	}

	public static boolean onAppleCoreFoodStatsUpdate(FoodStats foodStats, PlayerEntity player)
	{
		verifyFoodStats(foodStats, player);

		IAppleCoreFoodStats appleCoreFoodStats = (IAppleCoreFoodStats) foodStats;

		appleCoreFoodStats.setPrevFoodLevel(foodStats.getFoodLevel());

		Result allowExhaustionResult = Hooks.fireAllowExhaustionEvent(player);
		float maxExhaustion = Hooks.fireExhaustionTickEvent(player, appleCoreFoodStats.getExhaustion());
		if (allowExhaustionResult == Result.ALLOW || (allowExhaustionResult == Result.DEFAULT && appleCoreFoodStats.getExhaustion() >= maxExhaustion))
		{
			ExhaustionEvent.Exhausted exhaustedEvent = Hooks.fireExhaustionMaxEvent(player, maxExhaustion, appleCoreFoodStats.getExhaustion());

			appleCoreFoodStats.setExhaustion(appleCoreFoodStats.getExhaustion() + exhaustedEvent.deltaExhaustion);
			if (!exhaustedEvent.isCanceled())
			{
				appleCoreFoodStats.setSaturation(Math.max(foodStats.getSaturationLevel() + exhaustedEvent.deltaSaturation, 0.0F));
				foodStats.setFoodLevel(Math.max(foodStats.getFoodLevel() + exhaustedEvent.deltaHunger, 0));
			}
		}

		boolean hasNaturalRegen = player.world.getGameRules().getBoolean(GameRules.NATURAL_REGENERATION);

		Result allowSaturatedRegenResult = Hooks.fireAllowSaturatedRegenEvent(player);
		boolean shouldDoSaturatedRegen = allowSaturatedRegenResult == Result.ALLOW || (allowSaturatedRegenResult == Result.DEFAULT && hasNaturalRegen && foodStats.getSaturationLevel() > 0.0F && player.shouldHeal() && foodStats.getFoodLevel() >= 20);
		Result allowRegenResult = shouldDoSaturatedRegen ? Result.DENY : Hooks.fireAllowRegenEvent(player);
		boolean shouldDoRegen = allowRegenResult == Result.ALLOW || (allowRegenResult == Result.DEFAULT && hasNaturalRegen && foodStats.getFoodLevel() >= 18 && player.shouldHeal());
		if (shouldDoSaturatedRegen)
		{
			appleCoreFoodStats.setFoodTimer(appleCoreFoodStats.getFoodTimer() + 1);

			if (appleCoreFoodStats.getFoodTimer() >= Hooks.fireSaturatedRegenTickEvent(player))
			{
				HealthRegenEvent.SaturatedRegen saturatedRegenEvent = Hooks.fireSaturatedRegenEvent(player);
				if (!saturatedRegenEvent.isCanceled())
				{
					player.heal(saturatedRegenEvent.deltaHealth);
					foodStats.addExhaustion(saturatedRegenEvent.deltaExhaustion);
				}
				appleCoreFoodStats.setFoodTimer(0);
			}
		}
		else if (shouldDoRegen)
		{
			appleCoreFoodStats.setFoodTimer(appleCoreFoodStats.getFoodTimer() + 1);

			if (appleCoreFoodStats.getFoodTimer() >= Hooks.fireRegenTickEvent(player))
			{
				HealthRegenEvent.Regen regenEvent = Hooks.fireRegenEvent(player);
				if (!regenEvent.isCanceled())
				{
					player.heal(regenEvent.deltaHealth);
					foodStats.addExhaustion(regenEvent.deltaExhaustion);
				}
				appleCoreFoodStats.setFoodTimer(0);
			}
		}
		else
		{
			appleCoreFoodStats.setFoodTimer(0);
		}

		Result allowStarvationResult = Hooks.fireAllowStarvation(player);
		if (allowStarvationResult == Result.ALLOW || (allowStarvationResult == Result.DEFAULT && foodStats.getFoodLevel() <= 0))
		{
			appleCoreFoodStats.setStarveTimer(appleCoreFoodStats.getStarveTimer() + 1);

			if (appleCoreFoodStats.getStarveTimer() >= Hooks.fireStarvationTickEvent(player))
			{
				StarvationEvent.Starve starveEvent = Hooks.fireStarveEvent(player);
				if (!starveEvent.isCanceled())
				{
					player.attackEntityFrom(DamageSource.STARVE, starveEvent.starveDamage);
				}
				appleCoreFoodStats.setStarveTimer(0);
			}
		}
		else
		{
			appleCoreFoodStats.setStarveTimer(0);
		}

		return true;
	}

	public static FoodValues onFoodStatsAdded(FoodStats foodStats, @Nonnull Item itemFood, @Nonnull ItemStack itemStack, PlayerEntity player)
	{
		verifyFoodStats(foodStats, player);
		return AppleCoreAPI.accessor.getFoodValuesForPlayer(itemStack, player);
	}

	public static void onPostFoodStatsAdded(FoodStats foodStats, @Nonnull Item itemFood, @Nonnull ItemStack itemStack, FoodValues foodValues, int hungerAdded, float saturationAdded, PlayerEntity player)
	{
		verifyFoodStats(foodStats, player);
		MinecraftForge.EVENT_BUS.post(new FoodEvent.FoodEaten(player, itemStack, foodValues, hungerAdded, saturationAdded));
	}

	public static int getItemInUseMaxCount(LivingEntity entityLiving, int savedMaxDuration)
	{
		UseAction useAction = entityLiving.getActiveItemStack().getUseAction();
		if (useAction == UseAction.EAT || useAction == UseAction.DRINK)
			return savedMaxDuration;
		else
			return entityLiving.getActiveItemStack().getUseDuration();
	}

	public static void onBlockFoodEaten(Block block, World world, PlayerEntity player)
	{
		squeek.applecore.api.food.IEdible edibleBlock = (squeek.applecore.api.food.IEdible) block;
		ItemStack itemStack = new ItemStack(AppleCoreAPI.registry.getItemFromEdibleBlock(block));
		player.getFoodStats().consume(itemStack.getItem(), itemStack);
	}

	public static Result fireAllowExhaustionEvent(PlayerEntity player)
	{
		ExhaustionEvent.AllowExhaustion event = new ExhaustionEvent.AllowExhaustion(player);
		MinecraftForge.EVENT_BUS.post(event);
		return event.getResult();
	}

	public static float fireExhaustionTickEvent(PlayerEntity player, float foodExhaustionLevel)
	{
		return AppleCoreAPI.accessor.getMaxExhaustion(player);
	}

	public static ExhaustionEvent.Exhausted fireExhaustionMaxEvent(PlayerEntity player, float maxExhaustionLevel, float foodExhaustionLevel)
	{
		ExhaustionEvent.Exhausted event = new ExhaustionEvent.Exhausted(player, maxExhaustionLevel, foodExhaustionLevel);
		MinecraftForge.EVENT_BUS.post(event);
		return event;
	}

	public static Result fireAllowRegenEvent(PlayerEntity player)
	{
		HealthRegenEvent.AllowRegen event = new HealthRegenEvent.AllowRegen(player);
		MinecraftForge.EVENT_BUS.post(event);
		return event.getResult();
	}

	public static Result fireAllowSaturatedRegenEvent(PlayerEntity player)
	{
		HealthRegenEvent.AllowSaturatedRegen event = new HealthRegenEvent.AllowSaturatedRegen(player);
		MinecraftForge.EVENT_BUS.post(event);
		return event.getResult();
	}

	public static int fireRegenTickEvent(PlayerEntity player)
	{
		return AppleCoreAPI.accessor.getHealthRegenTickPeriod(player);
	}

	public static int fireSaturatedRegenTickEvent(PlayerEntity player)
	{
		return AppleCoreAPI.accessor.getSaturatedHealthRegenTickPeriod(player);
	}

	public static HealthRegenEvent.Regen fireRegenEvent(PlayerEntity player)
	{
		HealthRegenEvent.Regen event = new HealthRegenEvent.Regen(player);
		MinecraftForge.EVENT_BUS.post(event);
		return event;
	}

	public static HealthRegenEvent.SaturatedRegen fireSaturatedRegenEvent(PlayerEntity player)
	{
		HealthRegenEvent.SaturatedRegen event = new HealthRegenEvent.SaturatedRegen(player);
		MinecraftForge.EVENT_BUS.post(event);
		return event;
	}

	public static HealthRegenEvent.PeacefulRegen firePeacefulRegenEvent(PlayerEntity player)
	{
		HealthRegenEvent.PeacefulRegen event = new HealthRegenEvent.PeacefulRegen(player);
		MinecraftForge.EVENT_BUS.post(event);
		return event;
	}

	public static HungerRegenEvent.PeacefulRegen firePeacefulHungerRegenEvent(PlayerEntity player)
	{
		HungerRegenEvent.PeacefulRegen event = new HungerRegenEvent.PeacefulRegen(player);
		MinecraftForge.EVENT_BUS.post(event);
		return event;
	}

	public static Result fireAllowStarvation(PlayerEntity player)
	{
		StarvationEvent.AllowStarvation event = new StarvationEvent.AllowStarvation(player);
		MinecraftForge.EVENT_BUS.post(event);
		return event.getResult();
	}

	public static int fireStarvationTickEvent(PlayerEntity player)
	{
		return AppleCoreAPI.accessor.getStarveDamageTickPeriod(player);
	}

	public static StarvationEvent.Starve fireStarveEvent(PlayerEntity player)
	{
		StarvationEvent.Starve event = new StarvationEvent.Starve(player);
		MinecraftForge.EVENT_BUS.post(event);
		return event;
	}

	public static boolean fireFoodStatsAdditionEvent(PlayerEntity player, FoodValues foodValuesToBeAdded)
	{
		FoodEvent.FoodStatsAddition event = new FoodEvent.FoodStatsAddition(player, foodValuesToBeAdded);
		MinecraftForge.EVENT_BUS.post(event);
		return event.isCancelable() && event.isCanceled();
	}

	public static boolean needFood(FoodStats foodStats)
	{
		verifyFoodStats(foodStats, null);
		return foodStats.getFoodLevel() < getMaxHunger(foodStats);
	}

	public static int getMaxHunger(FoodStats foodStats)
	{
		verifyFoodStats(foodStats, null);

		PlayerEntity player = ((IAppleCoreFoodStats) foodStats).getPlayer();
		return AppleCoreAPI.accessor.getMaxHunger(player);
	}

	@OnlyIn(Dist.CLIENT)
	public static int getHungerForDisplay(FoodStats foodStats)
	{
		if (!(foodStats instanceof IAppleCoreFoodStats))
			return foodStats.getFoodLevel();

		// return a scaled value so that the HUD can still use the same logic
		// as if the max was 20
		PlayerEntity player = ((IAppleCoreFoodStats) foodStats).getPlayer();
		float scale = 20f / AppleCoreAPI.accessor.getMaxHunger(player);
		int realHunger = foodStats.getFoodLevel();

		// only return 0 if the real hunger value is 0
		if (realHunger == 0)
			return 0;

		// floor here so that full hunger is only drawn when its actually maxed
		int scaledHunger = MathHelper.floor(realHunger * scale);

		// hunger is always some non-zero value here, so return at least one
		// to make sure we don't draw 0 hunger when we're not actually
		// starving
		return Math.max(scaledHunger, 1);
	}

	public static float onExhaustionAdded(FoodStats foodStats, float deltaExhaustion)
	{
		verifyFoodStats(foodStats, null);

		PlayerEntity player = ((IAppleCoreFoodStats) foodStats).getPlayer();
		ExhaustionEvent.ExhaustionAddition event = new ExhaustionEvent.ExhaustionAddition(player, deltaExhaustion);
		MinecraftForge.EVENT_BUS.post(event);
		return event.deltaExhaustion;
	}

	public static float fireExhaustingActionEvent(PlayerEntity player, ExhaustionEvent.ExhaustingActions source, float deltaExhaustion)
	{
		ExhaustionEvent.ExhaustingAction event = new ExhaustionEvent.ExhaustingAction(player, source, deltaExhaustion);
		MinecraftForge.EVENT_BUS.post(event);
		return event.deltaExhaustion;
	}
}