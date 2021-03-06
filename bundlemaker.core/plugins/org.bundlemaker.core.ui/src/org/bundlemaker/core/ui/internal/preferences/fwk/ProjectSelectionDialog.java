/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.bundlemaker.core.ui.internal.preferences.fwk;

import java.util.Set;

import org.bundlemaker.core.BundleMakerCore;
import org.bundlemaker.core.project.IProjectDescriptionAwareBundleMakerProject;
import org.bundlemaker.core.ui.BundleMakerImages;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.SelectionStatusDialog;

/**
 * <p>
 * </p>
 * 
 * <p>
 * This source was copied (and than modified) from the internal class
 * {@link org.org.bundlemaker.core.ui.internal.preferences.fwk.ProjectSelectionDialog}.
 * </p>
 * 
 * @author Gerd W&uuml;therich (gerd@gerd-wuetherich.de)
 */
public class ProjectSelectionDialog extends SelectionStatusDialog {

  // the visual selection widget group
  private TableViewer                                     _tableViewer;

  private Set<IProjectDescriptionAwareBundleMakerProject> _projectsWithSpecifics;

  // sizing constants
  private final static int                                SIZING_SELECTION_WIDGET_HEIGHT = 250;

  private final static int                                SIZING_SELECTION_WIDGET_WIDTH  = 300;

  private final static String                             DIALOG_SETTINGS_SHOW_ALL       = "ProjectSelectionDialog.show_all"; //$NON-NLS-1$

  private ViewerFilter                                    fFilter;

  public ProjectSelectionDialog(Shell parentShell, Set<IProjectDescriptionAwareBundleMakerProject> projectsWithSpecifics) {
    super(parentShell);
    setTitle("Project Specific Configuration");
    setMessage("Select the project to configure:");
    _projectsWithSpecifics = projectsWithSpecifics;

    fFilter = new ViewerFilter() {
      @Override
      public boolean select(Viewer viewer, Object parentElement, Object element) {
        return _projectsWithSpecifics.contains(element);
      }
    };
  }

  /*
   * (non-Javadoc) Method declared on Dialog.
   */
  @Override
  protected Control createDialogArea(Composite parent) {
    // page group
    Composite composite = (Composite) super.createDialogArea(parent);

    Font font = parent.getFont();
    composite.setFont(font);

    createMessageArea(composite);

    _tableViewer = new TableViewer(composite, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
    _tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
      @Override
      public void selectionChanged(SelectionChangedEvent event) {
        doSelectionChanged(((IStructuredSelection) event.getSelection()).toArray());
      }
    });
    _tableViewer.addDoubleClickListener(new IDoubleClickListener() {
      @Override
      public void doubleClick(DoubleClickEvent event) {
        okPressed();
      }
    });
    GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
    data.heightHint = SIZING_SELECTION_WIDGET_HEIGHT;
    data.widthHint = SIZING_SELECTION_WIDGET_WIDTH;
    _tableViewer.getTable().setLayoutData(data);

    _tableViewer.setLabelProvider(new ColumnLabelProvider() {

      @Override
      public Image getImage(Object element) {
        return BundleMakerImages.BUNDLEMAKER_PROJECT.getImage();
      }

      @Override
      public String getText(Object element) {
        return ((IProjectDescriptionAwareBundleMakerProject) element).getName();
      }
    });
    _tableViewer.setContentProvider(ArrayContentProvider.getInstance());
    _tableViewer.setComparator(new ViewerComparator());
    _tableViewer.getControl().setFont(font);

    Button checkbox = new Button(composite, SWT.CHECK);
    checkbox.setText("Show only projects with project specific settings");
    checkbox.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true, false));
    checkbox.addSelectionListener(new SelectionListener() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        updateFilter(((Button) e.widget).getSelection());
      }

      @Override
      public void widgetDefaultSelected(SelectionEvent e) {
        updateFilter(((Button) e.widget).getSelection());
      }
    });
    // IDialogSettings dialogSettings= JavaPlugin.getDefault().getDialogSettings();
    // boolean doFilter= !dialogSettings.getBoolean(DIALOG_SETTINGS_SHOW_ALL) &&
    // !fProjectsWithSpecifics.isEmpty();
    boolean doFilter = false;
    checkbox.setSelection(doFilter);
    updateFilter(doFilter);

    _tableViewer.setInput(BundleMakerCore.getBundleMakerProjects());

    doSelectionChanged(new Object[0]);
    Dialog.applyDialogFont(composite);
    return composite;
  }

  protected void updateFilter(boolean selected) {
    if (selected) {
      _tableViewer.addFilter(fFilter);
    } else {
      _tableViewer.removeFilter(fFilter);
    }
    // JavaPlugin.getDefault().getDialogSettings().put(DIALOG_SETTINGS_SHOW_ALL, !selected);
  }

  private void doSelectionChanged(Object[] objects) {
    if (objects.length != 1) {
      updateStatus(new StatusInfo(IStatus.ERROR, "")); //$NON-NLS-1$
      setSelectionResult(null);
    } else {
      updateStatus(new StatusInfo());
      setSelectionResult(objects);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.ui.dialogs.SelectionStatusDialog#computeResult()
   */
  @Override
  protected void computeResult() {
  }
}
