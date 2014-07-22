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

	@Cancelable
	public static class RegenHealth extends FoodEvent
	{
		public int regenTickPeriod = 80;
		public float exhaustionFromRegen = 3f;
		public int minHungerToHeal = 18;

		public RegenHealth(EntityPlayer player)
		{
			super(player);
		}
	}

	public abstract static class Starvation extends FoodEvent
	{
		private Starvation(EntityPlayer player)
		{
			super(player);
		}

		@Cancelable
		public static class Tick extends Starvation
		{
			public int starveTickPeriod = 80;
			public int starvationHungerThreshold = 0;

			public Tick(EntityPlayer player, float exhaustionLevel)
			{
				super(player);
			}
		}

		@Cancelable
		public static class OnStarve extends Starvation
		{
			public float starveDamage = 1f;

			public OnStarve(EntityPlayer player)
			{
				super(player);

				EnumDifficulty difficulty = player.worldObj.difficultySetting;
				boolean shouldDoDamage = player.getHealth() > 10.0F || difficulty == EnumDifficulty.HARD || player.getHealth() > 1.0F && difficulty == EnumDifficulty.NORMAL;

				if (!shouldDoDamage)
					starveDamage = 0f;
			}
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

			}
		}

		@Cancelable
		public static class OnMaxReached extends Exhaustion
		{
			public float exhaustion;
			public float deltaHunger = 1f;
			public float deltaSaturation = 1f;

			public OnMaxReached(EntityPlayer player)
			{
				super(player);
			}
		}
	}
}
