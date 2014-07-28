package squeek.applecore.api;

public abstract class AppleCoreAccessor
{
	private static IAppleCoreAccessor accessorImpl;

	public static IAppleCoreAccessor get()
	{
		return accessorImpl;
	}
}
