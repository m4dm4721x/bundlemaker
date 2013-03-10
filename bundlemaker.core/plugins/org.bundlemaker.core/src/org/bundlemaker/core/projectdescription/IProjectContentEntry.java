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

import java.util.Map;
import java.util.Set;

import org.bundlemaker.core.resource.IResource;

/**
 * <p>
 * </p>
 * 
 * @author Gerd W&uuml;therich (gerd@gerd-wuetherich.de)
 * 
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface IProjectContentEntry extends IIdentifiableContentEntry {

  /**
   * <p>
   * Return <code>true</code> if this content entry is a resource entry that should be parsed and analyzed,
   * <code>false</code> otherwise.
   * </p>
   * 
   * @return <code>true</code> if this content entry is a resource entry that should be parsed and analyzed,
   *         <code>false</code> otherwise.
   */
  boolean isAnalyze();

  /**
   * <p>
   * Returns the {@link AnalyzeMode} for this {@link IProjectContentEntry}.
   * </p>
   * 
   * @return the {@link AnalyzeMode} for this {@link IProjectContentEntry}
   */
  AnalyzeMode getAnalyzeMode();

  /**
   * <p>
   * Returns all binary root paths of this {@link IProjectContentEntry}.
   * </p>
   * 
   * @return all binary root paths of this {@link IProjectContentEntry}.
   */
  Set<VariablePath> getBinaryRootPaths();

  /**
   * <p>
   * Returns all source root paths of this {@link IProjectContentEntry}.
   * </p>
   * 
   * @return all source root paths of this {@link IProjectContentEntry}.
   */
  Set<VariablePath> getSourceRootPaths();

  /**
   * <p>
   * Returns a {@link Set} of all resources of the specified type
   * </p>
   * <p>
   * If this content entry is not a resource content ( <code>isAnalyze()</code> returns <code>false</code>), an empty
   * set will be returned.
   * </p>
   * 
   * @param type
   * @return a Set of resources, never null.
   */
  Set<? extends IResource> getResources(ProjectContentType type);

  /**
   * <p>
   * Returns a {@link Set} of all binary resources
   * </p>
   * <p>
   * This is a convenience method for {@link #getResources(ProjectContentType) getResources(ContentType.BINARY)}
   * </p>
   * <p>
   * If this content entry is not a resource content ( <code>isAnalyze()</code> returns <code>false</code>), an empty
   * set will be returned.
   * </p>
   * 
   * @return a Set of resources, never null.
   */
  Set<? extends IResource> getBinaryResources();

  /**
   * Returns all source resources
   * <p>
   * This is a convenience method for {@link #getResources(ProjectContentType) getResources(ContentType.SOURCE)}
   * </p>
   * <p>
   * If this content entry is not a resource content ( <code>isAnalyze()</code> returns <code>false</code>), an empty
   * set will be returned.
   * </p>
   * 
   * @return a Set of resources, never null.
   */
  Set<? extends IResource> getSourceResources();

  /**
   * <p>
   * </p>
   * 
   * @return
   */
  Map<String, Object> getUserAttributes();
}