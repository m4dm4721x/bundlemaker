package org.bundlemaker.core.analysis.spi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

import org.bundlemaker.core.analysis.DependencyKind;
import org.bundlemaker.core.analysis.IBundleMakerArtifact;
import org.bundlemaker.core.analysis.IDependency;
import org.eclipse.core.runtime.Assert;

/**
 * <p>
 * </p>
 */
public class Dependency implements IDependency {

  /** - */
  private IBundleMakerArtifact    _to;

  /** - */
  private IBundleMakerArtifact    _from;

  private DependencyKind          dependencyKind = DependencyKind.USES;

  private Collection<IDependency> dependencies;

  /** - */
  private boolean                 _isCoreDependency;

  /**
   * <p>
   * Creates a new instance of type {@link Dependency}.
   * </p>
   * 
   * @param from
   * @param to
   * @param isCoreDependency
   */
  public Dependency(IBundleMakerArtifact from, IBundleMakerArtifact to, boolean isCoreDependency) {

    Assert.isNotNull(from);
    Assert.isNotNull(to);

    //
    _from = from;
    _to = to;

    //
    _isCoreDependency = isCoreDependency;
  }

  /**
   * <p>
   * </p>
   * 
   * @param dependencyKind
   */
  public void setDependencyKind(DependencyKind dependencyKind) {
    this.dependencyKind = dependencyKind;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public DependencyKind getDependencyKind() {
    return dependencyKind;
  }

  /**
   * Fuegt der Abhaengigkeit eine andere Abhaengigkeit hinzu. Diese Methode wird verwendet, um aggregierte
   * Abhaengigkeiten zu erstellen
   * 
   * @param dependency
   *          Abhaengigkeit, die hinzugefuegt werden soll
   */
  public void addDependency(IDependency dependency) {
    if (dependencies == null) {
      dependencies = new ArrayList<IDependency>();
    }
    dependencies.add(dependency);
  }

  /**
   * @return the weight
   */
  @Override
  public int getWeight() {

    //
    if (dependencies != null && !dependencies.isEmpty()) {
      return _isCoreDependency ? dependencies.size() + 1 : dependencies.size();
    } else {
      return _isCoreDependency ? 1 : 0;
    }
  }

  /**
   * @return the from
   */
  @Override
  public IBundleMakerArtifact getFrom() {
    return _from;
  }

  @Override
  public IBundleMakerArtifact getTo() {
    return _to;
  }

  @Override
  public String toString() {
    StringBuffer sb = new StringBuffer();

    sb.append("Dependency( ");
    sb.append(this.getFrom().getQualifiedName());
    sb.append(" ");
    sb.append(dependencyKind);
    sb.append(" ");
    sb.append(this.getTo().getQualifiedName());
    sb.append(" )");

    return sb.toString();
  }

  @Override
  /**
   * Gibt eine <code>Collection</code> von Abhaengigkeiten zurueck, falls die
   * Abhaengigkeit aus aggegrierten Abhaengigkeiten besteht.
   * 
   * @return <code>Collection</code> von Abhaengigkeiten
   * 
   */
  public Collection<IDependency> getDependencies() {
    return dependencies;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Collection<IDependency> getCoreDependencies() {
    Collection<IDependency> result = new LinkedList<IDependency>();
    getLeafDependencies(result);
    return result;
  }

  public void getLeafDependencies(Collection<IDependency> leafDependencies) {

    //
    if (_isCoreDependency) {
      leafDependencies.add(this);
    }

    //
    else {
      if (dependencies != null) {
        for (IDependency dependency : dependencies) {
          ((Dependency) dependency).getLeafDependencies(leafDependencies);
        }
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_from == null) ? 0 : _from.hashCode());
    result = prime * result + ((_to == null) ? 0 : _to.hashCode());
    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    Dependency other = (Dependency) obj;
    if (_from == null) {
      if (other._from != null) {
        return false;
      }
    } else if (!_from.equals(other._from)) {
      return false;
    }
    if (_to == null) {
      if (other._to != null) {
        return false;
      }
    } else if (!_to.equals(other._to)) {
      return false;
    }
    return true;
  }
}