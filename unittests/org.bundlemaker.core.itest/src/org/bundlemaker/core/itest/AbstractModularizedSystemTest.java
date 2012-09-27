package org.bundlemaker.core.itest;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.bundlemaker.core.IProblem;
import org.bundlemaker.core.analysis.IBundleMakerArtifact;
import org.bundlemaker.core.itestframework.AbstractBundleMakerProjectTest;
import org.bundlemaker.core.modules.IModule;
import org.bundlemaker.core.modules.ModuleIdentifier;
import org.bundlemaker.core.modules.modifiable.IModifiableModularizedSystem;
import org.bundlemaker.core.testutils.BundleMakerTestUtils;
import org.bundlemaker.core.testutils.FileDiffUtil;
import org.bundlemaker.core.util.ProgressMonitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.junit.Assert;
import org.junit.Before;

/**
 * <p>
 * </p>
 * 
 * @author Gerd W&uuml;therich (gerd@gerd-wuetherich.de)
 */
public abstract class AbstractModularizedSystemTest extends AbstractBundleMakerProjectTest {

  /** - */
  private IModifiableModularizedSystem _modularizedSystem;

  /**
   * <p>
   * </p>
   * 
   * @return
   */
  public IModifiableModularizedSystem getModularizedSystem() {
    return _modularizedSystem;
  }

  @Before
  @Override
  public void before() throws CoreException {

    super.before();

    //
    addProjectDescription();

    //
    getBundleMakerProject().initialize(new ProgressMonitor());

    // parse and open the project
    getBundleMakerProject().parseAndOpen(new ProgressMonitor());

    // assert no parse errors
    if (getBundleMakerProject().getProblems().size() > 0) {

      //
      StringBuilder builder = new StringBuilder();

      //
      for (IProblem problem : getBundleMakerProject().getProblems()) {
        builder.append(problem.getMessage());
        builder.append("\n");
      }
      Assert.fail(builder.toString());
    }

    _modularizedSystem = (IModifiableModularizedSystem) getBundleMakerProject().getModularizedSystemWorkingCopy(
        getTestProjectName());

    _modularizedSystem.applyTransformations(null, new GroupTransformation(new ModuleIdentifier(getTestProjectName(),
        "1.0.0"), new Path("group1/group2")));

    //
    IModule module = getModularizedSystem().getModule(getTestProjectName(), "1.0.0");
    Assert.assertNotNull(module);
  }

  @Override
  public void after() throws CoreException {

    //
    super.after();

    //
    _modularizedSystem = null;
  }

  /**
   * <p>
   * </p>
   * 
   * @param node
   * @param type
   * @param nodeName
   * @param parentName
   */
  protected void assertNode(IBundleMakerArtifact node, Class<?> type, String nodeName, String parentName) {
    Assert.assertTrue(String.format("Node '%s' has to be assignable from %s", node, type),
        type.isAssignableFrom(node.getClass()));
    Assert.assertEquals(nodeName, node.getName());
    Assert.assertNotNull(node.getParent());
    Assert.assertEquals(parentName, node.getParent().getName());
  }

  /**
   * <p>
   * </p>
   * 
   * @param node
   * @param type
   * @param nodeName
   */
  protected void assertNode(IBundleMakerArtifact node, Class<?> type, String nodeName) {
    Assert.assertTrue(String.format("Node '%s' has to be assignable from %s", node, type),
        type.isAssignableFrom(node.getClass()));
    Assert.assertEquals(nodeName, node.getName());
    Assert.assertNotNull(node.getParent());
  }

  /**
   * <p>
   * </p>
   * 
   * @param modularizedSystem
   * @param resourceModule
   * @param expectedResult
   * @throws Exception
   */
  protected static void assertResult(String dumpedModel, InputStream expected, String resultFileName) {

    Assert.assertNotNull(dumpedModel);
    Assert.assertNotNull(expected);
    Assert.assertNotNull(resultFileName);

    // actual
    File actual = new File(System.getProperty("user.dir"), "result" + File.separatorChar + resultFileName + ".txt");
    StringBuilder builder = new StringBuilder();
    builder.append(dumpedModel);
    BundleMakerTestUtils.writeToFile(builder.toString(), actual);

    // htmlReport
    String name = actual.getAbsolutePath();
    name = name.substring(0, name.length() - ".txt".length()) + ".html";
    File htmlReport = new File(name);

    // assert
    FileDiffUtil.assertArtifactModel(expected, new ByteArrayInputStream(dumpedModel.getBytes()), htmlReport);
  }

  /**
   * <p>
   * </p>
   * 
   * @return
   */
  protected static String getCurrentTimeStamp() {
    return new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date());
  }
}