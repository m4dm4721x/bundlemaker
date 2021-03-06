package org.bundlemaker.core.ui.editor.dsm.widget;

import org.eclipse.core.runtime.Assert;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.graphics.Color;

/**
 * <p>
 * </p>
 * 
 * @author Gerd W&uuml;therich (gerd@gerd-wuetherich.de)
 */
public class DefaultMatrixColorScheme implements IDsmColorScheme {

  // ##### Color Palette by Color Scheme Designer
  // ##### Palette URL: http://colorschemedesigner.com/#1611ThWs0g0g0
  // ##### Generated by Color Scheme Designer (c) Petr Stanicek 2002-2010
  // var. 1 = #FFDE8A = rgb(255,222,138)
  // var. 2 = #DEC585 = rgb(222,197,133)
  // var. 3 = #D1AE54 = rgb(209,174,84)
  // var. 4 = #FFEAB2 = rgb(255,234,178)
  // var. 5 = #FFF3D3 = rgb(255,243,211)

  // ##### Color Palette by Color Scheme Designer
  // ##### Palette URL: http://colorschemedesigner.com/#0911ThWs0g0g0
  // ##### Color Space: RGB;
  // var. 1 = #DF8462 = rgb(223,132,98)
  // var. 2 = #C37D64 = rgb(195,125,100)
  // var. 3 = #B85E3D = rgb(184,94,61)
  // var. 4 = #E79476 = rgb(231,148,118)
  // var. 5 = #E79E83 = rgb(231,158,131)

  // private final Color VAR_1_MEDIUM = getColor("FFDE8A");

  private final Color VAR_2_DARK_MEDIUM        = getColor("DEC585");

  private final Color VAR_3_DARK               = getColor("D1AE54");

  private final Color VAR_4_LIGHT_MEDIUM       = getColor("FFEAB2");

  private final Color VAR_5_LIGHT              = getColor("FFF3D3");

  private final Color CYCLE_VAR_2_DARK_MEDIUM  = getColor("C37D64");

  private final Color CYCLE_VAR_3_DARK         = getColor("B85E3D");

  private final Color CYCLE_VAR_4_LIGHT_MEDIUM = getColor("E79476");

  private final Color CYCLE_VAR_5_LIGHT        = getColor("E79E83");

  @Override
  public Color getSideMarkerBackgroundColor() {
    return VAR_5_LIGHT;
  }

  @Override
  public Color getCycleSideMarkerColor() {
    return CYCLE_VAR_5_LIGHT;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Color getCycleSideMarkerSeparatorColor() {
    return CYCLE_VAR_2_DARK_MEDIUM;
  }

  @Override
  public Color getCycleMatrixDiagonalColor() {
    return CYCLE_VAR_4_LIGHT_MEDIUM;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Color getCycleMatrixMarkedCellColor() {
    return CYCLE_VAR_3_DARK;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Color getCycleMatrixMarkedColumnRowColor() {
    return CYCLE_VAR_2_DARK_MEDIUM;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Color getCycleSideMarkerMarkedColor() {
    return CYCLE_VAR_2_DARK_MEDIUM;
  }

  @Override
  public Color getSideMarkerEvenColor() {
    return VAR_5_LIGHT;
  }

  @Override
  public Color getSideMarkerMarkedColor() {
    return VAR_2_DARK_MEDIUM;
  }

  @Override
  public Color getSideMarkerSeparatorColor() {
    return VAR_2_DARK_MEDIUM;
  }

  @Override
  public Color getSideMarkerTextColor() {
    return ColorConstants.black;
  }

  @Override
  public Color getMatrixSeparatorColor() {
    return VAR_3_DARK;
  }

  @Override
  public Color getMatrixBackgroundColor() {
    return VAR_5_LIGHT;
  }

  @Override
  public Color getMatrixTextColor() {
    return ColorConstants.black;
  }

  @Override
  public Color getMatrixDiagonalColor() {
    return VAR_4_LIGHT_MEDIUM;
  }

  @Override
  public Color getMatrixMarkedColumnRowColor() {
    return VAR_2_DARK_MEDIUM;
  }

  @Override
  public Color getMatrixMarkedCellColor() {
    return VAR_3_DARK;
  }

  /**
   * <p>
   * </p>
   * 
   * @param rgbString
   * @return
   */
  protected Color getColor(String rgbString) {

    Assert.isNotNull(rgbString);
    Assert.isTrue(rgbString.length() == 6);

    int r = Integer.parseInt(rgbString.substring(0, 2), 16);
    int g = Integer.parseInt(rgbString.substring(2, 4), 16);
    int b = Integer.parseInt(rgbString.substring(4, 6), 16);

    return new Color(null, r, g, b);
  }

}
