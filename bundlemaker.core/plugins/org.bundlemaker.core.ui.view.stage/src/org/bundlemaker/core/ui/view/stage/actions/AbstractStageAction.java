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

package org.bundlemaker.core.ui.view.stage.actions;

import java.util.List;

import org.bundlemaker.core.analysis.IBundleMakerArtifact;
import org.bundlemaker.core.selection.IArtifactSelection;
import org.bundlemaker.core.selection.Selection;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;

/**
 * @author Nils Hartmann (nils@nilshartmann.net)
 * 
 */
public class AbstractStageAction extends Action {

  /** The selection in the project explorer */
  private IArtifactSelection _artifactSelection;

  public AbstractStageAction(String title) {
    super(title, IAction.AS_PUSH_BUTTON);
  }

  public void setArtifactSelection(IArtifactSelection artifactSelection) {
    this._artifactSelection = artifactSelection;
  }

  public IArtifactSelection getArtifactSelection() {
    return this._artifactSelection;
  }

  protected void addToStage(List<IBundleMakerArtifact> artifacts) {
    Selection.instance().getArtifactStage().addToStage(artifacts);
  }

  protected void removeFromStage(List<IBundleMakerArtifact> artifacts) {
    Selection.instance().getArtifactStage().removeStagedArtifacts(artifacts);
  }

  protected boolean isManualAddMode() {
    return !Selection.instance().getArtifactStage().getAddMode().isAutoAddMode();
  }

}
