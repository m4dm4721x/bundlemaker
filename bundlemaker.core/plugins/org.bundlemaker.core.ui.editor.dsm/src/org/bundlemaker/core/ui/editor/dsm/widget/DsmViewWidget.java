package org.bundlemaker.core.ui.editor.dsm.widget;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Observable;
import java.util.Observer;

import org.bundlemaker.core.ui.editor.dsm.ArtifactLabelProvider;
import org.bundlemaker.core.ui.editor.dsm.DsmViewModel;
import org.bundlemaker.core.ui.editor.dsm.IDsmViewModel;
import org.bundlemaker.core.ui.editor.dsm.widget.internal.matrix.IMatrixListener;
import org.bundlemaker.core.ui.editor.dsm.widget.internal.matrix.Matrix;
import org.bundlemaker.core.ui.editor.dsm.widget.internal.matrix.MatrixEvent;
import org.bundlemaker.core.ui.editor.dsm.widget.internal.sidemarker.HorizontalSideMarker;
import org.bundlemaker.core.ui.editor.dsm.widget.internal.sidemarker.VerticalSideMarker;
import org.bundlemaker.core.ui.editor.dsm.widget.internal.util.BoxSize;
import org.bundlemaker.core.ui.editor.dsm.widget.internal.util.DependencyLabelProvider;
import org.bundlemaker.core.ui.editor.dsm.widget.internal.util.DsmUtils;
import org.bundlemaker.core.ui.editor.dsm.widget.internal.zoom.ZoomableScrollPane;
import org.eclipse.core.runtime.Assert;
import org.eclipse.draw2d.Cursors;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FigureUtilities;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.LayoutListener;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.MouseMotionListener;
import org.eclipse.draw2d.ScrollPane;
import org.eclipse.draw2d.Viewport;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

/**
 * <p>
 * </p>
 * 
 * @author Gerd W&uuml;therich (gerd@gerd-wuetherich.de)
 */
public class DsmViewWidget extends Canvas implements Observer {

  @Deprecated
  float                        _zoom                   = 1.0f;

  /** the {@link DsmViewModel} */
  private IDsmViewModel        _model;

  /** the main figure */
  private Figure               _mainFigure;

  private ZoomableScrollPane   _zoomableScrollpane;

  private ZoomableScrollPane   _zoomableScrollpaneVerticalBar;

  private ZoomableScrollPane   _zoomableScrollpaneHorizontalBar;

  /** - */
  private Matrix               _matrixFigure;

  private VerticalSideMarker   _verticalListFigure;

  private HorizontalSideMarker _horizontalListFigure;

  private int                  _horizontalFigureHeight = 8;

  private int                  _verticalFigureWidth    = -1;

  private int                  _x;

  private int                  _y;

  private boolean              _drawToolTip            = false;

  private ILabelProvider       _artifactLabelProvider;

  private ILabelProvider       _dependencyLabelProvider;

  private IDsmColorScheme      _colorScheme;

