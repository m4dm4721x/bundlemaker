package org.bundlemaker.core.project;

import java.util.List;

/**
 * <p>
 * A movable unit defines a list of types together with a list of (binary and source) resources that must be moved as
 * <i>one</i> unit. Normally this is necessary because they all are associated with the same source file.
 * </p>
 * 
 * @author Gerd W&uuml;therich (gerd@gerd-wuetherich.de)
 */
public interface IMovableUnit {

  /**
   * <p>
   * </p>
   * 
   * @return
   */
  boolean hasAssociatedBinaryResources();

  /**
   * <p>
   * Returns the list of binary resources that are associated with this movable unit.
   * </p>
   * 
   * @return the list of binary resources that are associated with this movable unit.
   */
  List<? extends IProjectContentResource> getAssociatedBinaryResources();

  /**
   * <p>
   * </p>
   * 
   * @return
   */
  boolean hasAssociatedSourceResource();

  /**
   * <p>
   * Returns the source resource that is associated with this movable type.
   * </p>
   * 
   * @return the source resource that is associated with this movable type.
   */
  IProjectContentResource getAssociatedSourceResource();
}
