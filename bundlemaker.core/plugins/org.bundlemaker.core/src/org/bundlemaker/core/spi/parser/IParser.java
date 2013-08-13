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
package org.bundlemaker.core.spi.parser;

import java.util.List;

import org.bundlemaker.core.internal.BundleMakerProject;
import org.bundlemaker.core.parser.IProblem;
import org.bundlemaker.core.project.IProjectContentEntry;
import org.bundlemaker.core.project.IProjectDescriptionAwareBundleMakerProject;
import org.eclipse.core.runtime.CoreException;

/**
 * <p>
 * Defines the common interface to parse a {@link BundleMakerProject}.
 * </p>
 * 
 * @author Gerd W&uuml;therich (gerd@gerd-wuetherich.de)
 */
public interface IParser {

  /**
   * <p>
   * </p>
   * 
   * @return
   */
  public ParserType getParserType();

  /**
   * <p>
   * </p>
   * 
   * @param resourceKey
   * @return
   */
  boolean canParse(IParsableResource resourceKey);

  /**
   * <p>
   * </p>
   * 
   * @param content
   * @param resource
   * @param context
   * @param parseReferences TODO
   * @throws CoreException
   */
  List<IProblem> parseResource(IProjectContentEntry content, IParsableResource resource,
      boolean parseReferences)
      throws CoreException;

  /**
   * <p>
   * </p>
   * 
   * @param bundleMakerProject
   */
  void batchParseStart(IProjectDescriptionAwareBundleMakerProject bundleMakerProject);

  /**
   * <p>
   * </p>
   * 
   * @param bundleMakerProject
   */
  void batchParseStop(IProjectDescriptionAwareBundleMakerProject bundleMakerProject);

  /**
   * <p>
   * </p>
   * 
   * @author Gerd W&uuml;therich (gerd@gerd-wuetherich.de)
   */
  public enum ParserType {
    BINARY, BINARY_AND_SOURCE, SOURCE;
  }
}
