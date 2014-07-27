package squeek.applecore.asm;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.launchwrapper.IClassTransformer;
import squeek.applecore.asm.module.*;

public class TransformerModuleHandler implements IClassTransformer
{
	private static final List<IClassTransformerModule> transformerModules = new ArrayList<IClassTransformerModule>();
	static
	{
		registerTransformerModule(new ModuleFoodStats());
		registerTransformerModule(new ModuleCrops());
		registerTransformerModule(new ModuleFoodEatingSpeed());
	}

	public static void registerTransformerModule(IClassTransformerModule transformerModule)
	{
		transformerModules.add(transformerModule);
	}

	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass)
	{
		for (IClassTransformerModule transformerModule : transformerModules)
		{
			for (String classToTransform : transformerModule.getClassesToTransform())
			{
				if (classToTransform.equals(transformedName))
				{
					basicClass = transformerModule.transform(name, transformedName, basicClass);
				}
			}
		}
		return basicClass;
	}

}
