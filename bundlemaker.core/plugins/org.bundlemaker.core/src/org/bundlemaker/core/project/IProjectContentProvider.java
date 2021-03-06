package org.bundlemaker.core.project;

import java.util.List;

/**
 * <p>
 * </p>
 * 
 * <p>
 * Note: Implementations of this class must be subclasses of AbstractProjectContentProvider.
 * 
 * @author Gerd W&uuml;therich (gerd@gerd-wuetherich.de)
 */
public interface IProjectContentProvider {

  /**
   * <p>
   * </p>
   * 
   * @return
   */
  IProjectDescriptionAwareBundleMakerProject getBundleMakerProject();

  /**
   * <p>
   * Returns the internal identifier of this content entry provider.
   * </p>
   * 
   * @return the internal identifier of this content entry provider.
   */
  String getId();

  /**
   * <p>
   * </p>
   * 
   * @return
   */
  List<IProjectContentEntry> getBundleMakerProjectContent();

  /**
   * <p>
   * </p>
   * 
   * @return
   */
  List<IProjectContentProblem> getProblems();

  /**
   * <p>
   * </p>
   * 
   * @return
   */
  boolean hasProblems();
}
