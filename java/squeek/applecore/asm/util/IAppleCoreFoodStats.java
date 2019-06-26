package squeek.applecore.asm.util;

import net.minecraft.entity.player.PlayerEntity;

public interface IAppleCoreFoodStats
{
	int getFoodTimer();
	void setFoodTimer(int value);
	int getStarveTimer();
	void setStarveTimer(int value);
	PlayerEntity getPlayer();
	void setPlayer(PlayerEntity player);
	void setPrevFoodLevel(int value);
	float getExhaustion();
	void setExhaustion(float value);
	void setSaturation(float value);
}