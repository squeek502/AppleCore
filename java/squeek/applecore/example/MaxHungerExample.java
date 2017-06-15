package squeek.applecore.example;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import squeek.applecore.api.hunger.HungerEvent;

/**
 * Created by primetoxinz on 6/15/17.
 */
public class MaxHungerExample {
    @SubscribeEvent
    public void getMaxHunger(HungerEvent.GetMaxHunger event)
    {
        event.maxHunger = 60;
    }
}
