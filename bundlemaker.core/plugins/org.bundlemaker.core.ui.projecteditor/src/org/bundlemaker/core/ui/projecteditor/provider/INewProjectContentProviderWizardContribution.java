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
package org.bundlemaker.core.ui.projecteditor.provider;

import org.bundlemaker.core.project.IProjectDescriptionAwareBundleMakerProject;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.swt.graphics.Image;

/**
 * @author Nils Hartmann (nils@nilshartmann.net)
 * 
 */
public interface INewProjectContentProviderWizardContribution {

  public String getLabel(IProjectDescriptionAwareBundleMakerProject bundleMakerProject);

  public Image getImage(IProjectDescriptionAwareBundleMakerProject bundleMakerProject);

  /**
   * Returns a readable description of this Wizard
   * 
   * @param bundleMakerProject
   * @return
   */
  public String getDescription(IProjectDescriptionAwareBundleMakerProject bundleMakerProject);

  public IWizard createWizard(IProjectDescriptionAwareBundleMakerProject bundleMakerProject);

}
