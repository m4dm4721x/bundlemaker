/*
 * generated by Xtext
 */
package org.bundlemaker.core.transformations.dsl.ui.contentassist;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.bundlemaker.analysis.model.ArtifactType;
import org.bundlemaker.analysis.model.IArtifact;
import org.bundlemaker.analysis.model.IDependencyModel;
import org.bundlemaker.core.BundleMakerCore;
import org.bundlemaker.core.BundleMakerProjectState;
import org.bundlemaker.core.IBundleMakerProject;
import org.bundlemaker.core.analysis.ArtifactModelConfiguration;
import org.bundlemaker.core.projectdescription.IProjectContentEntry;
import org.bundlemaker.core.transformations.dsl.transformationDsl.ModuleIdentifier;
import org.bundlemaker.core.transformations.dsl.transformationDsl.ResourceList;
import org.bundlemaker.core.transformations.dsl.transformationDsl.ResourceSet;
import org.bundlemaker.core.transformations.dsl.ui.contentassist.AbstractTransformationDslProposalProvider;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.xtext.Assignment;
import org.eclipse.xtext.ui.editor.contentassist.ContentAssistContext;
import org.eclipse.xtext.ui.editor.contentassist.ICompletionProposalAcceptor;

/**
 * see http://www.eclipse.org/Xtext/documentation/latest/xtext.html#contentAssist on how to customize content assistant
 */
public class TransformationDslProposalProvider extends AbstractTransformationDslProposalProvider {

  /*
   * (non-Javadoc)
   * 
   * @see org.bundlemaker.core.transformations.dsl.ui.contentassist.AbstractTransformationDslProposalProvider#
   * completeResourceSet_IncludeResources(org.eclipse.emf.ecore.EObject, org.eclipse.xtext.Assignment,
   * org.eclipse.xtext.ui.editor.contentassist.ContentAssistContext,
   * org.eclipse.xtext.ui.editor.contentassist.ICompletionProposalAcceptor)
   */
  @Override
  public void completeResourceSet_IncludeResources(EObject model, Assignment assignment, ContentAssistContext context,
      ICompletionProposalAcceptor acceptor) {
    super.completeResourceSet_IncludeResources(model, assignment, context, acceptor);

    ResourceSet resourceSet = (ResourceSet) model;
    addPackageProposals(resourceSet, context, acceptor);

  }

