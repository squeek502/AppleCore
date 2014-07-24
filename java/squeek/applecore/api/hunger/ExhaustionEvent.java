package squeek.applecore.api.hunger;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.EnumDifficulty;
import cpw.mods.fml.common.eventhandler.Cancelable;
import cpw.mods.fml.common.eventhandler.Event;

public abstract class ExhaustionEvent extends Event
{
	public EntityPlayer player;

	public ExhaustionEvent(EntityPlayer player)
	{
		this.player = player;
	}

	@Cancelable
	public static class Tick extends ExhaustionEvent
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
	public static class MaxReached extends ExhaustionEvent
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