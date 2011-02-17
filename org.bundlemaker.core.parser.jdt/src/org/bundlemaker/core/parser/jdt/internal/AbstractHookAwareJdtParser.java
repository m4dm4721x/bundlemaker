package org.bundlemaker.core.parser.jdt.internal;

import java.util.List;

import org.bundlemaker.core.IBundleMakerProject;
import org.bundlemaker.core.parser.IParser;
import org.bundlemaker.core.parser.jdt.IJdtSourceParserHook;
import org.bundlemaker.core.resource.IResourceKey;
import org.bundlemaker.core.util.ExtensionRegistryTracker;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.CompilationUnit;

/**
 * <p>
 * </p>
 * 
 * @author Gerd W&uuml;therich (gerd@gerd-wuetherich.de)
 */
public abstract class AbstractHookAwareJdtParser implements IParser {

	/** - */
	private ExtensionRegistryTracker<IJdtSourceParserHook> _hookRegistry;

	/** - */
	private List<IJdtSourceParserHook> _currentHooks;

	/**
	 * <p>
	 * </p>
	 * 
	 * @param bundleMakerProject
	 * @throws CoreException
	 */
	public AbstractHookAwareJdtParser(
			ExtensionRegistryTracker<IJdtSourceParserHook> hookRegistry) {

		Assert.isNotNull(hookRegistry);

		//
		_hookRegistry = hookRegistry;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void parseBundleMakerProjectStart(
			IBundleMakerProject bundleMakerProject) {

		// initialize current hooks
		_currentHooks = _hookRegistry.getExtensionObjects();

		// notify 'start'
		for (IJdtSourceParserHook sourceParserHook : _currentHooks) {
			sourceParserHook.parseBundleMakerProjectStart(bundleMakerProject);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void parseBundleMakerProjectStop(
			IBundleMakerProject bundleMakerProject) {

		// notify 'stop'
		for (IJdtSourceParserHook sourceParserHook : _currentHooks) {
			sourceParserHook.parseBundleMakerProjectStop(bundleMakerProject);
		}
		
		//
		_currentHooks = null;
	}

	/**
	 * <p>
	 * </p>
	 * 
	 * @param iCompilationUnit
	 * @param compilationUnit
	 */
	protected final void callSourceParserHooks(IResourceKey resourceKey,
			CompilationUnit compilationUnit) {

		//
		Assert.isNotNull(_currentHooks);

		//
		for (IJdtSourceParserHook hook : _currentHooks) {

			//
			hook.analyzeCompilationUnit(resourceKey, compilationUnit);
		}
	}
}
