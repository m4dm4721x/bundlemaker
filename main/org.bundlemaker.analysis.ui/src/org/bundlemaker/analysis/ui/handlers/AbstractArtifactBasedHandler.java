/*******************************************************************************
 * Copyright (c) 2011 Bundlemaker project team.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Bundlemaker project team - initial API and implementation
 ******************************************************************************/
package org.bundlemaker.analysis.ui.handlers;

import java.util.List;

import org.bundlemaker.analysis.ui.internal.Activator;
import org.bundlemaker.core.analysis.IBundleMakerArtifact;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ISelection;

/**
 * Abstract base class for BundleMaker-based command handlers.
 * 
 * <p>
 * This class provides access to the IArtifact objects, that are selected in the navigator view
 * 
 * @author Nils Hartmann (nils@nilshartmann.net)
 * 
 */
public abstract class AbstractArtifactBasedHandler extends AbstractBundleMakerHandler {

  @Override
  protected void execute(ExecutionEvent event, ISelection structuredSelection) throws Exception {
    List<IBundleMakerArtifact> selectedArtifacts = getSelectedObject(structuredSelection, IBundleMakerArtifact.class);

    // Invoke execution method
    execute(event, selectedArtifacts);
  }

  /**
   * Will be invoked when the command is executed. Subclasses must override this method to provide their execution logic
   * 
   * @param selectedArtifacts
   *          The {@link IArtifact} objects that are selected in the tree. Never null.
   */
  protected abstract void execute(ExecutionEvent event, List<IBundleMakerArtifact> selectedArtifacts) throws Exception;

  @Override
  protected void reportError(String pluginId, String message, Throwable ex) {
    Status errorStatus = new Status(IStatus.ERROR, pluginId, message, ex);
    Activator.getDefault().getLog().log(errorStatus);
  }

}
