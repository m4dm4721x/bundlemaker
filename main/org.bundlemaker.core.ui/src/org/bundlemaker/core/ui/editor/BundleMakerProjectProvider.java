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
package org.bundlemaker.core.ui.editor;

import org.bundlemaker.core.IBundleMakerProject;

/**
 * @author Nils Hartmann (nils@nilshartmann.net)
 * 
 */
public interface BundleMakerProjectProvider {

  public IBundleMakerProject getBundleMakerProject();

  /**
   * TODO
   */
  public void parseProject();

}