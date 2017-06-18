package squeek.applecore.api;

import squeek.applecore.api_impl.AppleCoreDispatcherImpl;

/**
 * Used to access/mutate various hidden values of the hunger system or fire standard AppleCore events.
 * 
 * See {@link IAppleCoreAccessor}, {@link IAppleCoreMutator}, and {@link AppleCoreDispatcherImpl} for a list of the available functions.
 * {@link #accessor}, {@link #mutator}, and {@link #dispatcher} will be initialized by AppleCore on startup.
 */
public abstract class AppleCoreAPI
{
	public static IAppleCoreAccessor accessor;
	public static IAppleCoreMutator mutator;
	public static AppleCoreDispatcherImpl dispatcher;
}