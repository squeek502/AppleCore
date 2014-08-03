package squeek.applecore.asm;

import org.objectweb.asm.ClassWriter;

// getCommonSuperClass needs to be overwritten to avoid ClassNotFoundExceptions in obfuscated environments
public class ObfRemappingClassWriter extends ClassWriter
{
	public ObfRemappingClassWriter(int flags)
	{
		super(flags);
	}

	@Override
	protected String getCommonSuperClass(final String type1, final String type2)
	{
		Class<?> c, d;
		ClassLoader classLoader = getClass().getClassLoader();
		try
		{
			c = Class.forName(ObfHelper.toDeobfClassName(type1.replace('/', '.')), false, classLoader);
			d = Class.forName(ObfHelper.toDeobfClassName(type2.replace('/', '.')), false, classLoader);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e.toString());
		}
		if (c.isAssignableFrom(d))
		{
			return type1;
		}
		if (d.isAssignableFrom(c))
		{
			return type2;
		}
		if (c.isInterface() || d.isInterface())
		{
			return "java/lang/Object";
		}
		else
		{
			do
			{
				c = c.getSuperclass();
			}
			while (!c.isAssignableFrom(d));
			return ObfHelper.toObfClassName(c.getName()).replace('.', '/');
		}
	}
}
