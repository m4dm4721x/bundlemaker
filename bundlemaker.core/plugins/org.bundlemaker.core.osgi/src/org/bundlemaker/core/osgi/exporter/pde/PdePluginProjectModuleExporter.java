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
package org.bundlemaker.core.osgi.exporter.pde;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;

import org.bundlemaker.core.common.ResourceType;
import org.bundlemaker.core.common.utils.FileUtils;
import org.bundlemaker.core.exporter.IModuleExporterContext;
import org.bundlemaker.core.exporter.ITemplateProvider;
import org.bundlemaker.core.exporter.util.Helper;
import org.bundlemaker.core.osgi.exporter.AbstractManifestAwareExporter;
import org.bundlemaker.core.osgi.internal.Activator;
import org.bundlemaker.core.osgi.manifest.IBundleManifestCreator;
import org.bundlemaker.core.osgi.manifest.IManifestPreferences;
import org.bundlemaker.core.project.IProjectContentResource;
import org.bundlemaker.core.resource.IModularizedSystem;
import org.bundlemaker.core.resource.IModule;
import org.bundlemaker.core.resource.IModuleResource;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.pde.core.project.IBundleClasspathEntry;
import org.eclipse.pde.core.project.IBundleProjectDescription;
import org.eclipse.pde.core.project.IBundleProjectService;

/**
 * h
 * <p>
 * </p>
 * 
 * @author Gerd W&uuml;therich (gerd@gerd-wuetherich.de)
 */
public class PdePluginProjectModuleExporter extends AbstractManifestAwareExporter {

  /** - */
  private static final String SRC_DIRECTORY_NAME = "src";

  /** - */
  private static final String BIN_DIRECTORY_NAME = "bin";

  /** - */
  private boolean             _useClassifcationForExportDestination;

  /**
   * <p>
   * Creates a new instance of type {@link PdePluginProjectModuleExporter}.
   * </p>
   */
  public PdePluginProjectModuleExporter() {
    this(null, null, null);
  }

  /**
   * <p>
   * Creates a new instance of type {@link PdePluginProjectModuleExporter}.
   * </p>
   */
  public PdePluginProjectModuleExporter(ITemplateProvider templateProvider,
      IBundleManifestCreator bundleManifestCreator, IManifestPreferences manifestPreferences) {
    super(templateProvider, bundleManifestCreator, manifestPreferences);
  }

  /**
   * <p>
   * </p>
   * 
   * @return
   */
  public boolean isUseClassifcationForExportDestination() {
    return _useClassifcationForExportDestination;
  }

  /**
   * <p>
   * </p>
   * 
   * @param useClassifcationForExportDestination
   */
  public void setUseClassifcationForExportDestination(boolean useClassifcationForExportDestination) {
    _useClassifcationForExportDestination = useClassifcationForExportDestination;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean canExport(IModularizedSystem modularizedSystem, IModule module, IModuleExporterContext context) {

    //
    return !module.getResources(ResourceType.SOURCE).isEmpty();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void doExport(IProgressMonitor progressMonitor) throws CoreException {

    // step 1: get a non-existing project name
    String projectName = Helper.getUniqueProjectName(getCurrentModule().getModuleIdentifier().getName());

    // step 2: delete and create project
    IPath location = null;

    if (isUseClassifcationForExportDestination()) {

      Path destinationDirectoryPath = new Path(getCurrentContext().getDestinationDirectory().getAbsolutePath());

      location = destinationDirectoryPath.append(getCurrentModule().getClassification()).append(projectName);
    }

    // (re-)create the project
    IProject project = Helper.deleteAndCreateProject(projectName, location);

    // step 3: add java and plug-nature
    IProjectDescription description = project.getDescription();
    description.setNatureIds(new String[] { JavaCore.NATURE_ID, IBundleProjectDescription.PLUGIN_NATURE });
    project.setDescription(description, null);

    // 'clean' the java project
    IJavaProject javaProject = JavaCore.create(project);
    javaProject.setRawClasspath(new IClasspathEntry[] { JavaRuntime.getDefaultJREContainerEntry() }, null);
    javaProject.save(null, true);

    // step 4: create and set the bundle project description
    IBundleProjectService bundleProjectService = Activator.getBundleProjectService();

    IBundleProjectDescription bundleProjectDescription = bundleProjectService.getDescription(project);

    //
    for (String header : getManifestContents().getMainAttributes().keySet()) {
      bundleProjectDescription.setHeader(header, getManifestContents().getMainAttributes().get(header));
    }

    // set source dir
    IBundleClasspathEntry bundleClasspathEntry = bundleProjectService.newBundleClasspathEntry(new Path(
        SRC_DIRECTORY_NAME), new Path(BIN_DIRECTORY_NAME), null);

    //
    bundleProjectDescription.setBundleClassath(new IBundleClasspathEntry[] { bundleClasspathEntry });
    //
    bundleProjectDescription.apply(null);

    // step 5: copy the source files
    IFolder srcFolder = project.getFolder(SRC_DIRECTORY_NAME);

    // copy the source
    for (IProjectContentResource resourceStandin : getCurrentModule().getResources(ResourceType.SOURCE)) {

      if (!resourceStandin.getPath().startsWith("META-INF")) {

        //
        File targetFile = new File(srcFolder.getRawLocation().toFile(), resourceStandin.getPath());
        targetFile.getParentFile().mkdirs();

        try {
          //
          FileUtils.copy(new ByteArrayInputStream(resourceStandin.getContent()), new FileOutputStream(targetFile),
              new byte[1024]);
        } catch (Exception e) {
          throw new CoreException(new Status(IStatus.ERROR, Activator.BUNDLE_ID, "Unable to copy file "
               + resourceStandin.getRoot() + "to " + targetFile +": " + e, e));
        }
      }
    }
    
    // Refresh source-folder to make Eclipse aware of new copied files
    srcFolder.refreshLocal(1, progressMonitor);
  }
}