  @Override
  public void completeResourceSet_ExcludeResources(EObject model, Assignment assignment, ContentAssistContext context,
      ICompletionProposalAcceptor acceptor) {
    super.completeResourceSet_ExcludeResources(model, assignment, context, acceptor);

    ResourceSet resourceSet = (ResourceSet) model;
    addPackageProposals(resourceSet, context, acceptor);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.bundlemaker.core.transformations.dsl.ui.contentassist.AbstractTransformationDslProposalProvider#
   * completeModuleIdentifier_Modulename(org.eclipse.emf.ecore.EObject, org.eclipse.xtext.Assignment,
   * org.eclipse.xtext.ui.editor.contentassist.ContentAssistContext,
   * org.eclipse.xtext.ui.editor.contentassist.ICompletionProposalAcceptor)
   */
  @Override
  public void completeModuleIdentifier_Modulename(EObject model, Assignment assignment, ContentAssistContext context,
      ICompletionProposalAcceptor acceptor) {
    super.completeModuleIdentifier_Modulename(model, assignment, context, acceptor);
    List<? extends IProjectContentEntry> fileBasedContent = getFileBasedContent(model);
    for (IProjectContentEntry iFileBasedContent : fileBasedContent) {
      if (iFileBasedContent.isAnalyze()) {
        String moduleName = getValueConverter().toString(iFileBasedContent.getName(), "MODULEID");
        // Create completion proposal
        ICompletionProposal completionProposal = createCompletionProposal(moduleName, context);

        // register the proposal, the acceptor handles null-values gracefully
        acceptor.accept(completionProposal);

      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.bundlemaker.core.transformations.dsl.ui.contentassist.AbstractTransformationDslProposalProvider#
   * completeModuleIdentifier_Version(org.eclipse.emf.ecore.EObject, org.eclipse.xtext.Assignment,
   * org.eclipse.xtext.ui.editor.contentassist.ContentAssistContext,
   * org.eclipse.xtext.ui.editor.contentassist.ICompletionProposalAcceptor)
   */
  @Override
  public void completeModuleIdentifier_Version(EObject model, Assignment assignment, ContentAssistContext context,
      ICompletionProposalAcceptor acceptor) {
    super.completeModuleIdentifier_Version(model, assignment, context, acceptor);
    ModuleIdentifier moduleIdentifier = (ModuleIdentifier) model;
    List<? extends IProjectContentEntry> fileBasedContent = getFileBasedContent(model);
    for (IProjectContentEntry iFileBasedContent : fileBasedContent) {
      if (iFileBasedContent.isAnalyze()) {
        if (moduleIdentifier.getModulename() == null
            || moduleIdentifier.getModulename().equals(iFileBasedContent.getName())) {

          String version = getValueConverter().toString(iFileBasedContent.getVersion(), "STRING");
          // Create completion proposal
          ICompletionProposal completionProposal = createCompletionProposal(version, context);

          // register the proposal, the acceptor handles null-values gracefully
          acceptor.accept(completionProposal);
        }
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.bundlemaker.core.transformations.dsl.ui.contentassist.AbstractTransformationDslProposalProvider#
   * completeResourceList_Resources(org.eclipse.emf.ecore.EObject, org.eclipse.xtext.Assignment,
   * org.eclipse.xtext.ui.editor.contentassist.ContentAssistContext,
   * org.eclipse.xtext.ui.editor.contentassist.ICompletionProposalAcceptor)
   */
  @Override
  public void completeResourceList_Resources(EObject model, Assignment assignment, ContentAssistContext context,
      ICompletionProposalAcceptor acceptor) {
    super.completeResourceList_Resources(model, assignment, context, acceptor);

    ResourceList resourceList = (ResourceList) model;
    ResourceSet resourceSet = (ResourceSet) resourceList.eContainer();

    addPackageProposals(resourceSet, context, acceptor);
  }

  private void addPackageProposals(ResourceSet resourceSet, ContentAssistContext context,
      ICompletionProposalAcceptor acceptor) {
    ModuleIdentifier moduleIdentifier = resourceSet.getModuleIdentifier();
    String qualifedModuleName = moduleIdentifier.getModulename() + "_" + moduleIdentifier.getVersion();
    IArtifact root = getRootArtifact(resourceSet);
    IArtifact moduleArtifact = getModuleArtifact(root, qualifedModuleName);

    // Packages, that already have been added to the resource set should not be
    // presented to the user
    HashSet<String> ignoredResources = new HashSet<String>();

    ResourceList excludeResources = resourceSet.getExcludeResources();
    if (excludeResources != null) {
      EList<String> resources = excludeResources.getResources();
      ignoredResources.addAll(resources);
    }

    ResourceList includedResources = resourceSet.getIncludeResources();
    if (includedResources != null) {
      EList<String> resources = includedResources.getResources();
      ignoredResources.addAll(resources);
    }

    // TODO hide already added resource patterns
    addPackageProposals(moduleArtifact, ignoredResources, context, acceptor);
  }

  private void addPackageProposals(IArtifact root, HashSet<String> ignoredResources, ContentAssistContext context,
      ICompletionProposalAcceptor acceptor) {
    if (root == null) {
      return;
    }
    boolean packageArtifact = root.getType() == ArtifactType.Package;
    if (packageArtifact) {
      String packageResourceName = root.getQualifiedName().replace('.', '/') + "/**";

      if (!ignoredResources.contains(packageResourceName)) {
        String packageName = getValueConverter().toString(packageResourceName, "STRING");
        // Create completion proposal
        ICompletionProposal completionProposal = createCompletionProposal(packageName, context);

        // register the proposal, the acceptor handles null-values gracefully
        acceptor.accept(completionProposal);
      }
    }

    for (IArtifact iArtifact : root.getChildren()) {
      addPackageProposals(iArtifact, ignoredResources, context, acceptor);
    }
  }

  private IArtifact getRootArtifact(EObject model) {
    IBundleMakerProject bundleMakerProject = getBundleMakerProject(model);
    System.out.printf("model: %s bundleMakerProject: %s%n", model, bundleMakerProject);

    if (bundleMakerProject == null) {
      return null;
    }

    return null;
  }

  private List<? extends IProjectContentEntry> getFileBasedContent(EObject model) {
    IBundleMakerProject bundleMakerProject = getBundleMakerProject(model);
    System.out.printf("model: %s bundleMakerProject: %s%n", model, bundleMakerProject);

    if (bundleMakerProject == null) {
      return new LinkedList<IProjectContentEntry>();
    }

    if (bundleMakerProject.getState() != BundleMakerProjectState.READY) {
      return new LinkedList<IProjectContentEntry>();
    }

    List<? extends IProjectContentEntry> fileBasedContent = bundleMakerProject.getProjectDescription().getContent();
    return fileBasedContent;

  }

  private IArtifact getModuleArtifact(IArtifact root, String qualifedModuleName) {
    if (root == null) {
      return null;
    }
    if (root.getType() == ArtifactType.Module) {
      if (root.getQualifiedName().equals(qualifedModuleName)) {
        return root;
      }
      return null;
    }

    for (IArtifact iArtifact : root.getChildren()) {
      IArtifact moduleArtifact = getModuleArtifact(iArtifact, qualifedModuleName);
      if (moduleArtifact != null) {
        return moduleArtifact;
      }
    }

    return null;
  }

  public static IProject getOwningProject(EObject object) {
    URI uri = object.eResource().getURI();
    String platformString = uri.toPlatformString(false);
    IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
    IFile file = root.getFile(new Path(platformString));
    IProject project = file.getProject();
    return project;
  }

  public static IBundleMakerProject getBundleMakerProject(EObject object) {
    try {
      IProject project = getOwningProject(object);
      if (BundleMakerCore.isBundleMakerProject(project)) {
        return BundleMakerCore.getBundleMakerProject(project, null);
      }
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return null;
  }

}
