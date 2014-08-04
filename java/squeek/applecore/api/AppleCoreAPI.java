package squeek.applecore.api;

/**
 * Used to access or mutate various hidden values of the hunger system.
 * 
 * See {@link IAppleCoreAccessor} and {@link IAppleCoreMutator} for a list of the available functions.
 * {@link #accessor} and {@link #mutator} will be initialized by AppleCore on startup.
 */
public abstract class AppleCoreAPI
{
	public static IAppleCoreAccessor accessor;
	public static IAppleCoreMutator mutator;
}
