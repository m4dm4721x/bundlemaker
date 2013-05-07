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
package org.bundlemaker.core.resource;

import org.bundlemaker.core.projectdescription.IProjectContentEntry;

/**
 * <p>
 * An {@link IProjectContentResource} defines a resource that is definied through a {@link IProjectContentEntry}. It
 * provides access to the <code>contentId</code>, the <code>root</code> directory or archive file, and a
 * <code>timestamp</code>.
 * </p>
 * 
 * @author Gerd W&uuml;therich (gerd@gerd-wuetherich.de)
 * 
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface IProjectContentResource extends IResource {

  /**
   * <p>
   * Returns the identifier of the {@link IProjectContentEntry} that defines the resource.
   * </p>
   * 
   * @return the identifier of the {@link IProjectContentEntry} that defines the resource.
   */
  String getProjectContentEntryId();

  /**
   * <p>
   * Returns the root directory or archive file that contains the resource (e.g. <code>'c:/dev/classes.zip'</code> or
   * <code>'c:/dev/source'</code>). Note that resource paths are always slash-delimited ('/').
   * </p>
   * 
   * @return the root directory or archive file that contains the resource.
   */
  String getRoot();

  /**
   * <p>
   * The timestamp.
   * </p>
   * 
   * @return
   */
  long getCurrentTimestamp();
}
