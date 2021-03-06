package org.bundlemaker.core.itest.misc_models;

import java.io.IOException;

import org.bundlemaker.core.analysis.AnalysisCore;
import org.bundlemaker.core.analysis.AnalysisModelConfiguration;
import org.bundlemaker.core.analysis.DependencyKind;
import org.bundlemaker.core.analysis.IBundleMakerArtifact;
import org.bundlemaker.core.analysis.IDependency;
import org.bundlemaker.core.itestframework.AbstractBundleMakerModelTest;
import org.eclipse.core.runtime.CoreException;
import org.junit.Assert;
import org.junit.Test;

/**
 * <p>
 * </p>
 * 
 * @author Gerd W&uuml;therich (gerd@gerd-wuetherich.de)
 */
public class NoPrimaryTypeTest extends AbstractBundleMakerModelTest {

  /**
   * <p>
   * </p>
   * 
   * @throws CoreException
   * @throws IOException
   */
  @Test
  public void testNoPrimaryType_aggregatedTypes() throws CoreException, IOException {

    // Step 1: transform the model
    IBundleMakerArtifact rootArtifact = AnalysisCore.getAnalysisModel(getModularizedSystem(),
        AnalysisModelConfiguration.SOURCE_RESOURCES_CONFIGURATION).getRoot();
    Assert.assertNotNull(rootArtifact);

    //
    IBundleMakerArtifact moduleArtifact = rootArtifact.getChild("NoPrimaryTypeTest_1.0.0");
    Assert.assertNotNull(moduleArtifact);

    //
    IBundleMakerArtifact clientTypeArtifact = moduleArtifact
        .getChild("org.bundlemaker.noprimarytype|Client.java|org.bundlemaker.noprimarytype.Client");
    Assert.assertNotNull(clientTypeArtifact);

    //
    IBundleMakerArtifact noPrimaryTestInterfaceTypeArtifact = moduleArtifact
        .getChild("org.bundlemaker.noprimarytype|TestInterface.java|org.bundlemaker.noprimarytype.NoPrimaryTestInterface");
    Assert.assertNotNull(noPrimaryTestInterfaceTypeArtifact);

    Assert.assertEquals(1, moduleArtifact.getDependenciesTo().size());

    IDependency dependency = clientTypeArtifact.getDependencyTo(noPrimaryTestInterfaceTypeArtifact);
    Assert.assertNotNull(dependency);
    Assert.assertEquals(clientTypeArtifact, dependency.getFrom());
    Assert.assertEquals(noPrimaryTestInterfaceTypeArtifact, dependency.getTo());
    Assert.assertEquals(DependencyKind.USES, dependency.getDependencyKind());
  }
}
