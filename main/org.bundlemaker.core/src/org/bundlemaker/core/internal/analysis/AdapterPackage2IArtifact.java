package org.bundlemaker.core.internal.analysis;

import org.bundlemaker.core.analysis.IArtifactTreeVisitor;
import org.bundlemaker.core.analysis.IBundleMakerArtifact;
import org.bundlemaker.core.analysis.IModuleArtifact;
import org.bundlemaker.core.analysis.IPackageArtifact;
import org.bundlemaker.core.analysis.IResourceArtifact;
import org.bundlemaker.core.analysis.ITypeArtifact;
import org.bundlemaker.core.internal.analysis.cache.ArtifactCache;
import org.bundlemaker.core.internal.analysis.cache.ModuleKey;
import org.bundlemaker.core.internal.analysis.cache.ModulePackageKey;
import org.bundlemaker.core.modules.IModule;
import org.bundlemaker.core.modules.IResourceModule;
import org.eclipse.core.runtime.Assert;

/**
 * <p>
 * </p>
 * 
 * @author Gerd W&uuml;therich (gerd@gerd-wuetherich.de)
 */
public class AdapterPackage2IArtifact extends AbstractBundleMakerArtifactContainer implements IPackageArtifact {

  /** - */
  private String        _qualifiedName;

  /** - */
  private boolean       _isVirtual;

  /** - */
  private boolean       _isHierarchical;

  /** - */
  private ArtifactCache _artifactCache;

  /** - */
  private IModule       _containingModule;

  /**
   * <p>
   * Creates a new instance of type {@link AdapterPackage2IArtifact}.
   * </p>
   * 
   * @param qualifiedName
   * @param parent
   */
  public AdapterPackage2IArtifact(String qualifiedName, IBundleMakerArtifact parent, boolean isVirtual,
      boolean isHierarchical, IModule containingModule, ArtifactCache artifactCache) {
    super(_getName(qualifiedName));

    // set parent/children dependency
    if (parent != null) {
      setParent(parent);
      ((AbstractBundleMakerArtifactContainer) parent).getModifiableChildren().add(this);
    }

    Assert.isNotNull(qualifiedName);

    // set the qualified name
    _qualifiedName = qualifiedName;
    _isVirtual = isVirtual;
    _artifactCache = artifactCache;
    _containingModule = containingModule;
    _isHierarchical = isHierarchical;
  }

  // @Override
  // public boolean containsPackages() {
  // for (IBundleMakerArtifact bundleMakerArtifact : getChildren()) {
  // if (bundleMakerArtifact.getType().equals(ArtifactType.Package)
  // && ((IPackageArtifact) bundleMakerArtifact).containsTypesOrResources()) {
  // return true;
  // }
  // }
  // return false;
  // }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getUniquePathIdentifier() {
    return _isHierarchical ? getName() : getQualifiedName();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isVirtual() {
    return _isVirtual;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isMovable() {

    //
    IBundleMakerArtifact artifact = getParent(IModuleArtifact.class);

    //
    return artifact instanceof IModuleArtifact
        && ((IModuleArtifact) artifact).getAssociatedModule() instanceof IResourceModule;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getQualifiedName() {
    return _qualifiedName;
  }

  @Override
  public String handleCanAdd(IBundleMakerArtifact artifact) {

    //
    if (artifact.isInstanceOf(IResourceArtifact.class)) {
      String packageName = ((IResourceArtifact) artifact).getAssociatedResource().getPackageName();
      if (!packageName.equals(this.getQualifiedName())) {
        return String.format("Can not add resource '%s' to package '%s'.", artifact.getQualifiedName(), packageName);
      } else {
        return null;
      }
    }

    if (artifact.isInstanceOf(ITypeArtifact.class)) {
      String packageName = ((ITypeArtifact) artifact).getAssociatedType().getPackageName();
      if (!packageName.equals(this.getQualifiedName())) {
        return String.format("Can not add type '%s' to package '%s'.", artifact.getQualifiedName(), packageName);
      } else {
        return null;
      }
    }

    if (artifact.isInstanceOf(IPackageArtifact.class)) {
      IPackageArtifact packageArtifact = ((IPackageArtifact) artifact);
      int index = packageArtifact.getQualifiedName().lastIndexOf(".");
      String parentPackageName = index != -1 ? packageArtifact.getQualifiedName().substring(0, index) : packageArtifact
          .getQualifiedName();
      if (!parentPackageName.equals(this.getQualifiedName())) {
        return String.format("Can not add package '%s' to package '%s'.", artifact.getQualifiedName(),
            this.getQualifiedName());
      } else {
        return null;
      }
    }

    return String.format("Can not handle artifact '%s'.", artifact.getQualifiedName());
  }

  /**
   * <p>
   * </p>
   * 
   * @return
   */
  public final IModule getContainingModule() {
    return _containingModule;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void onAddArtifact(IBundleMakerArtifact artifact) {

    // asserts
    Assert.isNotNull(artifact);
    assertCanAdd(artifact);

    // handle package
    if (artifact.isInstanceOf(IPackageArtifact.class)) {

      //
      ModulePackageKey modulePackageKey = new ModulePackageKey(new ModuleKey(_containingModule),
          artifact.getQualifiedName());

      IPackageArtifact packageArtifact = (IPackageArtifact) _artifactCache.getPackageCache().getOrCreate(
          modulePackageKey);

      // move the children to the new package artifact
      for (IBundleMakerArtifact child : artifact.getChildren()) {
        packageArtifact.addArtifact(child);
      }
    } else {
      AdapterUtils.addArtifactToPackage(this, artifact);
    }
  }

  @Override
  protected void onRemoveArtifact(IBundleMakerArtifact artifact) {

    // asserts
    Assert.isNotNull(artifact);

    // TODO: IS THIS CORRECT ??
    AdapterUtils.removeArtifact(artifact, this);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void accept(IArtifactTreeVisitor visitor) {

    //
    if (visitor.visit(this)) {
      //
      for (IBundleMakerArtifact artifact : getChildren()) {
        ((IBundleMakerArtifact) artifact).accept(visitor);
      }
    }
  }

  public void accept(IArtifactTreeVisitor... visitors) {
    DispatchingArtifactTreeVisitor artifactTreeVisitor = new DispatchingArtifactTreeVisitor(visitors);
    accept(artifactTreeVisitor);
  }

  /**
   * <p>
   * </p>
   * 
   * @param qualifiedName
   * @return
   */
  public static String _getName(String qualifiedName) {
    return qualifiedName.indexOf('.') != -1 ? qualifiedName.substring(qualifiedName.lastIndexOf('.') + 1)
        : qualifiedName;
  }
}
