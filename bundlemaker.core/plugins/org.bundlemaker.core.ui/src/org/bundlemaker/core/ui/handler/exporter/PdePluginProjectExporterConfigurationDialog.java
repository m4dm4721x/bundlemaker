/*******************************************************************************
 * Copyright (c) 2012 Bundlemaker project team.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Bundlemaker project team - initial API and implementation
 ******************************************************************************/
package org.bundlemaker.core.ui.handler.exporter;

import java.io.File;

import org.bundlemaker.core.osgi.manifest.IManifestPreferences.DependencyStyle;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

/**
 * @author Nils Hartmann (nils@nilshartmann.net)
 * 
 */
public class PdePluginProjectExporterConfigurationDialog extends AbstractExporterConfigurationDialog {
  private Button  _useClassificationCheckbox;

  private boolean _useClassification = false;

  private Button  _defaultLocationCheckbox;

  private boolean _defaultLocation   = true;

  private Combo   _locationCombo;

  private Button  _browseLocationButton;

  private String  _location;

  private Combo           _dependencyStyleCombo;

  private DependencyStyle _dependencyStyle;

  private Button          _useOptionalResolutionOnMissingImportsCheckBox;

  private boolean         _useOptionalResolutionOnMissingImports;

  /**
   * @param parentShell
   */
  public PdePluginProjectExporterConfigurationDialog(Shell parentShell) {
    super(parentShell);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.bundlemaker.core.ui.handler.exporter.AbstractExporterConfigurationDialog#createControls(org.eclipse.swt.widgets
   * .Composite)
   */
  @Override
  protected void createControls(Composite dialogComposite) {

    setMessage("Configure PDE project exporter");
    setTitle("Export PDE projects");

    Group destinationGroup = createGroup(dialogComposite, "Destination");
    _defaultLocationCheckbox = new Button(destinationGroup, SWT.CHECK);
    _defaultLocationCheckbox.setText("Default location (directly in your workspace)");
    _defaultLocationCheckbox.setSelection(_defaultLocation);
    _defaultLocationCheckbox.addListener(SWT.Selection, new Listener() {
      @Override
      public void handleEvent(Event event) {
        _defaultLocation = _defaultLocationCheckbox.getSelection();
        updateEnablement();
      }
    });

    Composite inner = new Composite(destinationGroup, SWT.NONE);
    GridLayout layout = new GridLayout();
    layout.numColumns = 2;
    layout.marginWidth = 0;
    inner.setLayout(layout);
    GridData data = new GridData(SWT.FILL, SWT.FILL, true, false);
    inner.setLayoutData(data);

    _locationCombo = createDropDownCombo(inner);
    _location = "";
    _locationCombo.setItems(PdePluginProjectExporterConfigurationStore.getInstance().getHistory());
    // _locationCombo.setText(_externalFolder);
    _locationCombo.addListener(SWT.Modify, new Listener() {
      @Override
      public void handleEvent(Event event) {
        if (!_defaultLocation) {
          String newLocation = _locationCombo.getText();
          if (!newLocation.equals(_location)) {
            _location = newLocation;

            updateEnablement();
          }
        }
      }
    });

    _browseLocationButton = new Button(inner, SWT.PUSH);
    _browseLocationButton.setText("Browse...");
    data = new GridData();
    data.horizontalAlignment = GridData.FILL;
    int widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
    data.widthHint = Math.max(widthHint, _browseLocationButton.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
    _browseLocationButton.setLayoutData(data);
    _browseLocationButton.addListener(SWT.Selection, new Listener() {
      @Override
      public void handleEvent(Event event) {

        DirectoryDialog dialog = new DirectoryDialog(getShell(), SWT.MULTI);
        String folder = dialog.open();
        if (folder != null) {
          _locationCombo.setText(folder);
          _location = folder;
          updateEnablement();
        }
      }
    });

    Group settingsGroup = createGroup(dialogComposite, "Settings");
    
    Composite dependencyStyleGroup = createComposite(settingsGroup, 2);
    Label label = new Label(dependencyStyleGroup, SWT.NONE);
    label.setText("Use Import-Package for Dependencies:");
    
    _dependencyStyleCombo = createReadOnlyDropDownCombo(dependencyStyleGroup);
    final ComboViewer comboViewer = new ComboViewer(_dependencyStyleCombo);
    comboViewer.setContentProvider(ArrayContentProvider.getInstance());
    comboViewer.setLabelProvider(new LabelProvider() {

      @Override
      public String getText(Object element) {
        DependencyStyle dependencyStyle = (DependencyStyle)element;
        
        switch (dependencyStyle) {
        case PREFER_IMPORT_PACKAGE:
          return "Preferred";
        case STRICT_IMPORT_PACKAGE:
          return "Always";
        case STRICT_REQUIRE_BUNDLE:
        return "Never";
        default:
          return super.getText(element);
        }
        }
        
    });
    comboViewer.setInput(DependencyStyle.values());
    _dependencyStyle = PdePluginProjectExporterConfigurationStore.getInstance().getDependencyStyle();
    comboViewer.setSelection(new StructuredSelection(_dependencyStyle));
    comboViewer.addSelectionChangedListener(new ISelectionChangedListener() {

      @Override
      public void selectionChanged(SelectionChangedEvent event) {
        IStructuredSelection s = (IStructuredSelection) comboViewer.getSelection();
        if (s.isEmpty()) {
          // ???
          return;
        }

        _dependencyStyle = (DependencyStyle) s.getFirstElement();

      }
    });
    
    _useOptionalResolutionOnMissingImportsCheckBox = new Button(settingsGroup, SWT.CHECK);
    _useOptionalResolutionOnMissingImportsCheckBox.setText("Use Optional Resolution on missing Packages");
    _useOptionalResolutionOnMissingImports = PdePluginProjectExporterConfigurationStore.getInstance()
        .getOptionalResolutionOnMissingType();
    _useOptionalResolutionOnMissingImportsCheckBox.setSelection(_useOptionalResolutionOnMissingImports);
    _useOptionalResolutionOnMissingImportsCheckBox.addListener(SWT.Selection, new Listener() {
      @Override
      public void handleEvent(Event event) {
        _useOptionalResolutionOnMissingImports = _useOptionalResolutionOnMissingImportsCheckBox.getSelection();
      }
    });
    

    _useClassificationCheckbox = new Button(settingsGroup, SWT.CHECK);
    _useClassificationCheckbox.setText("Use classification in output path");
    _useClassificationCheckbox.setSelection(_useClassification);
    _useClassificationCheckbox.addListener(SWT.Selection, new Listener() {
      @Override
      public void handleEvent(Event event) {
        _useClassification = _useClassificationCheckbox.getSelection();
      }
    });

    
    
    updateEnablement();
  }

  private void updateEnablement() {
    _locationCombo.setEnabled(_defaultLocation == false);
    _browseLocationButton.setEnabled(_defaultLocation == false);
    if (_defaultLocation) {
      _locationCombo.setText(getDefaultLocation().toString());
    } else {
      _locationCombo.setText(_location);
      if (_location == null || _location.isEmpty()) {
        setErrorMessage("Please enter output destination");
        return;
      }
    }

    setErrorMessage(null);
  }

  private IPath getDefaultLocation() {
    return ResourcesPlugin.getWorkspace().getRoot().getRawLocation();
  }

  /**
   * @return
   */
  public File getDestination() {
    if (_defaultLocation) {
      return getDefaultLocation().toFile();
    }

    // remember selection
    PdePluginProjectExporterConfigurationStore.getInstance().remember(_location);
    return new File(_location);
  }

  public DependencyStyle getDependencyStyle() {

    PdePluginProjectExporterConfigurationStore.getInstance().rememberDependencyStyle(_dependencyStyle);

    return this._dependencyStyle;
  }

  public boolean isUseClassificationInOutputPath() {
    return this._useClassification;
  }

  /**
   * @return the useOptionalResolutionOnMissingImports
   */
  public boolean isUseOptionalResolutionOnMissingImports() {

    PdePluginProjectExporterConfigurationStore.getInstance().rememberOptionalResolutionOnMissingType(
        _useOptionalResolutionOnMissingImports);

    return _useOptionalResolutionOnMissingImports;
  }

  static class PdePluginProjectExporterConfigurationStore extends ConfigurationStore {

    private static PdePluginProjectExporterConfigurationStore instance;

    public static PdePluginProjectExporterConfigurationStore getInstance() {
      if (instance == null) {
        instance = new PdePluginProjectExporterConfigurationStore();
      }
      return instance;
    }

    @Override
    protected String getPreviousTag() {
      return "previousFolders";
    }

    @Override
    protected String getListTag() {
      return "folders";
    }

    public void rememberDependencyStyle(DependencyStyle style) {
      put("dependencyStyle", style.name());
    }

    public DependencyStyle getDependencyStyle() {
      String value = get("dependencyStyle", DependencyStyle.PREFER_IMPORT_PACKAGE.name());
      return DependencyStyle.valueOf(value);
    }

    public void rememberOptionalResolutionOnMissingType(boolean value) {
      put("optionOnMissingTypes", String.valueOf(value));
    }

    public boolean getOptionalResolutionOnMissingType() {
      String value = get("optionOnMissingTypes", Boolean.TRUE.toString());
      return Boolean.valueOf(value);
    }

    @Override
    protected String getStoreSection() {
      return "PdePluginProjectExporterConfigurationDialog";
    }

  }

}
