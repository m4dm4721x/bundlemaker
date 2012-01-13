package org.bundlemaker.core.internal.analysis.cache.impl;

import org.bundlemaker.analysis.model.IArtifact;
import org.bundlemaker.core.internal.analysis.AbstractBundleMakerArtifactContainer;
import org.bundlemaker.core.internal.analysis.AdapterGroup2IArtifact;
import org.bundlemaker.core.internal.analysis.cache.ArtifactCache;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IPath;

/**
 * <p>
 * Implementation of an {@link AbstractSubCache} that holds all group artifacts.
 * </p>
 * 
 * @author Gerd W&uuml;therich (gerd@gerd-wuetherich.de)
 */
public class GroupSubCache extends AbstractSubCache<IPath, AbstractBundleMakerArtifactContainer> {

  /** serialVersionUID */
  private static final long serialVersionUID = 1L;

  /**
   * <p>
   * Creates a new instance of type {@link GroupSubCache}.
   * </p>
   * 
   * @param artifactCache
   */
  public GroupSubCache(ArtifactCache artifactCache) {
    super(artifactCache);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected AbstractBundleMakerArtifactContainer create(IPath classification) {

    // step 1: if the classification is 'null' or empty, we have to return the 'root' artifact
    if (classification == null || classification.isEmpty()) {
      return (AbstractBundleMakerArtifactContainer) getArtifactCache().getRootArtifact();
    }

    // step 2: compute the parent
    IArtifact parent = getGroupParent(classification);

    // step 3: create the new instance
    return new AdapterGroup2IArtifact(classification.lastSegment(), parent);
  }

  /**
   * <p>
   * Returns the correct parent for the given classification.
   * </p>
   * 
   * @param classification
   * @return
   */
  private IArtifact getGroupParent(IPath classification) {

    Assert.isNotNull(classification);

    // remove the parent
    return getArtifactCache().getGroupCache().getOrCreate(classification.removeLastSegments(1));
  }
}