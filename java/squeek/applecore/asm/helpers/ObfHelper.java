package squeek.applecore.asm.helpers;

import java.io.IOException;
import java.util.HashMap;
import net.minecraft.launchwrapper.LaunchClassLoader;
import cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;

public class ObfHelper
{
	public static boolean obfuscated = false;
	private static HashMap<String, String> classNameToObfClassNameCache = new HashMap<String, String>();
	private static HashMap<String, String> obfClassNameToClassNameCache = new HashMap<String, String>();

	static
	{
		boolean obf = true;
		try
		{
			obf = ((LaunchClassLoader) ObfHelper.class.getClassLoader()).getClassBytes("net.minecraft.world.World") == null;
		}
		catch (IOException iox)
		{
		}
		obfuscated = obf;
	}

	private static void cacheObfClassMapping(String obfClassName, String className)
	{
		obfClassNameToClassNameCache.put(obfClassName, className);
		classNameToObfClassNameCache.put(className, obfClassName);
	}

	public static String toDeobfClassName(String obfClassName)
	{
		if (obfuscated)
		{
			if (!obfClassNameToClassNameCache.containsKey(obfClassName))
				cacheObfClassMapping(obfClassName, FMLDeobfuscatingRemapper.INSTANCE.map(obfClassName.replace('.', '/')).replace('/', '.'));

			return obfClassNameToClassNameCache.get(obfClassName);
		}
		else
			return obfClassName;
	}

	public static String toObfClassName(String deobfClassName)
	{
		if (obfuscated)
		{
			if (!classNameToObfClassNameCache.containsKey(deobfClassName))
				cacheObfClassMapping(FMLDeobfuscatingRemapper.INSTANCE.unmap(deobfClassName.replace('.', '/')).replace('/', '.'), deobfClassName);

			return classNameToObfClassNameCache.get(deobfClassName);
		}
		else
			return deobfClassName;
	}

	public static String getInternalClassName(String className)
	{
		return toObfClassName(className).replace('.', '/');
	}

	public static String getDescriptor(String className)
	{
		return "L" + getInternalClassName(className) + ";";
	}
}
