/*******************************************************************************
 * Copyright (c) 2012 BundleMaker Project Team
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Nils Hartmann - initial API and implementation
 ******************************************************************************/
package org.bundlemaker.core.ui.commands;

import java.util.List;

import org.bundlemaker.core.BundleMakerCore;
import org.bundlemaker.core.IBundleMakerProject;
import org.bundlemaker.core.analysis.IBundleMakerArtifact;
import org.bundlemaker.core.modules.IModularizedSystem;
import org.bundlemaker.core.ui.Activator;
import org.bundlemaker.core.ui.artifact.CommonNavigatorUtils;
import org.bundlemaker.core.ui.artifact.configuration.IArtifactModelConfigurationProvider;
import org.bundlemaker.core.ui.editor.ParseBundleMakerProjectRunnable;
import org.bundlemaker.core.ui.event.Events;
import org.bundlemaker.core.ui.handler.AbstractBundleMakerHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.navigator.CommonNavigator;

/**
 * @author Nils Hartmann (nils@nilshartmann.net)
 * 
 */
public class ParseBundleMakerProjectHandler extends AbstractBundleMakerHandler implements IHandler {

  /*
   * (non-Javadoc)
   * 
   * @see org.bundlemaker.core.ui.handler.AbstractBundleMakerHandler#execute(org.eclipse.core.commands.ExecutionEvent,
   * org.eclipse.jface.viewers.ISelection)
   */
  @Override
  protected void execute(ExecutionEvent event, ISelection selection) throws Exception {
    List<IProject> selectedObjects = getSelectedObject(selection, IProject.class);
    IProject project = selectedObjects.get(0);
    IBundleMakerProject bundleMakerProject = BundleMakerCore.getBundleMakerProject(project, null);

    if (!ParseBundleMakerProjectRunnable.parseProject(bundleMakerProject)) {
      return;
    }

    // Select default modularized system in common navigator
    selectDefaultModularizedSystemArtifact(bundleMakerProject);

    // Notify listeners
    Events.instance().fireProjectOpened(bundleMakerProject);

    //
    // }

    // StructuredSelection newSelection = new StructuredSelection(artifact);
    // System.out.println("SET NEW SELECTION: " + newSelection);
    // findCommonNavigator.selectReveal(newSelection);
    // CommonNavigatorUtils.refresh("org.eclipse.ui.navigator.ProjectExplorer");

    // ITreeContentProvider contentProvider = (ITreeContentProvider) findCommonNavigator.getCommonViewer()
    // .getContentProvider();
    // System.out.println("ContentProvider: " + contentProvider);
    // IProject project2 = ResourcesPlugin.getWorkspace().getRoot().getProject("JavaProject");
    // IFile classpath = project2.getFile(".classpath");
    // System.out.println("classpath: " + classpath);
    //
    // findCommonNavigator.selectReveal(new StructuredSelection(classpath));

  }

  protected void selectDefaultModularizedSystemArtifact(IBundleMakerProject bundleMakerProject) throws CoreException {
    IProject eclipseProject = bundleMakerProject.getProject();

    // get the common navigator
    CommonNavigator commonNavigator = CommonNavigatorUtils
        .findCommonNavigator(CommonNavigatorUtils.PROJECT_EXPLORER_VIEW_ID);
    if (commonNavigator == null) {
      return;
    }

    // Expand Eclipse Project project in tree (i.e. make Artifacts node visible)

    commonNavigator.getCommonViewer().expandToLevel(eclipseProject, 1);

    // Select root artifact in tree
    IBundleMakerArtifact defaultModularizedSystemArtifact = getDefaultModularizedSystemArtifact(bundleMakerProject);
    StructuredSelection newSelection = new StructuredSelection(defaultModularizedSystemArtifact);
    commonNavigator.selectReveal(newSelection);
  }

  protected IBundleMakerArtifact getDefaultModularizedSystemArtifact(IBundleMakerProject bundleMakerProject)
      throws CoreException {
    IArtifactModelConfigurationProvider artifactModelConfigurationProvider = Activator.getDefault()
        .getArtifactModelConfigurationProvider();
    IModularizedSystem modularizedSystem = bundleMakerProject.getModularizedSystemWorkingCopy();

    //
    IBundleMakerArtifact artifact = modularizedSystem.getArtifactModel(artifactModelConfigurationProvider
        .getArtifactModelConfiguration());

    return artifact;

  }

  public static CommonNavigator findCommonNavigator(String navigatorViewId) {
    IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
    if (page != null) {
      IViewPart view = page.findView(navigatorViewId);
      if (view != null && view instanceof CommonNavigator)
        return ((CommonNavigator) view);
    }
    return null;
  }

}