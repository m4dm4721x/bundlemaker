/*******************************************************************************
 * Copyright (c) 2011 Gerd Wuetherich (gerd@gerd-wuetherich.de).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Gerd Wuetherich (gerd@gerd-wuetherich.de) - initial API and implementation
 ******************************************************************************/
package org.bundlemaker.core.internal.projectdescription;

import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.bundlemaker.core.BundleMakerProjectChangedEvent;
import org.bundlemaker.core.BundleMakerProjectChangedEvent.Type;
import org.bundlemaker.core.IBundleMakerProject;
import org.bundlemaker.core.internal.BundleMakerProject;
import org.bundlemaker.core.internal.ProjectDescriptionStore;
import org.bundlemaker.core.internal.resource.ResourceStandin;
import org.bundlemaker.core.projectdescription.AnalyzeMode;
import org.bundlemaker.core.projectdescription.IFileBasedContent;
import org.bundlemaker.core.projectdescription.IFileBasedContentProvider;
import org.bundlemaker.core.projectdescription.modifiable.FileBasedContent;
import org.bundlemaker.core.projectdescription.modifiable.IModifiableBundleMakerProjectDescription;
import org.bundlemaker.core.projectdescription.modifiable.IModifiableFileBasedContent;
import org.bundlemaker.core.resource.IResource;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.VariablesPlugin;

/**
 * <p>
 * </p>
 * 
 * @author Gerd W&uuml;therich (gerd@gerd-wuetherich.de)
 */
public class BundleMakerProjectDescription implements IModifiableBundleMakerProjectDescription {

  /** - */
  private static NumberFormat                       FORMATTER  = new DecimalFormat("000000");

  /** - */
  private List<FileBasedContent>                    _fileBasedContent;

  /** - */
  private List<? extends IFileBasedContentProvider> _fileBasedContentProvider;

  /** the resource list */
  private List<ResourceStandin>                     _sourceResources;

  /** the resource list */
  private List<ResourceStandin>                     _binaryResources;

  /** - */
  private String                                    _jre;

  /** - */
  private boolean                                   _initialized;

  /** - */
  private int                                       _currentId = 0;

  /** - */
  private BundleMakerProject                        _bundleMakerProject;

