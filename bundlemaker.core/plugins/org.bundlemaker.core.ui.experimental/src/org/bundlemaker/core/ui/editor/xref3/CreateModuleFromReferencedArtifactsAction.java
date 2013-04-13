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

package org.bundlemaker.core.ui.editor.xref3;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.bundlemaker.core.analysis.IBundleMakerArtifact;
import org.bundlemaker.core.analysis.IResourceArtifact;
import org.bundlemaker.core.ui.ErrorDialogUtil;
import org.bundlemaker.core.ui.editor.xref3.internal.Activator;
import org.bundlemaker.core.ui.handler.ModuleFromArtifactListCreator;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Display;

/**
 * @author Nils Hartmann (nils@nilshartmann.net)
 * 
 */
public class CreateModuleFromReferencedArtifactsAction extends Action {

  private final Set<IBundleMakerArtifact> _referencedArtifacts;

  public CreateModuleFromReferencedArtifactsAction(Set<IBundleMakerArtifact> set) {
    super("Create Module from selected References");
    _referencedArtifacts = set;
    setEnabled(_referencedArtifacts != null && !_referencedArtifacts.isEmpty());
  }

  @Override
  public void run() {
    System.out.println(_referencedArtifacts.size() + " referenced Artifacts");
    List<IBundleMakerArtifact> resources = new LinkedList<IBundleMakerArtifact>();
    for (IBundleMakerArtifact artifact : _referencedArtifacts) {
      if (artifact.isInstanceOf(IResourceArtifact.class)) {
        resources.add(artifact);
      }
    }

    ModuleFromArtifactListCreator creator = new ModuleFromArtifactListCreator();
    try {
      creator.execute(Display.getCurrent().getActiveShell(), resources);
    } catch (Exception e) {
      ErrorDialogUtil.errorDialogWithStackTrace("Creation of Module failed", e.getMessage(), Activator.PLUGIN_ID, e);
    }
  }

}
