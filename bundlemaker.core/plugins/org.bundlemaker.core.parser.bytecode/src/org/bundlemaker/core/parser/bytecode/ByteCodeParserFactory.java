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
package org.bundlemaker.core.parser.bytecode;

import org.bundlemaker.core.spi.parser.IParser;
import org.bundlemaker.core.spi.parser.IParserFactory;
import org.eclipse.core.runtime.CoreException;

/**
 * <p>
 * </p>
 * 
 * @author Gerd W&uuml;therich (gerd@gerd-wuetherich.de)
 */
public class ByteCodeParserFactory extends IParserFactory.Adapter {

  /**
   * {@inheritDoc}
   */
  @Override
  public IParser createParser() throws CoreException {

    // return the new byte code parser
    return new ByteCodeParser();
  }
}
