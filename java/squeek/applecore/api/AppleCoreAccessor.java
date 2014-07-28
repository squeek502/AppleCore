package squeek.applecore.api;

/**
 * Used to access various hidden values of the hunger system.
 * 
 * See {@link IAppleCoreAccessor} for a list of the available functions.
 */
public abstract class AppleCoreAccessor
{
	/**
	 * Initialized by AppleCore on startup
	 */
	private static IAppleCoreAccessor accessorImpl;

	/**
	 * Accessor for the IAppleCoreAccessor implementation
	 */
	public static IAppleCoreAccessor get()
	{
		return accessorImpl;
	}
}
