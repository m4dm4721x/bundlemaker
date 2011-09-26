package org.bundlemaker.core.modules.modifiable;

import org.bundlemaker.core.modules.ITypeContainer;

/**
 * <p>
 * </p>
 * 
 * @author Nils Hartmann (nils@nilshartmann.net)
 * 
 */
public interface IModifiableTypeContainer extends ITypeContainer {

  // /**
  // * <p>
  // * </p>
  // *
  // * @param type
  // */
  // @Deprecated
  // void add(IType type);
  //
  // /**
  // * <p>
  // * </p>
  // *
  // * @param type
  // */
  // @Deprecated
  // void remove(IType type);

  /**
   * <p>
   * </p>
   * 
   * @param movableUnit
   */
  void addMovableUnit(IMovableUnit movableUnit);

  /**
   * <p>
   * </p>
   * 
   * @param movableUnit
   */
  void removeMovableUnit(IMovableUnit movableUnit);
}