  /**
   * <p>
   * Creates a new instance of type {@link BundleMakerProjectDescription}.
   * </p>
   * 
   * @param bundleMakerProject
   */
  public BundleMakerProjectDescription(BundleMakerProject bundleMakerProject) {

    //
    _fileBasedContent = new ArrayList<FileBasedContent>();
    _fileBasedContentProvider = new ArrayList<IFileBasedContentProvider>();
    _sourceResources = new ArrayList<ResourceStandin>();
    _binaryResources = new ArrayList<ResourceStandin>();
    _bundleMakerProject = bundleMakerProject;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public IBundleMakerProject getBundleMakerProject() {
    return _bundleMakerProject;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<? extends IFileBasedContentProvider> getFileBasedContentProviders() {
    return Collections.unmodifiableList(_fileBasedContentProvider);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<? extends IFileBasedContentProvider> getModifiableFileBasedContentProvider() {
    return _fileBasedContentProvider;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<? extends IFileBasedContent> getFileBasedContent() {
    return Collections.unmodifiableList(_fileBasedContent);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  // TODO
  public IFileBasedContent getFileBasedContent(String id) {
    //
    return getModifiableFileBasedContent(id);
  }

  @Override
  public IModifiableFileBasedContent getModifiableFileBasedContent(String id) {

    // file based content
    for (FileBasedContent fileBasedContent : _fileBasedContent) {

      //
      if (fileBasedContent.getId().equals(id)) {
        return fileBasedContent;
      }
    }

    //
    return null;
  }

  @Override
  public void removeContent(String id) {

    for (Iterator<FileBasedContent> iterator = _fileBasedContent.iterator(); iterator.hasNext();) {

      FileBasedContent content = (FileBasedContent) iterator.next();

      if (content.getId().equals(id)) {
        iterator.remove();
        return;
      }
    }
  }

  @Override
  public void clear() {

    //
    _fileBasedContent.clear();

    //
    _currentId = 0;

    //
    _initialized = false;

    //
    _jre = null;
  }

  /**
   * <p>
   * </p>
   * 
   * @param bundlemakerProject
   * @throws CoreException
   */
  public void initialize(IBundleMakerProject bundlemakerProject) throws CoreException {

    // TODO
    if (isValid()) {
      throw new RuntimeException("Invalid description");
    }

    //
    int sourceResourcesCount = 0;
    int binaryResourcesCount = 0;

    //
    for (FileBasedContent fileBasedContent : _fileBasedContent) {
      fileBasedContent.initialize(this);

      //
      if (fileBasedContent.isAnalyze()) {

        binaryResourcesCount += fileBasedContent.getModifiableResourceContent().getModifiableBinaryResources().size();

        sourceResourcesCount += fileBasedContent.getModifiableResourceContent().getModifiableSourceResources().size();
      }
    }

    // TODO:
    System.out.println("Source resources to process: " + sourceResourcesCount);
    System.out.println("Binary resources to process: " + binaryResourcesCount);

    //
    _initialized = true;
  }

  /**
   * <p>
   * </p>
   * 
   * @return
   */
  public boolean isValid() {
    return _jre == null;
  }

  /**
   * <p>
   * </p>
   * 
   * @return
   */
  public String getJRE() {
    return _jre;
  }

  public boolean isInitialized() {
    return _initialized;
  }

  @Override
  public IModifiableFileBasedContent addContent(String binaryRoot, String sourceRoot, AnalyzeMode analyzeMode) {
    Assert.isNotNull(binaryRoot);
    Assert.isNotNull(analyzeMode);

    try {

      // get the jar info
      JarInfo jarInfo = JarInfoService.extractJarInfo(getAsFile(binaryRoot));

      //
      return addContent(jarInfo.getName(), jarInfo.getVersion(), toList(binaryRoot), toList(sourceRoot), analyzeMode);

    } catch (CoreException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      return null;
    }
  }

  private static List<String> toList(String string) {
    List<String> list = new LinkedList<String>();
    if (string != null) {
      list.add(string);
    }
    return list;
  }

  @Override
  public IModifiableFileBasedContent addResourceContent(String name, String version, String binaryRoot,
      String sourceRoot) {

    return addContent(name, version, toList(binaryRoot), toList(sourceRoot), AnalyzeMode.BINARIES_AND_SOURCES);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.bundlemaker.core.projectdescription.modifiable.IModifiableBundleMakerProjectDescription#addResourceContent(
   * java.lang.String)
   */
  @Override
  public IModifiableFileBasedContent addResourceContent(String binaryRoot) {
    return addContent(binaryRoot, null, AnalyzeMode.BINARIES_AND_SOURCES);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.bundlemaker.core.projectdescription.modifiable.IModifiableBundleMakerProjectDescription#addContent(java.lang
   * .String, java.lang.String, java.util.List, java.util.List, org.bundlemaker.core.projectdescription.AnalyzeMode)
   */
  @Override
  public IModifiableFileBasedContent addContent(String name, String version, List<String> binaryRoots,
      List<String> sourceRoots, AnalyzeMode analyzeMode) {
    Assert.isNotNull(name);
    Assert.isNotNull(version);
    Assert.isNotNull(binaryRoots);
    Assert.isNotNull(analyzeMode);

    // create new file based content
    FileBasedContent fileBasedContent = new FileBasedContent();

    // TODO: THREADING
    _currentId++;

    fileBasedContent.setId(FORMATTER.format(_currentId));
    fileBasedContent.setName(name);
    fileBasedContent.setVersion(version);

    // add the binary roots
    for (String string : binaryRoots) {
      fileBasedContent.getModifiableBinaryPaths().add(new RootPath(string, true));
    }

    //
    ResourceContent resourceContent = fileBasedContent.getModifiableResourceContent();

    if (sourceRoots != null) {
      // add the source roots
      for (String string : sourceRoots) {
        resourceContent.getModifiableSourcePaths().add(new RootPath(string, false));
      }
    }
    // add the analyze flag
    fileBasedContent.setAnalyzeMode(analyzeMode);

    // add file based content
    _fileBasedContent.add(fileBasedContent);

    // return result
    return fileBasedContent;
  }

  @SuppressWarnings("unchecked")
  public final List<IResource> getSourceResources() {
    List<? extends IResource> result = Collections.unmodifiableList(_sourceResources);
    return (List<IResource>) result;
  }

  @SuppressWarnings("unchecked")
  public final List<IResource> getBinaryResources() {
    List<? extends IResource> result = Collections.unmodifiableList(_binaryResources);
    return (List<IResource>) result;
  }

  public final List<ResourceStandin> getSourceResourceStandins() {
    return Collections.unmodifiableList(_sourceResources);
  }

  public final List<ResourceStandin> getBinaryResourceStandins() {
    return Collections.unmodifiableList(_binaryResources);
  }

  /**
   * <p>
   * </p>
   * 
   * @param resource
   */
  public void addSourceResource(ResourceStandin resourceStandin) {
    _sourceResources.add(resourceStandin);
  }

  public void addBinaryResource(ResourceStandin resourceStandin) {
    _binaryResources.add(resourceStandin);
  }

  /**
   * <p>
   * </p>
   * 
   * @return
   * @throws CoreException
   */
  private File getAsFile(String path) throws CoreException {

    //
    IStringVariableManager stringVariableManager = VariablesPlugin.getDefault().getStringVariableManager();

    //
    return new File(stringVariableManager.performStringSubstitution(path));
  }

  /**
   * <p>
   * </p>
   * 
   * @return
   */
  public List<FileBasedContent> getModifiableFileBasedContent() {

    //
    return _fileBasedContent;
  }

  /**
   * <p>
   * </p>
   * 
   * @return
   */
  public int getCurrentId() {
    return _currentId;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setJre(String jre) {
    _jre = jre;
  }

  /**
   * <p>
   * </p>
   * 
   * @param currentId
   */
  public void setCurrentId(int currentId) {
    _currentId = currentId;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void save() throws CoreException {
    ProjectDescriptionStore.saveProjectDescription(_bundleMakerProject.getProject(), this);

    // notify listener
    _bundleMakerProject.notifyListeners(new BundleMakerProjectChangedEvent(Type.PROJECT_DESCRIPTION_CHANGED));
  }
}
