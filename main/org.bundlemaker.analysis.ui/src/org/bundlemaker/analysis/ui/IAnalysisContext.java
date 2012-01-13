package org.bundlemaker.analysis.ui;

import java.beans.PropertyChangeListener;

import org.bundlemaker.analysis.model.IDependencyModel;
import org.bundlemaker.analysis.model.dependencies.IDependencyGraph;

/**
 * Repraesentiert den aktuellen Zustand der Auswahl, Modelle etc in der Workbench
 * 
 */
public interface IAnalysisContext {

  /**
   * Name des Properties, unter dem �nderungen am DependencyGraph notifiziert werden.
   * 
   * <p>
   * Dieses Property wird auch verwendet, wenn der Graph insgesamt ausgetauscht wird (sich also nicht nur ein Property
   * des Graphen �ndert)
   */
  public final static String GRAPH_CHANGED_PROPERTY_NAME = "dependencyGraph";

  public IDependencyModel getDependencyModel();

  public void setDependencyModel(IDependencyModel dependencyModel);

  public IDependencyGraph getDependencyGraph();

  public void setDependencyGraph(IDependencyGraph dependencyGraph);

  public void addPropertyChangeListener(PropertyChangeListener listener);

  void removePropertyChangeListener(PropertyChangeListener listener);

  /**
   * Notify listeners that the current {@link IDependencyModel} has been changed.
   * 
   * <p>
   * Note:
   * </p>
   * this method will be removed when IDependencyModel get support for listeners itself.
   */
  void dependencyModelChanged();

}
