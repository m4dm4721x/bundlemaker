package org.bundlemaker.core.ui.selection.workbench.editor;

import org.bundlemaker.core.analysis.IArtifactModelChangedListener;
import org.bundlemaker.core.ui.selection.IRootArtifactSelection;
import org.bundlemaker.core.ui.selection.IRootArtifactSelectionChangedEvent;
import org.bundlemaker.core.ui.selection.IRootArtifactSelectionListener;
import org.bundlemaker.core.ui.selection.IRootArtifactSelectionService;
import org.bundlemaker.core.ui.selection.Selection;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;

/**
 * <p>
 * </p>
 * 
 * @author Gerd W&uuml;therich (gerd@gerd-wuetherich.de)
 */
public abstract class AbstractRootArtifactSelectionAwareEditorPart extends EditorPart implements
    IRootArtifactSelectionListener, IArtifactModelChangedListener {

  /** - */
  private IRootArtifactSelection _rootArtifactSelection;

  /**
   * <p>
   * Creates a new instance of type {@link AbstractRootArtifactSelectionAwareEditorPart}.
   * </p>
   */
  public AbstractRootArtifactSelectionAwareEditorPart() {
    _rootArtifactSelection = null;
  }

  /**
   * <p>
   * </p>
   * 
   * @param selectedArtifacts
   */
  protected abstract void onRootArtifactSelectionChanged(IRootArtifactSelection rootArtifactSelection);

  /**
   * <p>
   * Returns the {@link IRootArtifactSelection} instances that should be visualized
   * </p>
   * 
   * @return
   */
  public IRootArtifactSelection getCurrentRootArtifactSelection() {
    return _rootArtifactSelection;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isDirty() {
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isSaveAsAllowed() {
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void doSave(IProgressMonitor monitor) {
    // nothing to do here
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void doSaveAs() {
    // nothing to do here
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void init(IEditorSite site, IEditorInput input) throws PartInitException {

    setInput(input);
    setSite(site);

    // add listener
    Selection.instance().getRootArtifactSelectionService().addRootArtifactSelectionListener(getProviderId(), this);

    // register part listener
    // this.getSite().getPage().addPartListener(listener);
  }

  /**
   * <p>
   * </p>
   */
  public void initRootArtifactSelection() {
    //
    IRootArtifactSelection currentRootArtifactSelection = Selection.instance().getRootArtifactSelectionService()
        .getRootArtifactSelection(getProviderId());

    if (currentRootArtifactSelection != null) {

      _rootArtifactSelection = currentRootArtifactSelection;

      registerArtifactModelChangedListener();

      onRootArtifactSelectionChanged(currentRootArtifactSelection);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void dispose() {

    // Remove ourself from the list of listeners
    Selection.instance().getRootArtifactSelectionService().removeRootArtifactSelectionListener(this);

    // invoke super
    super.dispose();
  }

  /**
   * <p>
   * </p>
   * 
   * @return the IRootArtifactSelectionService
   */
  protected IRootArtifactSelectionService getRootArtifactSelectionService() {
    return Selection.instance().getRootArtifactSelectionService();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void rootArtifactSelectionChanged(IRootArtifactSelectionChangedEvent event) {

    unregisterArtifactModelChangedListener();

    _rootArtifactSelection = event.getSelection();

    onRootArtifactSelectionChanged(_rootArtifactSelection);

    registerArtifactModelChangedListener();
  }

  protected String getProviderId() {
    return Selection.MAIN_ARTIFACT_SELECTION_ID;
  }

  /**
   * <p>
   * </p>
   * 
   */
  private void registerArtifactModelChangedListener() {
    if (_rootArtifactSelection != null && _rootArtifactSelection.hasSelectedRootArtifact()) {
      _rootArtifactSelection.getSelectedRootArtifact().addArtifactModelChangedListener(this);
    }
  }

  /**
   * <p>
   * </p>
   * 
   */
  private void unregisterArtifactModelChangedListener() {
    if (_rootArtifactSelection != null && _rootArtifactSelection.hasSelectedRootArtifact()) {
      _rootArtifactSelection.getSelectedRootArtifact().removeArtifactModelChangedListener(this);
    }
  }
}
