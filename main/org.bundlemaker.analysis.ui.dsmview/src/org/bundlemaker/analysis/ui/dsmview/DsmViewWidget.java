package org.bundlemaker.analysis.ui.dsmview;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Observable;
import java.util.Observer;

import org.bundlemaker.analysis.ui.Analysis;
import org.bundlemaker.analysis.ui.dsmview.figures.HorizontalSideMarker;
import org.bundlemaker.analysis.ui.dsmview.figures.IMatrixListener;
import org.bundlemaker.analysis.ui.dsmview.figures.Matrix;
import org.bundlemaker.analysis.ui.dsmview.figures.MatrixEvent;
import org.bundlemaker.analysis.ui.dsmview.figures.VerticalSideMarker;
import org.bundlemaker.analysis.ui.dsmview.figures.ZoomableScrollPane;
import org.eclipse.core.runtime.Assert;
import org.eclipse.draw2d.ButtonBorder;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FigureUtilities;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LayoutListener;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.draw2d.SchemeBorder;
import org.eclipse.draw2d.ScrollBar;
import org.eclipse.draw2d.ScrollPane;
import org.eclipse.draw2d.Viewport;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

/**
 * <p>
 * </p>
 * 
 * @author Gerd W&uuml;therich (gerd@gerd-wuetherich.de)
 */
public class DsmViewWidget implements Observer {

  /** the SWT canvas */
  private Canvas               _canvas;

  /** the {@link DsmViewModel} */
  private DsmViewModel         _model;

  /** the main figure */
  private Figure               _mainFigure;

  private ZoomableScrollPane   _zoomableScrollpane;

  private ZoomableScrollPane   _zoomableScrollpaneVerticalBar;

  private ZoomableScrollPane   _zoomableScrollpaneHorizontalBar;

  private ScrollBar            _zoomScrollBar;

  /** - */
  private Matrix               _matrixFigure;

  private VerticalSideMarker   _verticalListFigure;

  private HorizontalSideMarker _horizontalListFigure;

  /**
   * <p>
   * Creates a new instance of type {@link DsmViewWidget}.
   * </p>
   * 
   * @param model
   * @param canvas
   */
  public DsmViewWidget(DsmViewModel model, Composite parent) {

    // assert not null
    Assert.isNotNull(model);
    Assert.isNotNull(parent);

    // set model and canvas
    this._model = model;
    this._canvas = new Canvas(parent, SWT.NO_REDRAW_RESIZE);

    // set this view as an observer
    this._model.addObserver(this);

    // init
    init();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void update(Observable o, Object arg) {
    Display.getDefault().asyncExec(new Runnable() {
      @Override
      public void run() {
        _mainFigure.repaint();
      }
    });
  }

  /**
   * <p>
   * Initializes the {@link DsmViewWidget}.
   * </p>
   */
  private void init() {

    LightweightSystem lws = new LightweightSystem(_canvas);
    _mainFigure = new Figure();
    _mainFigure.setLayoutManager(new XYLayout());
    lws.setContents(_mainFigure);

    _matrixFigure = new Matrix(_model);

    _zoomableScrollpane = new ZoomableScrollPane(_matrixFigure, ScrollPane.ALWAYS, ScrollPane.ALWAYS);

    _verticalListFigure = new VerticalSideMarker(_model);
    _zoomableScrollpaneVerticalBar = new ZoomableScrollPane(_verticalListFigure, ScrollPane.NEVER, ScrollPane.NEVER);

    _horizontalListFigure = new HorizontalSideMarker(_model);
    _zoomableScrollpaneHorizontalBar = new ZoomableScrollPane(_horizontalListFigure, ScrollPane.NEVER, ScrollPane.NEVER);

    _matrixFigure.addMatrixListener(new IMatrixListener() {

      @Override
      public void toolTip(MatrixEvent event) {
        drawToolTip(event.getX(), event.getY());
      }

      @Override
      public void singleClick(MatrixEvent event) {
        Analysis.instance().getDependencySelectionService()
            .setSelection(DSMView.ID, _model.getDependency(event.getX(), event.getY()));
      }

      @Override
      public void doubleClick(MatrixEvent event) {
        // do nothing
      }

      @Override
      public void marked(MatrixEvent event) {
        _horizontalListFigure.mark(event.getX());
        _verticalListFigure.mark(event.getY());
      }
    });

    _zoomScrollBar = new ScrollBar();
    final Label zoomLabel = new Label("�Zoom�");
    zoomLabel.setBorder(new SchemeBorder(ButtonBorder.SCHEMES.BUTTON_SCROLLBAR));
    _zoomScrollBar.setThumb(zoomLabel);
    _zoomScrollBar.setHorizontal(true);
    _zoomScrollBar.setMaximum(200);
    _zoomScrollBar.setMinimum(0);
    _zoomScrollBar.setExtent(25);
    _zoomScrollBar.addPropertyChangeListener("value", new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        float z = (_zoomScrollBar.getValue() + 10) * 0.02f;
        _zoomableScrollpane.setZoom(z);
        _zoomableScrollpaneVerticalBar.setZoom(z);
        _zoomableScrollpaneHorizontalBar.setZoom(z);
      }
    });

