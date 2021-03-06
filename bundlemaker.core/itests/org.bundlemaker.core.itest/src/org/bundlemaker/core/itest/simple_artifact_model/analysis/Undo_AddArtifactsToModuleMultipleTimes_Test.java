package org.bundlemaker.core.itest.simple_artifact_model.analysis;

import static org.bundlemaker.core.itestframework.simple_artifact_model.ArtifactAssert.assertResourceModuleCount;
import static org.bundlemaker.core.itestframework.simple_artifact_model.ArtifactAssert.assertResourceModuleCountInModularizedSystem;

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
public class Undo_AddArtifactsToModuleMultipleTimes_Test extends AbstractSimpleArtifactModelTest {

  /**
   * <p>
   * </p>
   * 
   * @throws Exception
   */
  @Test
  public void undoAddPackageToModuleArtifactsMultipleTimes() throws Exception {

    perform(new AddToModule() {

      public void addToFirstModule(IModuleArtifact newModuleArtifact) {
        IPackageArtifact packageArtifact = getBinModel().getTestPackage();
        newModuleArtifact.addArtifact(packageArtifact);
        Assert.assertEquals(4, getModularizedSystem().getTransformations().size());
      }

      @Override
      public void addToSecondModule(IModuleArtifact newModuleArtifact) {
        newModuleArtifact.addArtifact(getBinModel().getKlasseResource());
        newModuleArtifact.addArtifact(getBinModel().getTestResource());
        Assert.assertEquals(7, getModularizedSystem().getTransformations().size());
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
  public void undoAddResourcesToModuleArtifactsMultipleTimes() throws Exception {

    perform(new AddToModule() {

      public void addToFirstModule(IModuleArtifact newModuleArtifact) {
        newModuleArtifact.addArtifact(getBinModel().getKlasseResource());
        newModuleArtifact.addArtifact(getBinModel().getTestResource());
        Assert.assertEquals(5, getModularizedSystem().getTransformations().size());
      }

      @Override
      public void addToSecondModule(IModuleArtifact newModuleArtifact) {
        newModuleArtifact.addArtifact(getBinModel().getKlasseResource());
        newModuleArtifact.addArtifact(getBinModel().getTestResource());
        Assert.assertEquals(8, getModularizedSystem().getTransformations().size());
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
  public void undoAddTypesToModuleArtifactsMultipleTimes() throws Exception {
    perform(new AddToModule() {

      public void addToFirstModule(IModuleArtifact newModuleArtifact) {
        newModuleArtifact.addArtifact(getBinModel().getKlasseResource().getChild("Klasse"));
        newModuleArtifact.addArtifact(getBinModel().getTestResource().getChild("Test"));
        Assert.assertEquals(5, getModularizedSystem().getTransformations().size());
      }

      public void addToSecondModule(IModuleArtifact newModuleArtifact) {
        newModuleArtifact.addArtifact(getBinModel().getKlasseResource().getChild("Klasse"));
        newModuleArtifact.addArtifact(getBinModel().getTestResource().getChild("Test"));
        Assert.assertEquals(8, getModularizedSystem().getTransformations().size());
      }
    });
  }

  /**
   * <p>
   * </p>
   * 
   * @throws Exception
   */
  private void perform(final AddToModule addToModule) throws Exception {

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

        addToModule.addToFirstModule(newModuleArtifact);

        // assert that we two groups and two modules
        Assert.assertEquals(new Path("group1/group2/NewModule_1.0.0/de/test/Test.class"), getBinModel()
            .getTestResource().getFullPath());
        Assert.assertEquals(new Path("group1/group2/NewModule_1.0.0/de/test/Test.java"), getSrcModel()
            .getTestResource().getFullPath());

        // STEP 2: create another new module
        IModuleArtifact newModuleArtifact2 = getBinModel().getGroup2Artifact().getOrCreateModule("NewModule2", "1.0.0");
        Assert.assertEquals("group1/group2/NewModule2_1.0.0", newModuleArtifact2.getQualifiedName());

        // assert that we two groups and two modules
        Assert.assertEquals(2, getModularizedSystem().getGroups().size());
        assertResourceModuleCountInModularizedSystem(getModularizedSystem(), 4);
        assertResourceModuleCount(getBinModel(), 4);
        assertResourceModuleCount(getSrcModel(), 4);

        addToModule.addToSecondModule(newModuleArtifact2);

        // assert that we two groups and two modules
        Assert.assertEquals(new Path("group1/group2/NewModule2_1.0.0/de/test/Test.class"), getBinModel()
            .getTestResource().getFullPath());
        Assert.assertEquals(new Path("group1/group2/NewModule2_1.0.0/de/test/Test.java"), getSrcModel()
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
  private static interface AddToModule {

    /**
     * <p>
     * </p>
     * 
     * @param newModuleArtifact
     */
    public void addToFirstModule(IModuleArtifact newModuleArtifact);

    /**
     * <p>
     * </p>
     * 
     * @param newModuleArtifact
     */
    public void addToSecondModule(IModuleArtifact newModuleArtifact);
  }
}
