/*******************************************************************************
 * Copyright (c) 2013 Bundlemaker project team.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Bundlemaker project team - initial API and implementation
 ******************************************************************************/

package org.bundlemaker.core.ui.editor.dependencyviewer.graph;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import org.bundlemaker.core.analysis.IBundleMakerArtifact;
import org.bundlemaker.core.analysis.IDependency;
import org.bundlemaker.core.analysis.IRootArtifact;
import org.bundlemaker.core.selection.Selection;
import org.bundlemaker.core.selection.stage.ArtifactStageChangedEvent;
import org.bundlemaker.core.selection.stage.IArtifactStage;
import org.bundlemaker.core.selection.stage.IArtifactStageChangeListener;
import org.bundlemaker.core.ui.artifact.ArtifactImages;
import org.bundlemaker.core.ui.editor.dependencyviewer.DependencyViewerEditor;
import org.bundlemaker.core.ui.view.ArtifactStageActionHelper;
import org.bundlemaker.core.ui.view.dependencytable.ArtifactPathLabelGenerator;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

import com.mxgraph.layout.mxCircleLayout;
import com.mxgraph.layout.mxIGraphLayout;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.mxGraphOutline;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxEventObject;
import com.mxgraph.util.mxEventSource.mxIEventListener;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxGraphView;
import com.mxgraph.view.mxStylesheet;

/**
 * @author Nils Hartmann (nils@nilshartmann.net)
 * 
 */
public class DependencyViewerGraph {

  private final static String                     BUNDLEMAKER_VERTEX_STYLE        = "BUNDLEMAKER_VERTEX";

  private final static String                     BUNDLEMAKER_EDGE_STYLE          = "BUNDLEMAKER_EDGE";

  private final static String                     BUNDLEMAKER_CIRCULAR_EDGE_STYLE = "BUNDLEMAKER_CIRCULAR_EDGE";

  private final Map<IBundleMakerArtifact, Object> _vertexCache                    = new Hashtable<IBundleMakerArtifact, Object>();

  private final EdgeCache                         _edgeCache                      = new EdgeCache();

  private mxGraphComponent                        _graphComponent;

  private mxGraph                                 _graph;

  private mxIGraphLayout                          _graphLayout;

  private Display                                 _display;

  private UnstageAction                           _unstageAction;

  protected boolean                               _autoFit                        = true;

  private ArtifactPathLabelGenerator              _labelGenerator                 = new ArtifactPathLabelGenerator();

  /**
   * Should the graph be re-layouted after artifacts have been added or removed?
   */
  private boolean                                 _doLayoutAfterArtifactsChange   = true;

  public void create(Frame parentFrame, Display display) {

    _display = display;
    _graph = createGraph();

    registerStyles();

    // Layout
    _graphLayout = new mxCircleLayout(_graph);

    _graphComponent = new mxGraphComponent(_graph);
    _graphComponent.setConnectable(false);
    _graphComponent.setToolTips(true);
    _graphComponent.getViewport().setOpaque(true);
    _graphComponent.getViewport().setBackground(Color.WHITE);
    _graphComponent.setTripleBuffered(true);
    _graphComponent.addMouseWheelListener(new ZoomMouseWheelListener());
    if (_autoFit) {
      _graphComponent.addComponentListener(new InitialComponentResizeListener());
    }

    // Populate the frame
    parentFrame.setLayout(new BorderLayout());
    parentFrame.add(createToolBar(), BorderLayout.NORTH);
    parentFrame.add(_graphComponent, BorderLayout.CENTER);
  }