    _zoomableScrollpane.getViewport().addPropertyChangeListener(new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        Viewport viewport = (Viewport) evt.getSource();

        _zoomableScrollpaneVerticalBar.getViewport().setViewLocation(0, viewport.getViewLocation().y);

        _zoomableScrollpaneHorizontalBar.getViewport().setViewLocation(viewport.getViewLocation().x, 0);

        _zoomableScrollpane.getViewport().setViewLocation(viewport.getViewLocation().x, viewport.getViewLocation().y);

        _mainFigure.revalidate();
        _mainFigure.repaint();
      }
    });

    _mainFigure.add(_zoomScrollBar);
    _mainFigure.add(_zoomableScrollpane);
    _mainFigure.add(_zoomableScrollpaneVerticalBar);
    _mainFigure.add(_zoomableScrollpaneHorizontalBar);

    _mainFigure.addLayoutListener(new LayoutListener.Stub() {

      @Override
      public boolean layout(IFigure container) {
        layoutF(container);
        return true;
      }
    });
  }

  protected void drawToolTip(int x, int y) {
    // System.out.println("drawToolTip");
  }

  /**
   * <p>
   * Compute the text extend.
   * </p>
   * 
   * @param matrixFigure
   * @param zoomableScrollpane
   * @return
   */
  private int getTextExtend(final Matrix matrixFigure, final ZoomableScrollPane zoomableScrollpane) {

    //
    int testExtend = FigureUtilities.getTextWidth(getLongestString(_model.getLabels()), _matrixFigure.getFont()) + 25;
    return (int) (testExtend * zoomableScrollpane.getZoom());
  }

  /**
   * <p>
   * Helper method that returns the longest string from the string array.
   * </p>
   * 
   * @param strings
   *          the string array
   * @return the longest string from the string array.
   */
  private String getLongestString(String[] strings) {

    // create the result
    String result = null;

    // iterate over all strings
    for (String string : strings) {
      if (result == null) {
        result = string;
      } else if (result.length() < string.length()) {
        result = string;
      }
    }

    // return the result
    return result;
  }

  private void layoutF(IFigure figure) {

    //
    _model.getConfiguration().setHorizontalBoxSize(computeSize());

    // adjust size
    _matrixFigure.resetSize();
    _horizontalListFigure.resetSize();
    _verticalListFigure.resetSize();

    // fix sized
    _zoomScrollBar.setLocation(new Point(0.0, 0.0));
    _zoomScrollBar.setSize(_mainFigure.getSize().width, 20);

    //
    int textExtend = getTextExtend(_matrixFigure, _zoomableScrollpane);

    //
    _zoomableScrollpane.setLocation(new Point(textExtend, 21.0 + textExtend));
    _zoomableScrollpane.setSize(_mainFigure.getSize().width - textExtend,
        (int) (_mainFigure.getSize().height - (21.0 + textExtend)));

    //
    _zoomableScrollpaneVerticalBar.setLocation(new Point(0, (21.0 + textExtend)));
    _zoomableScrollpaneVerticalBar.setSize(textExtend, (_mainFigure.getSize().height - (21 + textExtend + 17)));

    //
    _zoomableScrollpaneHorizontalBar.setLocation(new Point(textExtend, 21.0));
    _zoomableScrollpaneHorizontalBar.setSize((_mainFigure.getSize().width - (textExtend + 17)), textExtend);
  }

  private int computeSize() {

    //
    String value = getLongestString(_model.getValues());
    return FigureUtilities.getTextWidth(value, _matrixFigure.getFont()) + 6;
  }

  private String getLongestString(String[][] values) {

    // create the result
    String result = "";

    // iterate over all strings
    for (String[] value : values) {
      for (String string : value) {

        if (string != null) {

          if (result == null) {
            result = string;
          } else if (result.length() < string.length()) {
            result = string;
          }
        }
      }
    }

    // return the result
    return result;
  }

  public void setModel(DsmViewModel model) {
    _model = model;

    _matrixFigure.setModel(model);
    _verticalListFigure.setModel(model);
    _horizontalListFigure.setModel(model);

    _mainFigure.revalidate();
    _mainFigure.repaint();

    _zoomScrollBar.setValue(40);
  }

  public void addMatrixListener(IMatrixListener listener) {
    _matrixFigure.addMatrixListener(listener);
  }

  public void removeMatrixLIstener(IMatrixListener listener) {
    _matrixFigure.removeMatrixLIstener(listener);
  }
}