package org.bundlemaker.core.jdt.content;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IPath;

/**
 * <p>
 * </p>
 * 
 * @author Gerd W&uuml;therich (gerd@gerd-wuetherich.de)
 */
public class ResolvedEntry {

  /** - */
  private IPath       _binaryPath;

  /** - */
  private List<IPath> _sources;

  /** - */
  private boolean     _analyze;

  /** - */
  private String      _projectName;

  /**
   * <p>
   * Creates a new instance of type {@link ResolvedEntry}.
   * </p>
   * 
   * @param binaryPath
   */
  public ResolvedEntry(IPath binaryPath) {

    //
    Assert.isNotNull(binaryPath);

    //
    _binaryPath = binaryPath;
  }

  /**
   * <p>
   * </p>
   * 
   * @return
   */
  public IPath getBinaryPath() {
    return _binaryPath;
  }

  /**
   * <p>
   * </p>
   * 
   * @return
   */
  public List<IPath> getSources() {

    if (_sources == null) {
      _sources = new LinkedList<IPath>();
    }

    return _sources;
  }

  /**
   * <p>
   * </p>
   * 
   * @param analyze
   */
  public void setAnalyze(boolean analyze) {
    _analyze = analyze;
  }

  /**
   * <p>
   * </p>
   * 
   * @return
   */
  public boolean isAnalyze() {
    return _analyze;
  }

  /**
   * <p>
   * </p>
   * 
   * @return
   */
  public String getProjectName() {
    return _projectName;
  }

  /**
   * <p>
   * </p>
   * 
   * @param projectName
   */
  public void setProjectName(String projectName) {
    _projectName = projectName;
  }

  /**
   * <p>
   * </p>
   *
   * @return
   */
  public boolean hasProjectName() {
    return _projectName != null;
  }
}
