package squeek.applecore.api;

/**
 * Used to access/mutate various hidden values of the hunger system or fire standard AppleCore events.
 * 
 * See {@link IAppleCoreAccessor} and {@link IAppleCoreMutator} for a list of the available functions.
 * {@link #accessor} and {@link #mutator} will be initialized by AppleCore on startup.
 */
public abstract class AppleCoreAPI
{
	public static IAppleCoreAccessor accessor;
	public static IAppleCoreMutator mutator;
	public static IAppleCoreRegistry registry;
}