  /**
   * <p>
   * Creates a new instance of type {@link DsmViewWidget}.
   * </p>
   * 
   * @param model
   * @param canvas
   */
  public DsmViewWidget(IDsmViewModel model, ILabelProvider artifactLabelProvider,
      ILabelProvider dependencyLabelProvider, Composite parent) {
    super(parent, SWT.NO_REDRAW_RESIZE);

    // assert not null
    Assert.isNotNull(model);
    Assert.isNotNull(parent);
    Assert.isNotNull(artifactLabelProvider);
    Assert.isNotNull(dependencyLabelProvider);

    // set model and canvas
    this._model = model;

    //
    _artifactLabelProvider = artifactLabelProvider;
    _dependencyLabelProvider = dependencyLabelProvider;

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

    LightweightSystem lws = new LightweightSystem(this);

    //
    DsmViewWidgetMouseMotionListener motionListener = new DsmViewWidgetMouseMotionListener(this);

    //
    this.addMouseWheelListener(new MouseWheelListener() {
      @Override
      public void mouseScrolled(org.eclipse.swt.events.MouseEvent e) {
        if (e.count > 0) {
          DsmViewWidget.this.setZoom(getZoom() * 1.05f);
        } else if (e.count < 0) {
          DsmViewWidget.this.setZoom(getZoom() * 0.95f);
        }
      }
    });

    //
    // this.addMouseMoveListener(new MyMouseMoveListener(this));

    _mainFigure = new Figure() {

      @Override
      public void paint(Graphics graphics) {
        super.paint(graphics);

        if (_drawToolTip && _x >= _verticalFigureWidth && _y >= _horizontalFigureHeight) {
          graphics.fillRectangle(_x, _y, 100, 100);
          graphics.drawRectangle(_x, _y, 100, 100);
        }
      }
    };

    _mainFigure.setLayoutManager(new XYLayout());
    _mainFigure.addMouseMotionListener(motionListener);
    lws.setContents(_mainFigure);

    _colorScheme = new DefaultMatrixColorScheme();

    _matrixFigure = new Matrix(_model, _dependencyLabelProvider, _model, _colorScheme);
    _matrixFigure.addMouseMotionListener(motionListener);
    _matrixFigure.addMouseListener(motionListener);

    _zoomableScrollpane = new ZoomableScrollPane(_matrixFigure, ScrollPane.ALWAYS, ScrollPane.ALWAYS);

    _verticalListFigure = new VerticalSideMarker(_model, _model, _artifactLabelProvider, _colorScheme);
    _verticalListFigure.addMouseMotionListener(motionListener);
    _zoomableScrollpaneVerticalBar = new ZoomableScrollPane(_verticalListFigure, ScrollPane.NEVER, ScrollPane.NEVER);

    _horizontalListFigure = new HorizontalSideMarker(_model, _model, _artifactLabelProvider, _colorScheme);
    _horizontalListFigure.addMouseMotionListener(motionListener);
    _zoomableScrollpaneHorizontalBar = new ZoomableScrollPane(_horizontalListFigure, ScrollPane.NEVER, ScrollPane.NEVER);

    _matrixFigure.addMouseMotionListener(new MouseMotionListener.Stub() {

      /**
       * {@inheritDoc}
       */
      @Override
      public void mouseExited(org.eclipse.draw2d.MouseEvent me) {
        _drawToolTip = false;
      }
    });

    _matrixFigure.addMatrixListener(new IMatrixListener() {

      @Override
      public void toolTip(MatrixEvent event) {
        _drawToolTip = true;
        _mainFigure.repaint();
      }

      @Override
      public void singleClick(MatrixEvent event) {
        _drawToolTip = false;
        _mainFigure.repaint();
      }

      @Override
      public void doubleClick(MatrixEvent event) {
        _drawToolTip = false;
        _mainFigure.repaint();
      }

      @Override
      public void marked(MatrixEvent event) {
        _mainFigure.repaint();
        _horizontalListFigure.mark(event.getX());
        _verticalListFigure.mark(event.getY());
      }
    });

    // _zoomScrollBar = new ScrollBar();
    // final Label zoomLabel = new Label("Zoom");
    // zoomLabel.setBorder(new SchemeBorder(ButtonBorder.SCHEMES.BUTTON_SCROLLBAR));
    // _zoomScrollBar.setThumb(zoomLabel);
    // _zoomScrollBar.setHorizontal(true);
    // _zoomScrollBar.setMaximum(200);
    // _zoomScrollBar.setMinimum(0);
    // _zoomScrollBar.setExtent(25);
    // _zoomScrollBar.addPropertyChangeListener("value", new PropertyChangeListener() {
    // @Override
    // public void propertyChange(PropertyChangeEvent evt) {
    // float z = (_zoomScrollBar.getValue() + 10) * 0.02f;
    // _zoomableScrollpane.setZoom(z);
    // _zoomableScrollpaneVerticalBar.setZoom(z);
    // _zoomableScrollpaneHorizontalBar.setZoom(z);
    // _zoom = z;
    // }
    // });
    //
    // _useShortendLabelsCheckBox = new CheckBox("Shorten labels");
    // _useShortendLabelsCheckBox.getModel().addChangeListener(new ChangeListener() {
    //
    // @Override
    // public void handleStateChanged(ChangeEvent event) {
    // if ("selected".equals(event.getPropertyName())) {
    // _model.setUseShortendLabels(_useShortendLabelsCheckBox.isSelected());
    // _mainFigure.revalidate();
    // _mainFigure.repaint();
    // }
    // }
    // });

    _zoomableScrollpane.getViewport().addPropertyChangeListener(new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        Viewport viewport = (Viewport) evt.getSource();
        _zoomableScrollpaneVerticalBar.getViewport().setViewLocation(0, viewport.getViewLocation().y);
        _zoomableScrollpaneHorizontalBar.getViewport().setViewLocation(viewport.getViewLocation().x, 0);
        // _zoomableScrollpaneHorizontalBar.getViewport().setViewLocation(0, 0);
        _zoomableScrollpane.getViewport().setViewLocation(viewport.getViewLocation().x, viewport.getViewLocation().y);

        _mainFigure.revalidate();
        _mainFigure.repaint();
      }
    });

    // _mainFigure.add(_zoomScrollBar);
    // _mainFigure.add(_useShortendLabelsCheckBox);
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

  public void setZoom(float value) {
    // float z = (value + 10) * 0.02f;
    _zoomableScrollpane.setZoom(value);
    _zoomableScrollpaneVerticalBar.setZoom(value);
    _zoomableScrollpaneHorizontalBar.setZoom(value);
    _zoom = value;
    //
    // _mainFigure.repaint();
  }

  /**
   * <p>
   * </p>
   * 
   * @return the zoom
   */
  public float getZoom() {
    return _zoom;
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
    int testExtend = FigureUtilities.getTextWidth(DsmUtils.getLongestString(_model.getDisplayLabels()),
        matrixFigure.getFont());
    return (testExtend + 10/* * zoomableScrollpane.getZoom() */);
  }

  private void layoutF(IFigure figure) {

    //
    BoxSize boxSize = new BoxSize();
    boxSize.setHorizontalBoxSize(computeSize());

    //
    _matrixFigure.setBoxSize(boxSize);
    _horizontalListFigure.setBoxSize(boxSize);
    _verticalListFigure.setBoxSize(boxSize);

    // adjust size
    _matrixFigure.resetSize();
    _horizontalListFigure.resetSize();
    _verticalListFigure.resetSize();

    int horizontalBarHeight = (int) (_horizontalFigureHeight * _zoomableScrollpaneHorizontalBar.getZoom());
    if (_verticalFigureWidth == -1) {
      _verticalFigureWidth = getTextExtend(_matrixFigure, _zoomableScrollpane);
    }
    int verticalBarWidth = (int) (_verticalFigureWidth * _zoomableScrollpaneVerticalBar.getZoom());

    //
    _zoomableScrollpane.setLocation(new Point(verticalBarWidth, horizontalBarHeight));
    _zoomableScrollpane.setSize(_mainFigure.getSize().width - verticalBarWidth,
        (_mainFigure.getSize().height - (horizontalBarHeight)));

    // HACK
    int verticalOffset = 18;
    _zoomableScrollpaneVerticalBar.setLocation(new Point(0, (/* HORIZONTAL_OFFSET + */horizontalBarHeight)));
    _zoomableScrollpaneVerticalBar.setSize(verticalBarWidth,
        (_mainFigure.getSize().height - (horizontalBarHeight + verticalOffset)));

    //
    _zoomableScrollpaneHorizontalBar.setLocation(new Point(verticalBarWidth, 0));
    _zoomableScrollpaneHorizontalBar.setSize((_mainFigure.getSize().width - (verticalBarWidth + 18)),
        horizontalBarHeight);
  }

  private int computeSize() {
    String value = DsmUtils.getLongestString(_model.getValues());
    return FigureUtilities.getTextWidth(value, _matrixFigure.getFont()) + 6;
  }

  public void setModel(DsmViewModel model) {
    _model = model;

    // TODO: HACK!!
    ((ArtifactLabelProvider) _artifactLabelProvider).setModel(model);

    _matrixFigure.setModel(model);
    _verticalListFigure.setModel(model);
    _horizontalListFigure.setModel(model);

    _verticalFigureWidth = getTextExtend(_matrixFigure, _zoomableScrollpane);

    _mainFigure.revalidate();
    _mainFigure.repaint();

    // _zoomScrollBar.setValue(40);
    // _useShortendLabelsCheckBox.setSelected(_model.isUseShortendLabels());
  }

  /**
   * <p>
   * </p>
   * 
   * @return the model
   */
  public IDsmViewModel getModel() {
    return _model;
  }

  public void addMatrixListener(IMatrixListener listener) {
    _matrixFigure.addMatrixListener(listener);
  }

  public void removeMatrixLIstener(IMatrixListener listener) {
    _matrixFigure.removeMatrixLIstener(listener);
  }

  final class DsmViewWidgetMouseMotionListener extends MouseMotionListener.Stub implements MouseListener {

    /** - */
    private final DsmViewWidget _dsmViewWidget;

    /**
     * <p>
     * Creates a new instance of type {@link DsmViewWidgetMouseMotionListener}.
     * </p>
     * 
     * @param dsmViewWidget
     */
    public DsmViewWidgetMouseMotionListener(DsmViewWidget dsmViewWidget) {
      _dsmViewWidget = dsmViewWidget;
    }

    /** - */
    private static final int HORIZONTAL   = 1;

    /** - */
    private static final int VERTICAL     = 2;

    /** - */
    private static final int DIAGONAL     = 3;

    /** - */
    private static final int RANGE        = 5;

    /** - */
    private int              _currentDrag = -1;

    /**
     * {@inheritDoc}
     */
    @Override
    public void mousePressed(MouseEvent me) {
      // //
      // if (me.getSource().equals(_dsmViewWidget._matrixFigure)) {
      // ((Figure) me.getSource()).setCursor(Cursors.HAND);
      // }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mouseReleased(MouseEvent me) {
      // if (me.getSource().equals(_dsmViewWidget._matrixFigure)) {
      // ((Figure) me.getSource()).setCursor(Cursors.ARROW);
      // }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mouseDoubleClicked(MouseEvent me) {
    }

    @Override
    public void mouseMoved(MouseEvent me) {
      handle(me, false);
    }

    @Override
    public void mouseDragged(MouseEvent me) {
      handle(me, true);
    }

    /**
     * <p>
     * </p>
     * 
     * @param me
     * @param isDragged
     */
    private void handle(MouseEvent me, boolean isDragged) {

      // if (!isDragged) {
      // _currentDrag = -1;
      // }

      //
      if (me.getSource() instanceof Figure && (me.getState() & SWT.BUTTON1) == 0) {
        _currentDrag = isInRange(me);
        switch (_currentDrag) {
        case HORIZONTAL:
          ((Figure) me.getSource()).setCursor(Cursors.SIZENS);
          break;
        case VERTICAL:
          ((Figure) me.getSource()).setCursor(Cursors.SIZEWE);
          break;
        case DIAGONAL:
          ((Figure) me.getSource()).setCursor(Cursors.SIZENWSE);
          break;
        default:
          ((Figure) me.getSource()).setCursor(Cursors.ARROW);
          break;
        }
      }

      //
      if ((me.getState() & SWT.BUTTON1) != 0 && isDragged) {

        //
        // if (_currentDrag == -1) {
        // _currentDrag = isInRange(me);
        // }

        if ((me.getState() & SWT.SHIFT) != 0) {

          //
          if (me.getSource().equals(_dsmViewWidget._matrixFigure)) {
            float newZoom = me.getLocation().x / (float) _dsmViewWidget._verticalFigureWidth;
          }
          //
          else if (me.getSource().equals(_dsmViewWidget._mainFigure)) {
            float newZoom = me.getLocation().x / (float) _dsmViewWidget._verticalFigureWidth;

            _dsmViewWidget.setZoom(newZoom);
          }

        } else {

          //
          if ((_currentDrag == HORIZONTAL || _currentDrag == DIAGONAL)
              && me.getSource().equals(_dsmViewWidget._matrixFigure)) {
            _dsmViewWidget._horizontalFigureHeight = _dsmViewWidget._horizontalFigureHeight + me.getLocation().y;
          }
          //
          else if ((_currentDrag == HORIZONTAL || _currentDrag == DIAGONAL)
              && me.getSource().equals(_dsmViewWidget._horizontalListFigure)) {
            _dsmViewWidget._horizontalFigureHeight = me.getLocation().y;
          }
          //
          else if ((_currentDrag == HORIZONTAL || _currentDrag == DIAGONAL)
              && me.getSource().equals(_dsmViewWidget._verticalListFigure)) {
            _dsmViewWidget._horizontalFigureHeight = _dsmViewWidget._horizontalFigureHeight + me.getLocation().y;
          }
          //
          else if ((_currentDrag == HORIZONTAL || _currentDrag == DIAGONAL)
              && me.getSource().equals(_dsmViewWidget._mainFigure)) {
            _dsmViewWidget._horizontalFigureHeight = (int) ((me.getLocation().y / _dsmViewWidget._zoom));
          }
          //
          if ((_currentDrag == VERTICAL || _currentDrag == DIAGONAL)
              && me.getSource().equals(_dsmViewWidget._matrixFigure)) {
            _dsmViewWidget._verticalFigureWidth = _dsmViewWidget._verticalFigureWidth + me.getLocation().x;
          }
          //
          else if ((_currentDrag == VERTICAL || _currentDrag == DIAGONAL)
              && me.getSource().equals(_dsmViewWidget._verticalListFigure)) {
            _dsmViewWidget._verticalFigureWidth = me.getLocation().x;
          }
          //
          else if ((_currentDrag == VERTICAL || _currentDrag == DIAGONAL)
              && me.getSource().equals(_dsmViewWidget._horizontalListFigure)) {
            _dsmViewWidget._verticalFigureWidth = _dsmViewWidget._verticalFigureWidth + me.getLocation().x;
          }
          //
          else if ((_currentDrag == VERTICAL || _currentDrag == DIAGONAL)
              && me.getSource().equals(_dsmViewWidget._mainFigure)) {
            _dsmViewWidget._verticalFigureWidth = (int) (me.getLocation().x / _dsmViewWidget._zoom);
          }
        }
        _dsmViewWidget._mainFigure.revalidate();
      }
    }

    /**
     * <p>
     * </p>
     * 
     * @param me
     * @return
     */
    private int isInRange(MouseEvent me) {

      //
      if (me.getSource().equals(_dsmViewWidget._matrixFigure)) {

        if (Math.abs(me.getLocation().x) < (RANGE * _dsmViewWidget._zoom)
            && Math.abs(me.getLocation().y) < (RANGE * _dsmViewWidget._zoom)) {
          return DIAGONAL;
        }

        if (Math.abs(me.getLocation().x) < (RANGE * _dsmViewWidget._zoom)) {
          return VERTICAL;
        }

        if (Math.abs(me.getLocation().y) < (RANGE * _dsmViewWidget._zoom)) {
          return HORIZONTAL;
        }
      }

      else if (me.getSource().equals(_dsmViewWidget._horizontalListFigure)) {

        if (Math.abs(me.getLocation().x) < (RANGE * _dsmViewWidget._zoom)
            && _dsmViewWidget._horizontalListFigure.getSize().height - Math.abs(me.getLocation().y) < (RANGE * _dsmViewWidget._zoom)) {
          return DIAGONAL;
        }

        if (Math.abs(me.getLocation().x) < (RANGE * _dsmViewWidget._zoom)) {
          return VERTICAL;
        }

        if (_dsmViewWidget._horizontalListFigure.getSize().height - Math.abs(me.getLocation().y) < (RANGE * _dsmViewWidget._zoom)) {
          return HORIZONTAL;
        }
      }

      else if (me.getSource().equals(_dsmViewWidget._verticalListFigure)) {

        if (Math.abs(me.getLocation().y) < (RANGE * _dsmViewWidget._zoom)
            && _dsmViewWidget._verticalListFigure.getSize().width - Math.abs(me.getLocation().x) < (RANGE * _dsmViewWidget._zoom)) {
          return HORIZONTAL;
        }

        if (Math.abs(me.getLocation().y) < (RANGE * _dsmViewWidget._zoom)) {
          return HORIZONTAL;
        }

        if (_dsmViewWidget._verticalListFigure.getSize().width - Math.abs(me.getLocation().x) < (RANGE * _dsmViewWidget._zoom)) {
          return VERTICAL;
        }
      }

      //
      return -1;
    }
  }
}
