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
package org.bundlemaker.core.projectdescription;

import java.util.List;

import org.bundlemaker.core.internal.projectdescription.BundleMakerProjectDescription;
import org.eclipse.core.runtime.CoreException;

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
public interface IModifiableBundleMakerProjectDescription extends IBundleMakerProjectDescription {

  /**
   * <p>
   * Sets the JRE:
   * </p>
   * 
   * @param jre
   */
  void setJre(String jre);

  /**
   * <p>
   * </p>
   * 
   * @return
   */
  int getNextId();

  /**
   * <p>
   * Returns an <b>unmodifiable</b> list.
   * </p>
   * 
   * @return
   */
  List<? extends IBundleMakerProjectContentProvider> getContentProviders();

  /**
   * <p>
   * </p>
   * 
   * @param contentProvider
   */
  void addContentProvider(IBundleMakerProjectContentProvider contentProvider);

  /**
   * <p>
   * </p>
   * 
   * @param contentProvider
   */
  void removeContentProvider(IBundleMakerProjectContentProvider contentProvider);

  /**
   * <p>
   * </p>
   * 
   * @param move
   */
  void moveUpContentProviders(List<? extends IBundleMakerProjectContentProvider> move);

  /**
   * <p>
   * </p>
   * 
   * @param move
   */
  void moveDownContentProviders(List<? extends IBundleMakerProjectContentProvider> move);

  /**
   * <p>
   * Removes the content provider with the specified identifier.
   * </p>
   * 
   * @param identifier
   *          the identifier
   */
  void removeContentProvider(String identifier);

  /**
   * <p>
   * Clears the list of content entries. After calling this method the list of content entries is empty.
   * </p>
   */
  void clear();

  /**
   * <p>
   * Saves the {@link BundleMakerProjectDescription}.
   * </p>
   * <p>
   * The project description is saved internally in the xml file
   * <code>"&lt;project-directory&gt;/.bundlemaker/projectdescription.xml"</code> . Note that it's not intended to
   * directly modify this file.
   * </p>
   * 
   * @throws CoreException
   * 
   * @precondition none
   */
  void save() throws CoreException;
}
