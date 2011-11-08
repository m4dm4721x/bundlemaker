package org.bundlemaker.core.internal.analysis;

import org.bundlemaker.analysis.model.ArtifactType;
import org.bundlemaker.analysis.model.IArtifact;
import org.bundlemaker.core.analysis.IArtifactTreeVisitor;
import org.bundlemaker.core.analysis.IBundleMakerArtifact;
import org.bundlemaker.core.analysis.IPackageArtifact;
import org.bundlemaker.core.analysis.IResourceArtifact;
import org.bundlemaker.core.analysis.ITypeArtifact;
import org.bundlemaker.core.internal.analysis.cache.ArtifactCache;
import org.bundlemaker.core.internal.analysis.cache.ModuleKey;
import org.bundlemaker.core.internal.analysis.cache.ModulePackageKey;
import org.bundlemaker.core.modules.IResourceModule;
import org.bundlemaker.core.modules.modifiable.IModifiableResourceModule;
import org.eclipse.core.runtime.Assert;

/**
 * <p>
 * </p>
 * 
 * @author Gerd W&uuml;therich (gerd@gerd-wuetherich.de)
 */
public class AdapterResourceModule2IArtifact extends AdapterModule2IArtifact {

  /** - */
  private ArtifactCache _artifactCache;

  /**
   * <p>
   * Creates a new instance of type {@link AdapterResourceModule2IArtifact}.
   * </p>
   */
  public AdapterResourceModule2IArtifact(IResourceModule resourceModule, IArtifact parent, ArtifactCache artifactCache) {
    super(resourceModule, parent);

    //
    Assert.isNotNull(artifactCache);

    // set the default artifact cache
    _artifactCache = artifactCache;
  }

  /**
   * {@inheritDoc}
   */
  public void setName(String name) {
    super.setName(name);
  }

  @Override
  protected void onAddArtifact(IArtifact artifact) {
    // handle package
    if (artifact.getType().equals(ArtifactType.Package)) {
      handleAddPackage(artifact);
    } else if (artifact.getType().equals(ArtifactType.Resource)) {
      handleAddResource((IResourceArtifact) artifact);
    } else if (artifact.getType().equals(ArtifactType.Type)) {
      handleAddType((ITypeArtifact) artifact);
    }
  }

  @Override
  protected void onRemoveArtifact(IArtifact artifact) {
    //
    AdapterUtils.removeArtifact(artifact, this);
  }

  private void handleAddType(ITypeArtifact artifact) {

    //
    if (artifact.getParent() != null && artifact.getParent().getType().equals(ArtifactType.Resource)) {

      handleAddResource((IResourceArtifact) artifact.getParent());

    } else {

      // step 1: get the package key
      ModulePackageKey modulePackageKey = new ModulePackageKey(new ModuleKey(getAssociatedModule()), artifact
          .getAssociatedType().getPackageName());

      //
      IPackageArtifact newPackageArtifact = (IPackageArtifact) _artifactCache.getPackageCache().getOrCreate(
          modulePackageKey);

      //
      newPackageArtifact.addArtifact(artifact);
    }
  }

  /**
   * <p>
   * </p>
   * 
   * @param artifact
   */
  private void handleAddResource(IResourceArtifact artifact) {

    //
    AdapterUtils.addResourcesToModule(getResourceModule(), AdapterUtils.getAllMovableUnits(artifact));
  }

  /**
   * <p>
   * </p>
   * 
   * @param artifact
   */
  private void handleAddPackage(IArtifact artifact) {

    //
    AdapterUtils.addResourcesToModule(getResourceModule(), AdapterUtils.getAllMovableUnits(artifact));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String handleCanAdd(IArtifact artifact) {

    // a resource module artifact can contain packages, types and resources
    if (!(artifact.getType().equals(ArtifactType.Package) || artifact.getType().equals(ArtifactType.Type) || artifact
        .getType().equals(ArtifactType.Resource))) {

      return "Only packages, types or resources can be added to a resource module.";
    }

    return null;
  }

  /**
   * <p>
   * </p>
   * 
   * @return
   */
  public IModifiableResourceModule getResourceModule() {
    return (IModifiableResourceModule) getModule();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void accept(IArtifactTreeVisitor visitor) {

    //
    if (visitor.visit(this)) {
      //
      for (IArtifact artifact : getChildren()) {
        ((IBundleMakerArtifact) artifact).accept(visitor);
      }
    }
  }

}
