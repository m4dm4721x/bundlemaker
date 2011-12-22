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
package org.bundlemaker.core.ui.editor.resources;

import static java.lang.String.format;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import org.bundlemaker.core.BundleMakerProjectChangedEvent;
import org.bundlemaker.core.BundleMakerProjectChangedEvent.Type;
import org.bundlemaker.core.BundleMakerProjectState;
import org.bundlemaker.core.IBundleMakerProjectChangedListener;
import org.bundlemaker.core.projectdescription.AnalyzeMode;
import org.bundlemaker.core.projectdescription.IModifiableProjectDescription;
import org.bundlemaker.core.projectdescription.IProjectContentEntry;
import org.bundlemaker.core.projectdescription.IProjectDescription;
import org.bundlemaker.core.projectdescription.file.FileBasedContentProvider;
import org.bundlemaker.core.projectdescription.file.FileBasedContentProviderFactory;
import org.bundlemaker.core.ui.editor.BundleMakerProjectProvider;
import org.bundlemaker.core.ui.editor.ModifyProjectContentDialog;
import org.bundlemaker.core.ui.internal.UIImages;
import org.bundlemaker.core.ui.internal.VerticalFormButtonBar;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.ui.wizards.BuildPathDialogAccess;
import org.eclipse.jface.layout.TreeColumnLayout;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.model.BaseWorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * A block (represented by a Form Section) that can be used to edit either the resources or the types of the bundlemaker
 * project description
 * 
 * @see IProjectDescription
 * @author Nils Hartmann (nils@nilshartmann.net)
 * 
 */
public class ProjectResourcesBlock implements IBundleMakerProjectChangedListener {

  /**
   * Name of the property, that will be fired when the project content has been changed by this block
   */
  public final static String               PROJECT_CONTENT_PROPERTY = "bundleMakerProjectContent";

  /**
   * change listeners
   */
  private final ListenerList               _listeners               = new ListenerList();

  private final String                     _title;

  private final String                     _description;

  private final BundleMakerProjectProvider _bundleMakerProjectProvider;

  private TreeViewer                       _treeViewer;

  private SectionPart                      _resourcesSectionPart;

  private Button                           _removeButton;

  private Button                           _editButton;

  private Button                           _moveDownButton;

  private Button                           _moveUpButton;

  private Button                           _parseProjectButton;

  /**
   * @param editResources
   *          true if this block edits resources, false if it edits the project's types
   * @param title
   *          the title of this block
   * @param description
   *          the description to be display in the section's message part
   * @param provider
   */
  public ProjectResourcesBlock(String title, String description, BundleMakerProjectProvider provider) {
    _title = title;
    _description = description;
    _bundleMakerProjectProvider = provider;
  }

