package squeek.applecore.reference;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.FoodStats;
import net.minecraft.world.World;
import com.mojang.authlib.GameProfile;

public abstract class EntityPlayerModifications extends EntityPlayer
{
	// modified initialization of foodStats field to use the added constructor
	protected FoodStats foodStats = new FoodStatsModifications(this);

	// unmodified; required for compilation
	public EntityPlayerModifications(World p_i45324_1_, GameProfile p_i45324_2_)
	{
		super(p_i45324_1_, p_i45324_2_);
	}
}
