package org.bundlemaker.core.ui.artifact.cnf;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionConstants;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.eclipse.ui.navigator.ICommonMenuConstants;
import org.eclipse.ui.navigator.ICommonViewerSite;
import org.eclipse.ui.navigator.ICommonViewerWorkbenchSite;

/**
 */
public class BundlemakerArtifactActionProvider extends CommonActionProvider {

  /** - */
  private OpenResourceArtifactAction openAction;

  /**
   * <p>
   * </p>
   * 
   * Construct Property Action provider.
   */
  public BundlemakerArtifactActionProvider() {
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.ui.navigator.CommonActionProvider#init(org.eclipse.ui.navigator.ICommonActionExtensionSite)
   */
  @Override
  public void init(ICommonActionExtensionSite aSite) {

    ICommonViewerSite viewSite = aSite.getViewSite();
    if (viewSite instanceof ICommonViewerWorkbenchSite) {
      ICommonViewerWorkbenchSite workbenchSite =
          (ICommonViewerWorkbenchSite) viewSite;
      openAction =
          new OpenResourceArtifactAction(workbenchSite.getPage(),
              workbenchSite.getSelectionProvider());
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.ui.actions.ActionGroup#fillActionBars(org.eclipse.ui.IActionBars)
   */
  @Override
  public void fillActionBars(IActionBars actionBars) {
    /* Set up the property open action when enabled. */
    if (openAction.isEnabled())
      actionBars.setGlobalActionHandler(ICommonActionConstants.OPEN, openAction);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.ui.actions.ActionGroup#fillContextMenu(org.eclipse.jface.action.IMenuManager)
   */
  @Override
  public void fillContextMenu(IMenuManager menu) {
    if (openAction.isEnabled())
      menu.appendToGroup(ICommonMenuConstants.GROUP_OPEN, openAction);
  }

}
