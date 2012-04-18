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

import java.util.List;

import org.bundlemaker.core.IBundleMakerProject;
import org.bundlemaker.core.projectdescription.AnalyzeMode;
import org.bundlemaker.core.projectdescription.IProjectContentProvider;
import org.eclipse.swt.graphics.Image;

/**
 * @author Nils Hartmann (nils@nilshartmann.net)
 * 
 *         TODO rename IProjectContentTreeProvider
 */
public interface IProjectContentProviderEditor {

  public boolean canHandle(IProjectContentProvider provider);

  public Object getRootElement(IBundleMakerProject project, IProjectContentProvider provider);

  /**
   * @param provider
   *          the provider instance that has been passed to
   *          {@link #getRootElement(IBundleMakerProject, IProjectContentProvider)} or this method, which in turn has
   *          returned the given root element
   * 
   * @throws Exception
   */
  public List<? extends Object> getChildren(IBundleMakerProject project, IProjectContentProvider provider,
      Object rootElement) throws Exception;

  /**
   * Returns the image for left ("Resource") column
   * 
   * @param element
   * @return The Image. Might be null.
   */
  public Image getImage(Object element);

  /**
   * Returns the label for left ("Resource") column
   * 
   * @param element
   * @return the label. Might be null.
   */
  public String getLabel(Object element);

  /**
   * Returns the {@link AnalyzeMode} of the object or null if no analyze mode is available on object that doesn't
   * support AnalyzeMode
   * 
   * @param element
   * @return
   */
  public AnalyzeMode getAnalyzeMode(Object element);

  /**
   * Determines if the given object can be edited by this provider.
   * 
   * <p>
   * This method is invoked to check the enablement of the Edit button on the ProjectEditorPage
   * 
   * @param selectedObject
   * @return
   */
  public boolean canEdit(Object selectedObject);

  // public void edit(Object selectedObject);

  // ---- TODO: ---- //

  // *Edit* is only available on a single selection

  // *Removing* of ProjectContentProvider instances is directly done in the ProjectEditorPage
  // this is only invoked for selected child-objects of this provider
  // public boolean canRemove(Object[] selectedObjects);
  // public void remove(Object[] selectedObjects);

  // *Context menu*
  // passed in are *all* selected objects
  // ContextMenuAction getContextMenuActions(Object[] selectedObjects)
  // ContextMenuAction getLabel(Object[] selectedObjects), isEnabled(Object[] selectedObjects), execute(Object[]
  // selectedObject)

}
