package org.bundlemaker.core.internal.modelext;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.bundlemaker.core.spi.parser.IParserFactory;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.RegistryFactory;
import org.eclipse.core.runtime.dynamichelpers.ExtensionTracker;
import org.eclipse.core.runtime.dynamichelpers.IExtensionChangeHandler;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;

/**
 * <p>
 * </p>
 * 
 * @author Gerd W&uuml;therich (gerd@gerd-wuetherich.de)
 */
public abstract class AbstractBundleMakerExtensionRegistry<T> implements IExtensionChangeHandler,
    IBundleMakerExtensionRegistry<T> {

  /** - */
  public final String      _extensionPointId;

  /** - */
  private boolean          _isInitalized = false;

  /** - */
  private ExtensionTracker _tracker;

  /** - */
  private List<T>          _extensionInstances;

  /**
   * <p>
   * Creates a new instance of type {@link AbstractBundleMakerExtensionRegistry}.
   * </p>
   * 
   * @param extensionPointId
   */
  public AbstractBundleMakerExtensionRegistry(String extensionPointId) {
    Assert.isNotNull(extensionPointId);

    //
    _extensionPointId = extensionPointId;
  }

  /**
   * <p>
   * </p>
   */
  public void initialize() {

    //
    if (_isInitalized) {
      return;
    }

    // set initialized
    _isInitalized = true;

    //
    _extensionInstances = new LinkedList<T>();

    // get the extension registry
    IExtensionRegistry registry = RegistryFactory.getRegistry();

    // get the extension points
    IExtensionPoint extensionPoint = registry.getExtensionPoint(_extensionPointId);

    // get the extension tracker
    _tracker = new ExtensionTracker(registry);

    //
    for (IExtension extension : extensionPoint.getExtensions()) {
      addExtension(_tracker, extension);
    }

    // register IExtensionChangeHandler
    _tracker.registerHandler(this, ExtensionTracker.createExtensionPointFilter(extensionPoint));
  }

  /**
   * <p>
   * </p>
   * 
   * @return
   */
  public boolean isInitalized() {
    return _isInitalized;
  }

  /**
   * <p>
   * </p>
   */
  public void dispose() {
    _tracker.unregisterHandler(this);
  }

  /**
   * <p>
   * </p>
   * 
   * @return
   */
  public List<T> getExtensionInstances() {
    return Collections.unmodifiableList(_extensionInstances);
  }

  /**
   * {@inheritDoc}
   */
  public void addExtension(IExtensionTracker tracker, IExtension extension) {

    try {

      T parserFactory = createInstanceFromExtension(extension);

      _tracker.registerObject(extension, parserFactory, IExtensionTracker.REF_STRONG);

      // the parser factories
      _extensionInstances.add(parserFactory);

    } catch (CoreException e) {
      //
    }
  }

  public void removeExtension(IExtension extension, Object[] objects) {

    for (Object object : objects) {
      IParserFactory parserFactory = (IParserFactory) object;
      parserFactory.dispose();
      _extensionInstances.remove(parserFactory);
      _tracker.unregisterObject(extension, parserFactory);
    }
  }

  /**
   * <p>
   * </p>
   * 
   * @param extension
   * @return
   * @throws CoreException
   */
  protected abstract T createInstanceFromExtension(IExtension extension) throws CoreException;
}