  protected JPanel createToolBar() {
    JPanel comboBoxPanel = new JPanel();

    // Layout Selector
    Vector<GraphLayout> layouts = Layouts.createLayouts(_graphComponent);
    final JComboBox comboBox = new JComboBox(layouts);
    comboBox.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent event) {
        GraphLayout newLayout = (GraphLayout) comboBox.getSelectedItem();
        _graphLayout = newLayout.getLayout();
        DependencyViewerGraph.this.layoutGraph();
      }
    });
    comboBoxPanel.add(comboBox);

    // Zoom Action
    comboBoxPanel.add(new JButton(new ZoomAction("-", "Zoom out (Ctrl+Mouse Wheel)")));
    comboBoxPanel.add(new JButton(new ZoomAction("0", "Reset zoom")));
    comboBoxPanel.add(new JButton(new ZoomAction("+", "Zoom in (Ctrl+Mouse Wheel)")));
    comboBoxPanel.add(new JButton(new ZoomAction("Fit", "Zoom to fit (horizontal)")));
    JCheckBox jCheckBox = new JCheckBox(new AutoFitAction());
    jCheckBox.setSelected(_autoFit);
    comboBoxPanel.add(jCheckBox);
    // UnstageButton
    _unstageAction = new UnstageAction();
    comboBoxPanel.add(new JButton(_unstageAction));

    return comboBoxPanel;

  }

  public void dispose() {
    if (_unstageAction != null) {
      _unstageAction.dispose();
    }
  }

  protected mxGraph createGraph() {
    mxGraph graph = new mxGraph() {

      @Override
      public String getToolTipForCell(Object cell) {
        Object cellValue = model.getValue(cell);
        if (cellValue instanceof IBundleMakerArtifact) {
          IBundleMakerArtifact artifact = (IBundleMakerArtifact) cellValue;
          return _labelGenerator.getLabel(artifact);
          // return ((IBundleMakerArtifact) cellValue).getQualifiedName();
        }
        if (cellValue instanceof IDependency) {
          IDependency dependency = (IDependency) cellValue;

          String string = "<html>" + dependency.getFrom().getName() + " -> " + dependency.getTo() + ": "
              + dependency.getWeight();

          IDependency dependencyTo = dependency.getTo().getDependencyTo(dependency.getFrom());
          if (dependencyTo != null) {
            string += "<br/>" + dependencyTo.getFrom().getName() + " -> " + dependencyTo.getTo() + ": "
                + dependencyTo.getWeight();
          }

          return string + "</html>";
        }
        return super.getToolTipForCell(cell);
      }

      @Override
      public String convertValueToString(Object cell) {
        Object result = model.getValue(cell);

        if (result instanceof IBundleMakerArtifact) {
          return ((IBundleMakerArtifact) result).getName();
        }

        return super.convertValueToString(cell);
      }

    };

    // listener for cell selection changes
    graph.getSelectionModel().addListener(mxEvent.CHANGE, new mxIEventListener() {

      @Override
      public void invoke(Object sender, mxEventObject evt) {
        cellSelectionChanged();
      }
    });

    // Configure Graph
    graph.setCellsDisconnectable(false);
    graph.setConnectableEdges(false);
    graph.setCellsBendable(false);
    graph.setCellsEditable(false);
    graph.setCellsResizable(false);
    graph.setDropEnabled(false);

    return graph;

  }

  protected void registerStyles() {
    // Styles
    mxStylesheet stylesheet = _graph.getStylesheet();

    // Base style for an Artifact
    Hashtable<String, Object> style = new Hashtable<String, Object>();
    style.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_LABEL);
    style.put(mxConstants.STYLE_IMAGE_ALIGN, mxConstants.ALIGN_LEFT);
    style.put(mxConstants.STYLE_OPACITY, 50);
    style.put(mxConstants.STYLE_FONTCOLOR, "#000000");
    style.put(mxConstants.STYLE_FILLCOLOR, "#FFEAB2");
    style.put(mxConstants.STYLE_STROKECOLOR, "#C37D64");
    style.put(mxConstants.STYLE_STROKEWIDTH, "1");
    style.put(mxConstants.STYLE_FONTSIZE, "12");
    stylesheet.putCellStyle("BUNDLEMAKER_VERTEX", style);

    // base style for a uni-directional dependency
    style = new Hashtable<String, Object>();
    style.put(mxConstants.STYLE_FONTCOLOR, "#000000");
    style.put(mxConstants.STYLE_STROKECOLOR, "#000000"); // #FFEAB2");
    style.put(mxConstants.STYLE_STROKEWIDTH, "1");
    style.put(mxConstants.STYLE_NOLABEL, "1");
    stylesheet.putCellStyle("BUNDLEMAKER_EDGE", style);

    // base style for a circular dependency
    style = new Hashtable<String, Object>();
    style.put(mxConstants.STYLE_FONTCOLOR, "#000000");
    style.put(mxConstants.STYLE_STROKECOLOR, "#B85E3D");
    style.put(mxConstants.STYLE_STROKEWIDTH, "1");
    style.put(mxConstants.STYLE_FILLCOLOR, "#B85E3D");
    style.put(mxConstants.STYLE_NOLABEL, "1");
    style.put(mxConstants.STYLE_ENDARROW, mxConstants.NONE);
    style.put(mxConstants.STYLE_STARTARROW, mxConstants.NONE);
    stylesheet.putCellStyle("BUNDLEMAKER_CIRCULAR_EDGE", style);

  }

  /**
   * @param effectiveSelectedArtifacts
   */
  public void showArtifacts(List<IBundleMakerArtifact> effectiveSelectedArtifacts) {
    Object parent = _graph.getDefaultParent();

    mxIGraphModel model = _graph.getModel();

    if (effectiveSelectedArtifacts.size() > 0) {
      IBundleMakerArtifact anArtifact = effectiveSelectedArtifacts.get(0);
      IRootArtifact root = anArtifact.getRoot();
      _labelGenerator.setBaseArtifact(root);
    }

    model.beginUpdate();

    try {
      Iterator<Entry<IBundleMakerArtifact, Object>> iterator = _vertexCache.entrySet().iterator();

      while (iterator.hasNext()) {
        Entry<IBundleMakerArtifact, Object> entry = iterator.next();
        IBundleMakerArtifact artifact = entry.getKey();
        if (!effectiveSelectedArtifacts.contains(artifact)) {
          // Artifact is not longer part of selection => remove it...

          // ..from model
          model.remove(entry.getValue());

          // ..from vertex cache
          iterator.remove();

          // all conntected edges
          _edgeCache.removeEdgesConnectedTo(artifact);

        }
      }

      for (IBundleMakerArtifact iBundleMakerArtifact : effectiveSelectedArtifacts) {
        if (!_vertexCache.containsKey(iBundleMakerArtifact)) {

          String style = BUNDLEMAKER_VERTEX_STYLE;
          ArtifactImages image = ArtifactImages.forArtifact(iBundleMakerArtifact);
          style += ";image=" + image.getImageUrl();
          Rectangle bounds = image.getImage().getBounds();
          style += ";imageWidth=" + bounds.width;
          style += ";imageWidth=" + bounds.height;
          style += ";imageVerticalAlign=center;fontStyle=1;verticalAlign=top;spacingLeft=" + (bounds.width + 15)
              + ";spacingTop=2;imageAlign=left;align=top;spacingRight=5"; // +

          Object vertex = _graph.insertVertex(parent, null, iBundleMakerArtifact, 10, 10, 10, 10, style);
          _graph.updateCellSize(vertex);
          _vertexCache.put(iBundleMakerArtifact, vertex);
        }
      }

      for (IBundleMakerArtifact from : effectiveSelectedArtifacts) {
        Collection<IDependency> dependenciesTo = from.getDependenciesTo(effectiveSelectedArtifacts);
        Object fromVertex = _vertexCache.get(from);
        for (IDependency iDependency : dependenciesTo) {
          IBundleMakerArtifact to = iDependency.getTo();

          if (from.equals(to)) {
            continue;
          }

          Object edge = _edgeCache.getEdge(from, to);
          if (edge == null) {
            String style = null;

            if (to.getDependencyTo(from) == null) {
              style = BUNDLEMAKER_EDGE_STYLE;
            } else {
              style = BUNDLEMAKER_CIRCULAR_EDGE_STYLE;
            }

            Object toVertex = _vertexCache.get(to);
            edge = _graph.insertEdge(parent, null, iDependency, fromVertex, toVertex, style);
            _edgeCache.addEdge(edge);
          }
        }
      }

      if (_doLayoutAfterArtifactsChange) {
        layoutGraph();
      }

    } finally {
      model.endUpdate();
    }

    if (_autoFit) {
      zoomToFitHorizontal();
    }

  }

  /**
   * Handles changes of the cell (edges or vertex) selection
   */
  protected void cellSelectionChanged() {

    Object[] cells = _graph.getSelectionCells();

    final List<IBundleMakerArtifact> selectedArtifacts = new LinkedList<IBundleMakerArtifact>();
    final Set<IDependency> selectedDependencies = new LinkedHashSet<IDependency>();

    // collect selected artifacts and dependencies
    for (Object cell : cells) {

      Object value = _graph.getModel().getValue(cell);

      if (value instanceof IDependency) {
        IDependency dependency = (IDependency) value;
        selectedDependencies.add(dependency);
        IDependency dependencyTo = dependency.getTo().getDependencyTo(dependency.getFrom());
        if (dependencyTo != null) {
          selectedDependencies.add(dependencyTo);
        }
      } else if (value instanceof IBundleMakerArtifact) {
        IBundleMakerArtifact bundleMakerArtifact = (IBundleMakerArtifact) value;
        selectedArtifacts.add(bundleMakerArtifact);
        selectedDependencies.addAll(bundleMakerArtifact.getDependenciesFrom());
        selectedDependencies.addAll(bundleMakerArtifact.getDependenciesTo());
      }
    }

    // propagate selected dependencies
    runInSwt(new Runnable() {

      @Override
      public void run() {
        Selection
            .instance()
            .getDependencySelectionService()
            .setSelection(Selection.MAIN_DEPENDENCY_SELECTION_ID, DependencyViewerEditor.DEPENDENCY_VIEWER_EDITOR_ID,
                selectedDependencies);
      }
    });

    //
    _unstageAction.setUnstageCandidates(selectedArtifacts);

  }

  /**
   * (Re-)layouts the whole graph.
   * 
   * <p>
   * This methods uses the currently selected layout. It executes the layout regardless of the current
   * {@link #_doLayoutAfterArtifactsChange} setting
   */
  protected void layoutGraph() {
    _graph.getModel().beginUpdate();
    try {
      _graphLayout.execute(_graph.getDefaultParent());
    } finally {
      _graph.getModel().endUpdate();
    }
  }

  /**
   * @return the {@link IArtifactStage}
   */
  protected IArtifactStage getArtifactStage() {
    return Selection.instance().getArtifactStage();
  }

  /**
   * 
   */
  public void zoomToFitHorizontal() {

    mxGraphView view = _graph.getView();
    int compLen = _graphComponent.getWidth();
    int viewLen = (int) view.getGraphBounds().getWidth();

    if (compLen == 0 || viewLen == 0) {
      return;
    }

    double scale = (double) compLen / viewLen * view.getScale();

    System.out.println("compLen: " + compLen + ", viewLen: " + viewLen + ", scale: " + scale);

    if (scale > 1) {
      _graphComponent.zoomActual();
    } else {
      view.setScale(scale);
    }

  }

  /**
   * Runs the specified {@link Runnable} on the SWT Thread
   */
  protected void runInSwt(final Runnable runnable) {
    _display.asyncExec(runnable);
  }

  class UnstageAction extends AbstractAction implements Runnable, IArtifactStageChangeListener {

    private static final long          serialVersionUID = 1L;

    private List<IBundleMakerArtifact> _unstageCandidates;

    public UnstageAction() {
      super("Unstage");

      // getArtifactStage().addArtifactStageChangeListener(this);

      refreshEnablement();
    }

    /**
     * @param selectedArtifacts
     */
    public void setUnstageCandidates(List<IBundleMakerArtifact> selectedArtifacts) {
      _unstageCandidates = selectedArtifacts;

      refreshEnablement();
    }

    public void dispose() {
      // getArtifactStage().removeArtifactStageChangeListener(this);
      _unstageCandidates = null;
    }

    protected void refreshEnablement() {
      // if (getArtifactStage().getAddMode().isAutoAddMode()) {
      // setEnabled(false);
      // return;
      // }

      setEnabled(_unstageCandidates != null && _unstageCandidates.size() > 0);
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {

      // Changes in Stage are processed in SWT, so make sure, we're on the SWT thread
      runInSwt(this);
    }

    @Override
    public void run() {
      try {
        if (!ArtifactStageActionHelper.switchToManualAddModeIfRequired()) {
          return;
        }
        DependencyViewerGraph.this._doLayoutAfterArtifactsChange = false;

        getArtifactStage().removeStagedArtifacts(_unstageCandidates);
      } finally {
        DependencyViewerGraph.this._doLayoutAfterArtifactsChange = true;

      }
    }

    @Override
    public void artifactStateChanged(ArtifactStageChangedEvent event) {
      refreshEnablement();
    }
  }

  class AutoFitAction extends AbstractAction {
    private static final long serialVersionUID = 1L;

    public AutoFitAction() {
      super("Auto Fit");
      putValue(Action.SHORT_DESCRIPTION, "Auto Fit horizontal when content change");
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent e) {
      JCheckBox box = (JCheckBox) e.getSource();
      _autoFit = box.isSelected();
    }
  }

  class ZoomAction extends AbstractAction {
    private static final long serialVersionUID = 1L;

    public ZoomAction(String text, String tooltipText) {
      super(text);
      putValue(Action.SHORT_DESCRIPTION, tooltipText);
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {

      String name = (String) getValue(Action.NAME);

      if ("+".equals(name)) {
        _graphComponent.zoomIn();
      } else if ("-".equals(name)) {
        _graphComponent.zoomOut();
      } else if ("fit".equalsIgnoreCase(name)) {
        zoomToFitHorizontal();
      } else {
        _graphComponent.zoomActual();
      }

    }

  }

  /**
   * Zoom in/out using the mouse wheel while pressing the ctrl-key
   */
  class ZoomMouseWheelListener implements MouseWheelListener {
    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
      if (e.getSource() instanceof mxGraphOutline || e.isControlDown()) {
        if (e.getWheelRotation() < 0) {
          _graphComponent.zoomIn();
        } else {
          _graphComponent.zoomOut();
        }

      }
    }
  };

  class EdgeCache {

    private final List<Object> _edges = new LinkedList<Object>();

    public void removeEdgesConnectedTo(IBundleMakerArtifact artifact) {
      Iterator<Object> iterator = _edges.iterator();
      mxIGraphModel model = _graph.getModel();

      while (iterator.hasNext()) {
        Object cell = iterator.next();

        IDependency dependency = (IDependency) model.getValue(cell);
        if (artifact.equals(dependency.getTo()) || artifact.equals(dependency.getFrom())) {
          model.remove(cell);
          iterator.remove();
        }
      }
    }

    /**
     * @param edgeFromTo
     */
    public void addEdge(Object edgeFromTo) {
      _edges.add(edgeFromTo);
    }

    /**
     * @param dependencyOne
     * @param dependencyTwo
     */
    public Object getEdge(IBundleMakerArtifact dependencyOne, IBundleMakerArtifact dependencyTwo) {
      mxIGraphModel model = _graph.getModel();

      for (Object edge : _edges) {
        IDependency dependency = (IDependency) model.getValue(edge);

        if (dependencyOne.equals(dependency.getFrom()) && dependencyTwo.equals(dependency.getTo())) {
          return edge;
        }

        if (dependencyTwo.equals(dependency.getFrom()) && dependencyOne.equals(dependency.getTo())) {
          return edge;
        }
      }

      return null;
    }
  }

  class InitialComponentResizeListener implements ComponentListener {

    @Override
    public void componentHidden(ComponentEvent e) {
    }

    @Override
    public void componentMoved(ComponentEvent e) {
    }

    @Override
    public void componentResized(ComponentEvent e) {
      e.getComponent().removeComponentListener(this);
      if (_autoFit) {
        zoomToFitHorizontal();
      }
    }

    @Override
    public void componentShown(ComponentEvent e) {

    }

  }
}