  public void createControl(IManagedForm mform) {

    FormToolkit toolkit = mform.getToolkit();
    final ScrolledForm form = mform.getForm();

    // Create the collapsible section
    Section resourcesSection = toolkit.createSection(form.getBody(), Section.TITLE_BAR | Section.EXPANDED
        | Section.TWISTIE);
    FormText descriptionText = toolkit.createFormText(resourcesSection, false);
    descriptionText.setText("<form><p>" + _description + "</p></form>", true, false);
    resourcesSection.setDescriptionControl(descriptionText);
    resourcesSection.setText(_title);
    resourcesSection.setLayoutData(new GridData(GridData.FILL_BOTH));

    Composite client = toolkit.createComposite(resourcesSection);
    client.setLayoutData(new GridData(GridData.FILL_BOTH));
    client.setLayout(new GridLayout(2, false));
    resourcesSection.setClient(client);

    _resourcesSectionPart = new SectionPart(resourcesSection);
    mform.addPart(_resourcesSectionPart);

    // Create the tree view and viewer that displays the IFileBasedContent entries
    Composite treeComposite = new Composite(client, SWT.NO);
    final Tree projectContentTree = toolkit.createTree(treeComposite, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL
        | SWT.FULL_SELECTION);
    GridData gd = new GridData(GridData.FILL_BOTH);
    gd.heightHint = 200;
    gd.widthHint = 100;
    treeComposite.setLayoutData(gd);
    projectContentTree.setLinesVisible(true);

    _treeViewer = new TreeViewer(projectContentTree);
    _treeViewer.setLabelProvider(new WorkbenchLabelProvider());
    // _treeViewer.setLabelProvider(new DecoratingLabelProvider(new WorkbenchLabelProvider(),
    // new ProjectDescriptionLabelDecorator()));
    // new WorkbenchLabelProvider());
    _treeViewer.setContentProvider(new BaseWorkbenchContentProvider());
    createColumns();

    final Shell shell = client.getShell();

    _treeViewer.setInput(_bundleMakerProjectProvider.getBundleMakerProject());
    _treeViewer.addDoubleClickListener(new IDoubleClickListener() {

      @Override
      public void doubleClick(DoubleClickEvent event) {
        TreeSelection ts = (TreeSelection) event.getSelection();
        if (!ts.isEmpty()) {
          editContent(shell);
        }
      }
    });

    // Create the buttonbar
    final VerticalFormButtonBar buttonBar = new VerticalFormButtonBar(client, toolkit);

    _editButton = buttonBar.newButton("Edit...", new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        editContent(shell);
      }

    });

    // Create buttons
    buttonBar.newButton("Add...", new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        addContent(shell);
      }

    });

    buttonBar.newButton("Add archives...", new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        addArchives(shell);
      }
    });

    buttonBar.newButton("Add external archives...", new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        addExternalArchives(shell);
      }
    });

    _removeButton = buttonBar.newButton("Remove", new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        removeContent();
      }
    });
    _moveUpButton = buttonBar.newButton("Up", new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        // TODO
        System.out.println(" MOVE UP NEEDS TO BE IMPLEMENTED !!!!");
        // moveUp();
      }
    });
    _moveDownButton = buttonBar.newButton("Down", new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        // TODO
        System.out.println(" MOVE DOWN NEEDS TO BE IMPLEMENTED !!!!");

        // moveDown();
      }
    });

    _parseProjectButton = buttonBar.newButton("Parse project", new SelectionAdapter() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        _bundleMakerProjectProvider.parseProject();
      }

    });
    GridData layoutData = new GridData(GridData.FILL_HORIZONTAL);
    layoutData.grabExcessVerticalSpace = true;
    layoutData.verticalAlignment = SWT.BOTTOM;
    _parseProjectButton.setImage(UIImages.REFRESH.getImage());
    _parseProjectButton.setLayoutData(layoutData);

    _treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {

      @Override
      public void selectionChanged(SelectionChangedEvent event) {
        refreshEnablement();
      }
    });

    // set initial enablement
    refreshEnablement();

    // paint the borders
    toolkit.paintBordersFor(client);

    _bundleMakerProjectProvider.getBundleMakerProject().addBundleMakerProjectChangedListener(this);
  }

  @Override
  public void bundleMakerProjectChanged(BundleMakerProjectChangedEvent event) {
    if (event.getType() == Type.PROJECT_STATE_CHANGED) {
      if (_bundleMakerProjectProvider.getBundleMakerProject().getState() == BundleMakerProjectState.INITIALIZED) {
        // in state INITIALIZED project description is re-creates, so we have to refresh the references
        // to it
        Display.getDefault().syncExec(new Runnable() {
          @Override
          public void run() {
            _treeViewer.refresh();
          }
        });

      }
    }
  }

  /**
   * 
   */
  private void createColumns() {
    Tree tree = _treeViewer.getTree();
    TreeColumnLayout layout = new TreeColumnLayout();
    TreeViewerColumn column = new TreeViewerColumn(_treeViewer, SWT.NONE);
    column.setLabelProvider(new ResourceNameColumnLabelProvider());
    column.getColumn().setResizable(true);
    column.getColumn().setMoveable(true);
    column.getColumn().setText("Resource");
    layout.setColumnData(column.getColumn(), new ColumnWeightData(80));

    column = new TreeViewerColumn(_treeViewer, SWT.NONE);
    column.setLabelProvider(new BundleMakerProjectDescriptionColumnLabelProvider(1));
    column.setEditingSupport(FileBasedContentEditingSupport.newEditingSupportForAnalyzeResource(this, _treeViewer));
    column.getColumn().setResizable(true);
    column.getColumn().setMoveable(true);
    column.getColumn().setText("Analyze");
    column.getColumn().setAlignment(SWT.CENTER);
    layout.setColumnData(column.getColumn(), new ColumnWeightData(10));

    column = new TreeViewerColumn(_treeViewer, SWT.NONE);
    column.setLabelProvider(new BundleMakerProjectDescriptionColumnLabelProvider(2));
    column.setEditingSupport(FileBasedContentEditingSupport.newEditingSupportForAnalyzeSources(this, _treeViewer));
    column.getColumn().setResizable(true);
    column.getColumn().setMoveable(true);
    column.getColumn().setText("Analyze Sources");
    column.getColumn().setAlignment(SWT.CENTER);
    layout.setColumnData(column.getColumn(), new ColumnWeightData(20));

    tree.getParent().setLayout(layout);
    tree.setHeaderVisible(true);
    tree.layout(true);
  }

  /**
   * Opens the {@link ModifyProjectContentDialog} to edit the currently selected IFileBasedContent
   * 
   * @param shell
   */
  private void editContent(Shell shell) {

    // ITreeSelection selection = getSelection();
    //
    // if (selection.size() != 1) {
    // return;
    // }
    //
    // boolean projectDescriptionHasChanged = false;
    //
    // TreePath path = selection.getPaths()[0];
    // Object selectedObject = path.getLastSegment();
    // if (selectedObject instanceof FileBasedContentProvider) {
    // FileBasedContentProvider contentProvider = (FileBasedContentProvider) selectedObject;
    // FileBasedContent content = contentProvider.getFileBasedContent();
    // projectDescriptionHasChanged = editFileBasedContent(shell, contentProvider);
    // projectDescriptionChanged();
    // System.out.printf("content after projectDescriptionChanged: %s  name: %s%n", content, content.getName());
    // return;
    // }
    //
    // if (selectedObject instanceof VariablePath) {
    // VariablePath rootPath = (VariablePath) selectedObject;
    //
    // // open edit dialog
    // String currentPath = RootPathHelper.getLabel(rootPath);
    // EditEntryDialog editEntryDialog = new EditEntryDialog(shell, currentPath);
    // if (editEntryDialog.open() != Window.OK) {
    // return;
    // }
    //
    // String newPath = editEntryDialog.getEntry();
    // if (currentPath.equals(newPath)) {
    // // no changes
    // return;
    // }
    //
    // // Get existing root paths from owning FileBasedContent
    // IProjectContentProvider content = RootPathHelper.getOwningFileBasedContent(path);
    // if (content instanceof FileBasedContentProvider) {
    // FileBasedContentProvider fileBasedContentProvider = (FileBasedContentProvider) content;
    // // TODO
    // Set<VariablePath> existingPaths = null;
    // // if (rootPath.isBinaryPath()) {
    // // existingPaths = content.getBinaryRootPaths();
    // // } else {
    // // existingPaths = content.getSourceRootPaths();
    // // }
    //
    // // Create new set of root paths, replacing the old path with the new, edited, root path
    // List<String> newRootPaths = new LinkedList<String>();
    // for (VariablePath iRootPath : existingPaths) {
    // String existingPath = RootPathHelper.getLabel(iRootPath);
    // if (!existingPath.equals(currentPath)) {
    // newRootPaths.add(existingPath);
    // } else {
    // newRootPaths.add(newPath);
    // }
    // }
    //
    // // Set new array of root paths to FileBasedContent
    // if (rootPath.isBinaryPath()) {
    // content.setBinaryPaths(newRootPaths.toArray(new String[0]));
    // } else {
    // content.setSourcePaths(newRootPaths.toArray(new String[0]));
    // }
    //
    // projectDescriptionHasChanged = true;
    //
    // }
    // }
    //
    // if (projectDescriptionHasChanged) {
    // projectDescriptionChanged();
    // }
  }

  private boolean editFileBasedContent(Shell shell, FileBasedContentProvider content) {

    System.out.printf("content before opening: %s  name: %s%n", content, content.getFileBasedContent().getName());

    ModifyProjectContentDialog dialog = new ModifyProjectContentDialog(shell, content.getFileBasedContent());
    if (dialog.open() != Window.OK) {
      return false;
    }

    // Update the content
    content.setName(dialog.getName());
    content.setVersion(dialog.getVersion());
    content.setBinaryPaths(dialog.getBinaryPaths().toArray(new String[0]));
    content.setSourcePaths(dialog.getSourcePaths().toArray(new String[0]));
    content.setAnalyzeMode(getAnalyzeMode(dialog.isAnalyze(), dialog.isAnalyzeSources()));

    return true;
  }

  private AnalyzeMode getAnalyzeMode(boolean analyze, boolean analyzeSources) {
    if (analyzeSources) {
      return AnalyzeMode.BINARIES_AND_SOURCES;
    }

    if (analyze) {
      return AnalyzeMode.BINARIES_ONLY;
    }

    return AnalyzeMode.DO_NOT_ANALYZE;
  }

  // private void moveUp() {
  // Collection<IModifiableFileBasedContent> selectedContents =
  // getSelectedElementsOfType(IModifiableFileBasedContent.class);
  // if (selectedContents.isEmpty()) {
  // return;
  // }
  //
  // @SuppressWarnings("unchecked")
  // List<IModifiableFileBasedContent> modifiableFileBasedContent = (List<IModifiableFileBasedContent>)
  // getBundleMakerProjectDescription()
  // .getModifiableFileBasedContent();
  // List<IModifiableFileBasedContent> newList = moveUp(modifiableFileBasedContent, selectedContents);
  //
  // modifiableFileBasedContent.clear();
  // modifiableFileBasedContent.addAll(newList);
  //
  // projectDescriptionChanged();
  //
  // }
  //
  // private void moveDown() {
  // Collection<IModifiableFileBasedContent> toMoveDown = getSelectedElementsOfType(IModifiableFileBasedContent.class);
  //
  // if (toMoveDown.isEmpty()) {
  // return;
  // }
  //
  // @SuppressWarnings("unchecked")
  // List<IModifiableFileBasedContent> fileBasedContent = (List<IModifiableFileBasedContent>)
  // getBundleMakerProjectDescription()
  // .getModifiableFileBasedContent();
  // List<IModifiableFileBasedContent> newOrder = reverse(moveUp(reverse(fileBasedContent), toMoveDown));
  // fileBasedContent.clear();
  // fileBasedContent.addAll(newOrder);
  //
  // projectDescriptionChanged();
  //
  // }
  //
  // /**
  // * from org.eclipse.jdt.internal.ui.wizards.dialogfields.ListDialogField
  // *
  // */
  // private List<IModifiableFileBasedContent> reverse(List<IModifiableFileBasedContent> p) {
  // List<IModifiableFileBasedContent> reverse = new ArrayList<IModifiableFileBasedContent>(p.size());
  // for (int i = p.size() - 1; i >= 0; i--) {
  // reverse.add(p.get(i));
  // }
  // return reverse;
  // }
  //
  // /**
  // * from org.eclipse.jdt.internal.ui.wizards.dialogfields.ListDialogField
  // *
  // */
  // private List<IModifiableFileBasedContent> moveUp(List<IModifiableFileBasedContent> elements,
  // Collection<IModifiableFileBasedContent> move) {
  // int nElements = elements.size();
  // List<IModifiableFileBasedContent> res = new ArrayList<IModifiableFileBasedContent>(nElements);
  // IModifiableFileBasedContent floating = null;
  // for (int i = 0; i < nElements; i++) {
  // IModifiableFileBasedContent curr = elements.get(i);
  // if (move.contains(curr)) {
  // res.add(curr);
  // } else {
  // if (floating != null) {
  // res.add(floating);
  // }
  // floating = curr;
  // }
  // }
  // if (floating != null) {
  // res.add(floating);
  // }
  // return res;
  // }

  /**
   * Add content to the project description using the {@link ModifyProjectContentDialog}
   * 
   * @param shell
   */
  private void addContent(Shell shell) {
    ModifyProjectContentDialog dialog = new ModifyProjectContentDialog(shell);
    if (dialog.open() != Window.OK) {
      return;
    }

    // add the new content to the project description

    FileBasedContentProviderFactory.addNewFileBasedContentProvider(getBundleMakerProjectDescription(),
        dialog.getName(), dialog.getVersion(), dialog.getBinaryPaths(), dialog.getSourcePaths(),
        getAnalyzeMode(dialog.isAnalyze(), dialog.isAnalyzeSources()));

    projectDescriptionChanged();

  }

  /**
   * Remove the selected content from the project
   */
  private void removeContent() {

    ITreeSelection selection = getSelection();
    if (selection.isEmpty()) {
      return;
    }

    TreePath[] paths = selection.getPaths();

    for (TreePath treePath : paths) {
      Object element = treePath.getLastSegment();
      if (element instanceof IProjectContentEntry) {
        IProjectContentEntry content = (IProjectContentEntry) element;
        getBundleMakerProjectDescription().removeContentProvider(content.getId());
      }
      // if (element instanceof IVariablePath) {
      // IVariablePath rootPath = (IVariablePath) element;
      // IModifiableFileBasedContent fileBasedContent = RootPathHelper.getOwningFileBasedContent(treePath);
      // if (rootPath.isBinaryPath()) {
      // fileBasedContent.getModifiableBinaryPaths().remove(rootPath);
      // } else {
      // fileBasedContent.getModifiableSourcePaths().remove(rootPath);
      // }
      // }

    }

    projectDescriptionChanged();

  }

  protected ITreeSelection getSelection() {
    ITreeSelection selection = (ITreeSelection) _treeViewer.getSelection();
    return selection;
  }

  /**
   * Returns all selected elements from the TreeViewer that are instances of the specified class
   * 
   * @param <T>
   * @param type
   * @return
   */
  private <T> Collection<T> getSelectedElementsOfType(Class<T> type) {
    java.util.List<T> result = new LinkedList<T>();

    ITreeSelection selection = getSelection();

    Iterator<?> iterator = selection.iterator();
    while (iterator.hasNext()) {
      Object element = iterator.next();
      if (type.isInstance(element)) {
        @SuppressWarnings("unchecked")
        T content = (T) element;
        result.add(content);
      }
    }
    return result;

  }

  /**
   * Returns the {@link IProjectDescription} this block works on
   * 
   * @return
   */
  private IModifiableProjectDescription getBundleMakerProjectDescription() {
    return _bundleMakerProjectProvider.getBundleMakerProject().getModifiableProjectDescription();
  }

  /**
   * Adds the specified {@link IPropertyChangeListener} to the list of listeners
   * 
   * @param listener
   */
  public void addPropertyChangeListener(IPropertyChangeListener listener) {
    _listeners.add(listener);
  }

  /**
   * removes the specified {@link IPropertyChangeListener}
   * 
   * @param listener
   */
  public void removePropertyChangeListener(IPropertyChangeListener listener) {
    _listeners.remove(listener);
  }

  /**
   * Notifies the registered listener that the project description has been changed
   */
  private void firePropertyChange() {
    PropertyChangeEvent event = new PropertyChangeEvent(this, PROJECT_CONTENT_PROPERTY, null, null);
    Object[] listeners = _listeners.getListeners();
    for (int i = 0; i < listeners.length; i++) {
      IPropertyChangeListener listener = (IPropertyChangeListener) listeners[i];
      listener.propertyChange(event);
    }
  }

  /**
   * Adds external archives to the project description
   * 
   * @param shell
   */
  private void addExternalArchives(Shell shell) {
    FileDialog fileDialog = new FileDialog(shell, SWT.MULTI);
    fileDialog.setText("Add Resources");
    fileDialog.setFilterExtensions(new String[] { "*.jar;*.zip", "*.*" });
    if (fileDialog.open() == null) {
      return;
    }
    String[] fileNames = fileDialog.getFileNames();
    if (fileNames.length < 1) {
      return;
    }

    // Add all selected archives to the project description
    for (String string : fileNames) {
      IPath path = new Path(fileDialog.getFilterPath()).append(string);
      String binaryRoot = path.toOSString();

      FileBasedContentProviderFactory.addNewFileBasedContentProvider(getBundleMakerProjectDescription(), binaryRoot,
          null, AnalyzeMode.BINARIES_AND_SOURCES);

    }

    projectDescriptionChanged();

  }

  /**
   * Adds archives from the workspace to the project description
   * 
   * @param shell
   */
  private void addArchives(Shell shell) {
    IPath[] selected = BuildPathDialogAccess.chooseJAREntries(shell, _bundleMakerProjectProvider
        .getBundleMakerProject().getProject().getFullPath(), new IPath[0]);

    if (selected == null || selected.length < 1) {
      return;
    }

    for (IPath iPath : selected) {
      String workspacePath = format("${workspace_loc:%s}", iPath.toString());
      FileBasedContentProviderFactory.addNewFileBasedContentProvider(getBundleMakerProjectDescription(), workspacePath,
          null, AnalyzeMode.BINARIES_ONLY);
    }

    projectDescriptionChanged();

  }

  /**
   * Indicate the the project description has been changed
   */
  protected void projectDescriptionChanged() {
    // Refresh view
    _treeViewer.refresh();

    // notify listener
    firePropertyChange();

    // mark editor dirty
    _resourcesSectionPart.markDirty();

    refreshEnablement();
  }

  /**
   * sets the enabled state of the buttons according to the selection in the tree viewer
   */
  private void refreshEnablement() {
    ITreeSelection selection = (ITreeSelection) _treeViewer.getSelection();
    int selectedElements = selection.size();

    _removeButton.setEnabled(selectedElements > 0);
    _editButton.setEnabled(selectedElements == 1);

    TreeItem[] selectedItems = _treeViewer.getTree().getSelection();
    System.out.println("selecteditems: " + selectedItems.length);
    if (selectedItems.length > 0) {
      // TODO: Allow multiple selection
      // TODO: what should happen if a path (not a IFileBasedContent) is selected?
      int selectedIndex = _treeViewer.getTree().indexOf(selectedItems[0]);
      _moveDownButton.setEnabled(selectedIndex < _treeViewer.getTree().getItemCount() - 1);
      _moveUpButton.setEnabled(selectedIndex > 0);
    } else {
      _moveDownButton.setEnabled(false);
      _moveUpButton.setEnabled(false);
    }
  }

  /**
   * 
   */
  public void dispose() {
    _bundleMakerProjectProvider.getBundleMakerProject().removeBundleMakerProjectChangedListener(this);

  }
}
