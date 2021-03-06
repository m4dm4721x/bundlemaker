package org.bundlemaker.core.jtype;

import org.bundlemaker.core.analysis.IBundleMakerArtifact;
import org.bundlemaker.core.analysis.IResourceArtifact;
import org.bundlemaker.core.analysis.IResourceArtifact.IResourceArtifactContent;
import org.bundlemaker.core.resource.IModuleAwareMovableUnit;

/**
 * <p>
 * Defines an {@link IBundleMakerArtifact} that represented an {@link IType} instance.
 * </p>
 * 
 * @author Nils Hartmann (nils@nilshartmann.net)
 * @author Gerd W&uuml;therich (gerd@gerd-wuetherich.de)
 */
public interface ITypeArtifact extends IBundleMakerArtifact, IModuleAwareMovableUnit, IResourceArtifact.IResourceArtifactContent {

  /**
   * <p>
   * Returns the {@link IType} that is represented by this {@link ITypeArtifact}, never {@code null}.
   * </p>
   * 
   * @return the {@link IType} that is represented by this {@link ITypeArtifact}, never {@code null}.
   */
  IType getAssociatedType();

  /**
   * <p>
   * </p>
   * 
   * @return the fully qualified (Java) type name, e.g. com.example.MyClass, never null
   */
  String getQualifiedTypeName();

  /**
   * <p>
   * Returns the simple type name, e.g. {@code MyClass}, never {@code null}.
   * </p>
   * 
   * @return the simple type name, e.g. {@code MyClass}, never {@code null}.
   */
  String getName();

  /**
   * <p>
   * Returns the fully qualified (Java) type name, e.g. {@code com.example.MyClass}, never {@code null}.
   * </p>
   * 
   * @return the fully qualified (Java) type name, e.g. {@code com.example.MyClass}, never {@code null}.
   */
  String getQualifiedName();
}
