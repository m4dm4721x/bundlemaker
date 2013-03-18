package org.bundlemaker.core.internal.analysis;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.bundlemaker.core.analysis.IAnalysisModelVisitor;
import org.bundlemaker.core.analysis.IBundleMakerArtifact;
import org.bundlemaker.core.analysis.IModuleArtifact;
import org.bundlemaker.core.analysis.IPackageArtifact;
import org.bundlemaker.core.analysis.spi.AbstractArtifactContainer;
import org.bundlemaker.core.modules.IModule;
import org.bundlemaker.core.modules.modifiable.IModifiableModularizedSystem;
import org.bundlemaker.core.transformation.RenameModuleTransformation;
import org.eclipse.core.runtime.Assert;

/**
 * <p>
 * </p>
 * 
 * @author Gerd W&uuml;therich (gerd@gerd-wuetherich.de)
 */
public class AdapterModule2IArtifact extends AbstractArtifactContainer implements IModuleArtifact {

  /** the resource module */
  private IModule _module;

  /**
   * <p>
   * Creates a new instance of type {@link AdapterModule2IArtifact}.
   * </p>
   * 
   * @param modularizedSystem
   */
  public AdapterModule2IArtifact(IModule module, IBundleMakerArtifact parent) {
    super(module.getModuleIdentifier().toString());

    Assert.isNotNull(module);
    Assert.isTrue(parent instanceof AbstractArtifactContainer);

    // set the resource module
    _module = module;

    // set parent/children dependency
    setParent(parent);
    ((AbstractArtifactContainer) parent).getModifiableChildrenCollection().add(this);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Collection<IBundleMakerArtifact> getChildren() {

    // we have to filter out empty packages:
    // https://bundlemaker.jira.com/browse/BM-345

    // TODO: caching!
    List<IBundleMakerArtifact> result = new LinkedList<IBundleMakerArtifact>();

    //
    for (IBundleMakerArtifact bundleMakerArtifact : getModifiableChildrenCollection()) {

      if (bundleMakerArtifact instanceof IPackageArtifact
          && !((IPackageArtifact) bundleMakerArtifact).containsTypesOrResources()) {
        // skip
      } else {
        result.add(bundleMakerArtifact);
      }
    }

    //
    return Collections.unmodifiableCollection(result);
  }

  @Override
  protected void onRemoveArtifact(IBundleMakerArtifact artifact) {
    throw new UnsupportedOperationException("onRemoveArtifact");

  }

  @Override
  public boolean isVirtual() {
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isMovable() {
    return true;
  }

  @Override
  public void setNameAndVersion(String name, String version) {
    RenameModuleTransformation transformation = new RenameModuleTransformation(this, name, version);
    ((IModifiableModularizedSystem) getRoot().getModularizedSystem()).applyTransformations(null, transformation);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public IModule getAssociatedModule() {
    return _module;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final String getQualifiedName() {

    String classification = "";

    if (_module.hasClassification()) {
      classification = _module.getClassification().toString() + "/";
    }

    return classification + _module.getModuleIdentifier().toString();
  }

  @Override
  public String getModuleName() {
    return _module.getModuleIdentifier().getName();
  }

  @Override
  public String getModuleVersion() {
    return _module.getModuleIdentifier().getVersion();
  }

  @Override
  public String handleCanAdd(IBundleMakerArtifact artifact) {
    return "Can not add artifacts to non-resource modules.";
  }

  @Override
  public boolean isResourceModule() {
    return false;
  }

  @Override
  protected String getArtifactType() {
    return "module";
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void accept(IAnalysisModelVisitor visitor) {

    //
    if (visitor.visit(this)) {
      //
      for (IBundleMakerArtifact artifact : getChildren()) {
        ((IBundleMakerArtifact) artifact).accept(visitor);
      }
    }
  }

  public void accept(IAnalysisModelVisitor... visitors) {
    DispatchingArtifactTreeVisitor artifactTreeVisitor = new DispatchingArtifactTreeVisitor(visitors);
    accept(artifactTreeVisitor);
  }

  @Override
  public void setName(String name) {
    super.setName(name);
  }

  /**
   * <p>
   * Returns the module.
   * </p>
   * 
   * @return the module.
   */
  protected IModule getModule() {
    return _module;
  }
}
