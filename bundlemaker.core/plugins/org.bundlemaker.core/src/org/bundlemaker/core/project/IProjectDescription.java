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
package org.bundlemaker.core.project;

import java.util.List;

/**
 * <p>
 * Defines the interface of a bundle maker project description.
 * </p>
 * 
 * @author Gerd W&uuml;therich (gerd@gerd-wuetherich.de)
 * 
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface IProjectDescription {

  /**
   * <p>
   * Returns the containing bundle maker project.
   * </p>
   * 
   * @return the containing bundle maker project.
   */
  IProjectDescriptionAwareBundleMakerProject getBundleMakerProject();

  /**
   * <p>
   * Returns an <b>unmodifiable</b> list of all registered {@link IProjectContentProvider IProjectContentProviders}.
   * </p>
   * 
   * @return
   */
  List<? extends IProjectContentProvider> getContentProviders();

  /**
   * <p>
   * Returns a <b>unmodifiable</b> list with all the defined {@link IProjectContentEntry IProjectContentEntries}.
   * </p>
   * 
   * @return a <b>unmodifiable</b> list with all the defined {@link IProjectContentEntry IProjectContentEntries}.
   */
  List<? extends IProjectContentEntry> getContent();

  /**
   * <p>
   * Returns the {@link IProjectContentEntry} with the specified {@link IProjectContentEntry}.
   * </p>
   * 
   * @param identifier
   *          the identifier (must not be null)
   * @return the {@link IProjectContentEntry} with the specified {@link IProjectContentEntry}.
   */
  IProjectContentEntry getProjectContentEntry(String identifier);

  /**
   * <p>
   * Returns the name of the associated JRE.
   * </p>
   * 
   * @return the name of the associated JRE.
   */
  String getJRE();
}
