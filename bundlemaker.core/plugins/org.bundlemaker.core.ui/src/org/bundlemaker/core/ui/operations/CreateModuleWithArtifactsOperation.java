/*******************************************************************************
 * Copyright (c) 2013 Bundlemaker project team.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Bundlemaker project team - initial API and implementation
 ******************************************************************************/

package org.bundlemaker.core.ui.operations;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bundlemaker.core.analysis.IBundleMakerArtifact;
import org.bundlemaker.core.analysis.IGroupAndModuleContainer;
import org.bundlemaker.core.analysis.IModuleArtifact;
import org.bundlemaker.core.analysis.IPackageArtifact;
import org.bundlemaker.core.analysis.IRootArtifact;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;

/**
 * @author Nils Hartmann (nils@nilshartmann.net)
 * 
 */
public class CreateModuleWithArtifactsOperation extends AbstractUiOperation {

  /**
   * @param shell
   * @param artifacts
   */
  public CreateModuleWithArtifactsOperation(Shell shell, List<IBundleMakerArtifact> artifacts) {
    super(shell, artifacts);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.bundlemaker.core.ui.operations.AbstractUiOperation#doRun()
   */
  @Override
  protected void doRun() {
    if (!hasArtifacts()) {
      // nothing selected. Shouldn't happen anyway...
      return;
    }

    IRootArtifact rootArtifact = null;

    String preselectedModuleName = null;
    Set<String> selectedModuleVersions = new HashSet<String>();

    for (IBundleMakerArtifact artifact : getArtifacts()) {
      if (rootArtifact == null) {
        rootArtifact = artifact.getRoot();
      }

      IModuleArtifact module = (artifact instanceof IModuleArtifact ? (IModuleArtifact) artifact : artifact
          .getParent(IModuleArtifact.class));
      if (module != null) {
        selectedModuleVersions.add(module.getModuleVersion());
      }

      String packageName = (artifact instanceof IPackageArtifact) ? artifact.getQualifiedName() : artifact.getParent(
          IPackageArtifact.class).getQualifiedName();

      if (preselectedModuleName == null) {
        preselectedModuleName = packageName;
      } else {
        StringBuilder b = new StringBuilder();
        for (int i = 0, j = 0; i < preselectedModuleName.length() && j < packageName.length(); i++, j++) {
          if (preselectedModuleName.charAt(i) == packageName.charAt(j)) {
            b.append(preselectedModuleName.charAt(i));
          } else {
            break;
          }
        }
        preselectedModuleName = b.toString();
      }
    }

    if (preselectedModuleName.endsWith(".")) {
      if (preselectedModuleName.length() > 1) {
        preselectedModuleName = preselectedModuleName.substring(0, preselectedModuleName.length() - 1);
      } else
        preselectedModuleName = "";
    }

    if (preselectedModuleName.isEmpty()) {
      preselectedModuleName = "NewModule";
    }

    // Pre-select Version with version number of selected modules
    String preSelectedModuleVersion = (selectedModuleVersions.size() == 1 ? selectedModuleVersions.iterator().next()
        : "1.0.0");

    IBundleMakerArtifact artifact = getArtifacts().get(0);

    CreateModuleFromPackageSelectionDialog dialog = new CreateModuleFromPackageSelectionDialog(getShell(),
        artifact.getRoot(), preselectedModuleName, preSelectedModuleVersion);
    if (dialog.open() == Window.OK) {

      IGroupAndModuleContainer groupAndModuleContainer = dialog.getParent();
      IModuleArtifact newModuleArtifact = groupAndModuleContainer.getOrCreateModule(dialog.getModuleName(),
          dialog.getModuleVersion());

      newModuleArtifact.addArtifacts(getArtifacts());

    }

  }

}
