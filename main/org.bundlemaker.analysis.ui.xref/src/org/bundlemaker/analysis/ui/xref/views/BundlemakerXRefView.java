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
package org.bundlemaker.analysis.ui.xref.views;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bundlemaker.analysis.model.ArtifactType;
import org.bundlemaker.analysis.model.IArtifact;
import org.bundlemaker.analysis.model.IDependency;
import org.bundlemaker.analysis.ui.Analysis;
import org.bundlemaker.analysis.ui.DefaultArtifactLabelProvider;
import org.bundlemaker.analysis.ui.editor.DependencyPart;
import org.bundlemaker.analysis.ui.selection.IArtifactSelectionChangedEvent;
import org.bundlemaker.analysis.ui.selection.IArtifactSelectionListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * View for analysing IArtifact usedBy and uses dependencies. This view consists of three trees. In the middle tree the
 * subtree of the current artifact selection is displayed. Therefore this class registers an IArtifactSelectionListener
 * at the class<br>
 * 
 * <pre>
 * org.bundlemaker.analysis.ui.Analysis
 * </pre>
 * 
 * The left tree shows the artifacts which uses the artifacts in the middle. The right tree shows the artifacts which
 * are used by the middle. When the tree selection is changed the dependencies between the middle selection and the last
 * left or right selection is displayed in detail in the tree dependency view.
 * 
 * @see org.bundlemaker.analysis.ui.Analysis
 * @author Frank Schlueter
 */
public class BundlemakerXRefView extends DependencyPart implements IArtifactSelectionListener {

  /**
   * The ID of the view as specified by the extension.
   */
  public static final String ID                   = "org.bundlemaker.analysis.ui.xref.views.BundlemakerXRefView";

  private TreeViewer         leftTree;

  private TreeViewer         middleTree;

  private TreeViewer         rightTree;

  private IArtifact          rootArtifact;

  private List<IArtifact>    middleSelectedArtifacts;

  private List<IArtifact>    dependentSelectedArtifacts;

  private boolean            showUsedDependencies = true;

  /**
   * The constructor.
   */
  public BundlemakerXRefView() {
  }

  /**
   * Passing the focus request to the viewer's control.
   */
  @Override
  public void setFocus() {
    middleTree.getControl().setFocus();
  }

  @Override
  protected void doInit(Composite composite) {
    Composite panel = new Composite(composite, SWT.NONE);
    panel.setLayout(new GridLayout(3, true));

    leftTree = createTreeViewer(panel);
    leftTree.addSelectionChangedListener(new ISelectionChangedListener() {

      @SuppressWarnings("unchecked")
      @Override
      public void selectionChanged(SelectionChangedEvent event) {
        TreeSelection treeSelection = (TreeSelection) event.getSelection();
        selectLeftTree(treeSelection.toList());
      }
    });

    middleTree = createTreeViewer(panel);
    middleTree.addSelectionChangedListener(new ISelectionChangedListener() {

      @SuppressWarnings("unchecked")
      @Override
      public void selectionChanged(SelectionChangedEvent event) {
        TreeSelection treeSelection = (TreeSelection) event.getSelection();
        selectMiddleTree(treeSelection.toList());
      }
    });

    rightTree = createTreeViewer(panel);
    rightTree.addSelectionChangedListener(new ISelectionChangedListener() {

      @SuppressWarnings("unchecked")
      @Override
      public void selectionChanged(SelectionChangedEvent event) {
        TreeSelection treeSelection = (TreeSelection) event.getSelection();
        selectRightTree(treeSelection.toList());
      }
    });
    Analysis.instance().getArtifactSelectionService().addArtifactSelectionListener(this);
  }

  private TreeViewer createTreeViewer(Composite parent) {
    TreeViewer treeViewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
    DefaultArtifactLabelProvider artifactLabelProvider = new DefaultArtifactLabelProvider();
    treeViewer.setContentProvider(new ArtifactTreeContentProvider());
    treeViewer.getTree().setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
    treeViewer.setLabelProvider(artifactLabelProvider);

    return treeViewer;
  }

  private void selectLeftTree(List<IArtifact> selectedArtifacts) {
    dependentSelectedArtifacts = selectedArtifacts;
    showUsedDependencies = true;
    rightTree.getTree().deselectAll();
    showDependencyDetails(selectedArtifacts, middleSelectedArtifacts);
  }

  private void selectMiddleTree(List<IArtifact> selectedArtifacts) {
    middleSelectedArtifacts = selectedArtifacts;
    List<IArtifact> dependentArtifacts = getDependencies(selectedArtifacts);
    List<IArtifact> usedByArtifacts = getUsedByArtifacts(selectedArtifacts);
    leftTree.setFilters(new ViewerFilter[] { new DependentArtifactsFilter(usedByArtifacts) });
    rightTree.setFilters(new ViewerFilter[] { new DependentArtifactsFilter(dependentArtifacts) });
    if (showUsedDependencies) {
      showDependencyDetails(middleSelectedArtifacts, dependentSelectedArtifacts);
    } else {
      showDependencyDetails(dependentSelectedArtifacts, middleSelectedArtifacts);
    }
  }

  private void selectRightTree(List<IArtifact> selectedArtifacts) {
    showUsedDependencies = false;
    leftTree.getTree().deselectAll();
    showDependencyDetails(middleSelectedArtifacts, selectedArtifacts);
  }

  private void showDependencyDetails(List<IArtifact> fromArtifacts, List<IArtifact> toArtifacts) {
    List<IDependency> dependencies = new ArrayList<IDependency>();
    if ((fromArtifacts != null) && (toArtifacts != null)) {
      for (IArtifact artifact : fromArtifacts) {
        dependencies.addAll(artifact.getDependencies(toArtifacts));
      }
      Analysis.instance().getDependencySelectionService().setSelection(ID, dependencies);
      Analysis.instance().openDependencyTreeTableView();
    }
  }

  private List<IArtifact> getDependencies(List<IArtifact> selectedArtifacts) {
    List<IArtifact> dependentArtifacts = new ArrayList<IArtifact>();
    for (IArtifact artifact : selectedArtifacts) {
      for (IDependency dependency : artifact.getDependencies()) {
        dependentArtifacts.add(dependency.getTo());
      }
    }
    return dependentArtifacts;
  }

  private List<IArtifact> getUsedByArtifacts(List<IArtifact> selectedArtifacts) {
    Collection<IDependency> usedByDependencies = rootArtifact.getDependencies(selectedArtifacts);
    List<IArtifact> dependentArtifacts = new ArrayList<IArtifact>();
    for (IDependency dependency : usedByDependencies) {
      Collection<IDependency> leafDependencies = new ArrayList<IDependency>();
      dependency.getLeafDependencies(leafDependencies);
      for (IDependency leafDependency : leafDependencies) {
        dependentArtifacts.add(leafDependency.getFrom());
      }
    }
    return dependentArtifacts;
  }

  @Override
  protected void doDispose() {
  }

  @Override
  public void artifactSelectionChanged(IArtifactSelectionChangedEvent event) {
    middleSelectedArtifacts = null;
    dependentSelectedArtifacts = null;
    middleTree.setInput(event.getSelection().getSelectedArtifacts());
    rootArtifact = event.getSelection().getSelectedArtifacts().get(0).getParent(ArtifactType.Root);
    leftTree.setInput(rootArtifact);
    rightTree.setInput(rootArtifact);
  }

  @Override
  protected void useArtifacts(List<IArtifact> artifacts) {
  }
}