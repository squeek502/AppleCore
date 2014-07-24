package squeek.applecore.api.hunger;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.EnumDifficulty;
import cpw.mods.fml.common.eventhandler.Cancelable;
import cpw.mods.fml.common.eventhandler.Event;

public abstract class StarvationEvent extends Event
{
	public EntityPlayer player;

	public StarvationEvent(EntityPlayer player)
	{
		this.player = player;
	}

	public static class AllowStarvation extends StarvationEvent
	{
		public AllowStarvation(EntityPlayer player)
		{
			super(player);
		}
	}

	public static class Tick extends StarvationEvent
	{
		public int starveTickPeriod = 80;

		public Tick(EntityPlayer player)
		{
			super(player);
		}
	}

	@Cancelable
	public static class Starve extends StarvationEvent
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