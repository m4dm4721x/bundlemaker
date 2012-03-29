package org.bundlemaker.core.ui.artifact.tree;


import org.bundlemaker.analysis.model.ArtifactType;
import org.bundlemaker.core.analysis.IBundleMakerArtifact;
import org.bundlemaker.core.analysis.ITypeArtifact;
import org.bundlemaker.core.modules.IModularizedSystem;
import org.bundlemaker.core.ui.artifact.ArtifactImages;
import org.bundlemaker.core.ui.artifact.internal.Activator;
import org.eclipse.swt.graphics.Image;


/**
 * <p>
 * Adds support for {@link IModularizedSystem} and {@link ITypeArtifact} instances to the
 * {@link DefaultArtifactLabelProvider}
 * </p>
 * 
 * @author Gerd W&uuml;therich (gerd@gerd-wuetherich.de)
 */
public class ArtifactTreeLabelProvider extends DefaultArtifactLabelProvider {

  /**
   * {@inheritDoc}
   */
  @Override
  public Image getImage(Object obj) {

    if (obj instanceof String || obj instanceof IModularizedSystem) {
      return Activator.getDefault().getIcon("navigator/root.gif");
    }

    // All other types are handled by the superclass
    return super.getImage(obj);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getText(Object obj) {

    if (obj instanceof String) {
      return (String) obj;
    } else if (obj instanceof IModularizedSystem) {
      return ((IModularizedSystem) obj).getName();
    } else if (obj instanceof IBundleMakerArtifact && ((IBundleMakerArtifact) obj).getType().equals(ArtifactType.Package)
        && isHierarchicalPackageLayout()) {
      return ((IBundleMakerArtifact) obj).getName();
    }

    // All other cases are handled by the superclass

    return super.getText(obj);
  }

  @Override
  protected Image getImageForTypeArtifact(IBundleMakerArtifact artifact) {
    if (artifact instanceof ITypeArtifact) {
      ITypeArtifact typeHolder = (ITypeArtifact) artifact;

      if (typeHolder.getAssociatedType() != null) {
        switch (typeHolder.getAssociatedType().getType()) {
        case CLASS: {
          return ArtifactImages.CLASS_TYPE_ARTIFACT_ICON.getImage();
        }
        case INTERFACE: {
          return ArtifactImages.INTERFACE_TYPE_ARTIFACT_ICON.getImage();
        }
        case ENUM: {
          return ArtifactImages.ENUM_TYPE_ARTIFACT_ICON.getImage();
        }
        case ANNOTATION: {
          return ArtifactImages.ANNOTATION_TYPE_ARTIFACT_ICON.getImage();
        }
        default:
          break;
        }
      } else {
        return ArtifactImages.CLASS_TYPE_ARTIFACT_ICON.getImage();
      }
    }

    // default: let superclass determine image
    return super.getImageForTypeArtifact(artifact);
  }

  /**
   * <p>
   * </p>
   * 
   * @return
   */
  private boolean isHierarchicalPackageLayout() {
    return Activator.getDefault().getArtifactModelConfigurationProvider().getArtifactModelConfiguration()
        .isHierarchicalPackages();
  }
}