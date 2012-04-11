package org.bundlemaker.core.ui.editor.dsm.figures;

import org.eclipse.swt.graphics.Color;

public interface IMatrixColorScheme {

  Color getSideMarkerBackgroundColor();

  Color getSideMarkerEvenColor();

  Color getSideMarkerMarkedColor();

  Color getSideMarkerSeparatorColor();

  Color getSideMarkerTextColor();

  Color getMatrixSeparatorColor();

  Color getMatrixBackgroundColor();

  Color getMatrixTextColor();

  Color getMatrixDiagonalColor();

  Color getMatrixMarkedColumnRowColor();

  Color getMatrixMarkedCellColor();

  Color getCycleSideMarkerColor();

  Color getCycleMatrixMarkedColumnRowColor();

  Color getCycleSideMarkerSeparatorColor();

  Color getCycleMatrixMarkedCellColor();

  Color getCycleMatrixDiagonalColor();

  Color getCycleSideMarkerMarkedColor();
}
