package org.bundlemaker.core.itest.simple_artifact_model.analysis;

import static org.bundlemaker.core.itestframework.simple_artifact_model.ArtifactAssert.assertResourceModuleCount;
import static org.bundlemaker.core.itestframework.simple_artifact_model.ArtifactAssert.assertResourceModuleCountInModularizedSystem;

import org.bundlemaker.core.analysis.AnalysisModelException;
import org.bundlemaker.core.analysis.IModuleArtifact;
import org.bundlemaker.core.analysis.IPackageArtifact;
import org.bundlemaker.core.itestframework.simple_artifact_model.AbstractSimpleArtifactModelTest;
import org.bundlemaker.core.itestframework.simple_artifact_model.NoModificationAssertion;
import org.eclipse.core.runtime.Path;
import org.junit.Assert;
import org.junit.Test;

/**
 * <p>
 * </p>
 * 
 * @author Gerd W&uuml;therich (gerd@gerd-wuetherich.de)
 */
public class Undo_AddArtifactsToPackage_Test extends AbstractSimpleArtifactModelTest {

  /**
   * <p>
   * Not allowed: add package to package
   * </p>
   * 
   * @throws Exception
   */
  @Test(expected = AnalysisModelException.class)
  public void tryToAddPackageToModuleArtifacts_1() throws Exception {
    perform(new AddToPackage() {
      @Override
      public void addToPackage(IPackageArtifact newPackageArtifact) {
        IPackageArtifact packageArtifact = getBinModel().getTestPackage();
        newPackageArtifact.addArtifact(packageArtifact);
      }
    });
  }

  /**
   * <p>
   * Not allowed: add package to package
   * </p>
   * 
   * @throws Exception
   */
  @Test(expected = AnalysisModelException.class)
  public void tryToAddPackageToModuleArtifacts_2() throws Exception {
    perform(new AddToPackage() {
      @Override
      public void addToPackage(IPackageArtifact newPackageArtifact) {
        IPackageArtifact packageArtifact = getBinModel().getTestPackage();
        newPackageArtifact.getParent().addArtifact(packageArtifact);
      }
    });
  }

  /**
   * <p>
   * </p>
   * 
   * @throws Exception
   */
  @Test
  public void undoToAddResourceToModuleArtifacts() throws Exception {
    perform(new AddToPackage() {
      @Override
      public void addToPackage(IPackageArtifact newPackageArtifact) {
        newPackageArtifact.addArtifact(getBinModel().getTestResource());
      }
    });
  }

  /**
   * <p>
   * </p>
   * 
   * @throws Exception
   */
  @Test
  public void undoToAddTypeToModuleArtifacts() throws Exception {
    perform(new AddToPackage() {
      @Override
      public void addToPackage(IPackageArtifact newPackageArtifact) {
        newPackageArtifact.addArtifact(getBinModel().getTestResource().getChild("Test"));
      }
    });
  }

  /**
   * <p>
   * </p>
   * 
   * @throws Exception
   */
  private void perform(final AddToPackage addToModule) throws Exception {

    //
    NoModificationAssertion.assertNoModification(this, new NoModificationAssertion.Action() {

      /**
       * {@inheritDoc}
       */
      @Override
      public void prePostCondition() {
        Assert.assertEquals(2, getModularizedSystem().getGroups().size());
        assertResourceModuleCountInModularizedSystem(getModularizedSystem(), 2);
        assertResourceModuleCount(getBinModel(), 2);
        assertResourceModuleCount(getSrcModel(), 2);
        Assert.assertEquals(2, getModularizedSystem().getTransformations().size());
      }

      /**
       * {@inheritDoc}
       */
      @Override
      public void execute() {

        // STEP 1: create a new module
        IModuleArtifact newModuleArtifact = getBinModel().getGroup2Artifact().getOrCreateModule("NewModule", "1.0.0");
        Assert.assertEquals("group1/group2/NewModule_1.0.0", newModuleArtifact.getQualifiedName());

        // assert that we two groups and two modules
        Assert.assertEquals(2, getModularizedSystem().getGroups().size());
        assertResourceModuleCountInModularizedSystem(getModularizedSystem(), 3);
        assertResourceModuleCount(getBinModel(), 3);
        assertResourceModuleCount(getSrcModel(), 3);

        newModuleArtifact.addArtifact(getBinModel().getKlasseResource());
        IPackageArtifact newPackageArtifact = getBinModel().getKlasseResource().getParent(IPackageArtifact.class);
        Assert.assertNotNull(newPackageArtifact);
        Assert.assertNotNull(newPackageArtifact.getParent(IModuleArtifact.class));
        Assert.assertEquals("NewModule", newPackageArtifact.getParent(IModuleArtifact.class).getModuleName());

        addToModule.addToPackage(newPackageArtifact);

        // assert that we two groups and two modules
        Assert.assertEquals(new Path("group1/group2/NewModule_1.0.0/de/test/Test.class"), getBinModel()
            .getTestResource().getFullPath());
        Assert.assertEquals(new Path("group1/group2/NewModule_1.0.0/de/test/Test.java"), getSrcModel()
            .getTestResource().getFullPath());
      }
    });
  }

  /**
   * <p>
   * </p>
   * 
   * @author Gerd W&uuml;therich (gerd@gerd-wuetherich.de)
   */
  private static interface AddToPackage {

    /**
     * <p>
     * </p>
     * 
     * @param newModuleArtifact
     */
    public void addToPackage(IPackageArtifact packageArtifact);
  }
}
