package squeek.applecore.helpers;

import java.lang.reflect.Field;
import net.minecraft.util.FoodStats;
import cpw.mods.fml.relauncher.ReflectionHelper;

public class FoodStatsHelper
{
	public static final Field foodExhaustion = ReflectionHelper.findField(FoodStats.class, "foodExhaustionLevel", "field_75126_c", "c");
	static
	{
		foodExhaustion.setAccessible(true);
	}

	public static float getExhaustionLevel(FoodStats foodStats)
	{
		try
		{
			return foodExhaustion.getFloat(foodStats);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return 0f;
		}
	}

	public static void setExhaustionLevel(FoodStats foodStats, float val)
	{
		try
		{
			foodExhaustion.setFloat(foodStats, val);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
