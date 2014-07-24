package squeek.applecore.api;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.world.EnumDifficulty;
import cpw.mods.fml.common.eventhandler.Cancelable;
import cpw.mods.fml.common.eventhandler.Event;

public abstract class FoodEvent extends Event
{
	public EntityPlayer player;

	public FoodEvent(EntityPlayer player)
	{
		this.player = player;
	}

	public static class GetFoodValues extends FoodEvent
	{
		public FoodValues foodValues;
		public final FoodValues unmodifiedFoodValues;
		public final ItemStack food;

		public GetFoodValues(EntityPlayer player, ItemStack itemStack, FoodValues foodValues)
		{
			super(player);
			this.food = itemStack;
			this.foodValues = foodValues;
			this.unmodifiedFoodValues = foodValues;
		}
	}

	public static class FoodEaten extends FoodEvent
	{
		public FoodValues foodValues;
		public int hungerAdded;
		public float saturationAdded;
		public ItemFood itemFood;
		public ItemStack food;

		public FoodEaten(EntityPlayer player, ItemFood itemFood, ItemStack itemStack, FoodValues foodValues, int hungerAdded, float saturationAdded)
		{
			super(player);
			this.food = itemStack;
			this.itemFood = itemFood;
			this.foodValues = foodValues;
			this.hungerAdded = hungerAdded;
			this.saturationAdded = saturationAdded;
		}
	}

	public abstract static class Exhaustion extends FoodEvent
	{
		private Exhaustion(EntityPlayer player)
		{
			super(player);
		}

		@Cancelable
		public static class Tick extends Exhaustion
		{
			public float exhaustionLevel;
			public float maxExhaustionLevel = 4f;

			public Tick(EntityPlayer player, float exhaustionLevel)
			{
				super(player);
				this.exhaustionLevel = exhaustionLevel;
			}
		}

		@Cancelable
		public static class MaxReached extends Exhaustion
		{
			public final float currentExhaustionLevel;
			public float deltaExhaustion;
			public int deltaHunger = -1;
			public float deltaSaturation = -1f;

			public MaxReached(EntityPlayer player, float exhaustionToRemove, float currentExhaustionLevel)
			{
				super(player);
				this.deltaExhaustion = -exhaustionToRemove;
				this.currentExhaustionLevel = currentExhaustionLevel;

				boolean shouldDecreaseSaturationLevel = player.getFoodStats().getSaturationLevel() > 0f;
				
				if (!shouldDecreaseSaturationLevel)
					deltaSaturation = 0f;
				
				EnumDifficulty difficulty = player.worldObj.difficultySetting;
				boolean shouldDecreaseFoodLevel = !shouldDecreaseSaturationLevel && difficulty != EnumDifficulty.PEACEFUL;

				if (!shouldDecreaseFoodLevel)
					deltaHunger = 0;
			}
		}
	}

	public abstract static class RegenHealth extends FoodEvent
	{
		private RegenHealth(EntityPlayer player)
		{
			super(player);
		}
		
		public static class AllowRegen extends RegenHealth
		{
			public AllowRegen(EntityPlayer player)
			{
				super(player);
			}
		}
		
		@Cancelable
		public static class Tick extends RegenHealth
		{
			public int regenTickPeriod = 80;

			public Tick(EntityPlayer player)
			{
				super(player);
			}
		}

		@Cancelable
		public static class Regen extends RegenHealth
		{
			public float deltaHealth = 1f;
			public float deltaExhaustion = -3f;

			public Regen(EntityPlayer player)
			{
				super(player);
			}
		}
	}

	public abstract static class Starvation extends FoodEvent
	{
		private Starvation(EntityPlayer player)
		{
			super(player);
		}

		public static class AllowStarvation extends Starvation
		{
			public AllowStarvation(EntityPlayer player)
			{
				super(player);
			}
		}

		public static class Tick extends Starvation
		{
			public int starveTickPeriod = 80;

			public Tick(EntityPlayer player)
			{
				super(player);
			}
		}

		@Cancelable
		public static class Starve extends Starvation
		{
			public float starveDamage = 1f;

			public Starve(EntityPlayer player)
			{
				super(player);

				EnumDifficulty difficulty = player.worldObj.difficultySetting;
				boolean shouldDoDamage = player.getHealth() > 10.0F || difficulty == EnumDifficulty.HARD || player.getHealth() > 1.0F && difficulty == EnumDifficulty.NORMAL;

				if (!shouldDoDamage)
					starveDamage = 0f;
			}
		}
	}
}
