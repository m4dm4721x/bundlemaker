/*******************************************************************************
 * Copyright (c) 2011 Gerd Wuetherich (gerd@gerd-wuetherich.de).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Gerd Wuetherich (gerd@gerd-wuetherich.de) - initial API and implementation
 ******************************************************************************/
package org.bundlemaker.core.internal.modules.modularizedsystem;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.bundlemaker.core.internal.JdkModuleCreator;
import org.bundlemaker.core.internal.modules.AbstractModule;
import org.bundlemaker.core.internal.modules.Group;
import org.bundlemaker.core.internal.modules.ResourceModule;
import org.bundlemaker.core.internal.modules.TypeContainer;
import org.bundlemaker.core.internal.modules.TypeModule;
import org.bundlemaker.core.internal.resource.Type;
import org.bundlemaker.core.internal.transformation.BasicProjectContentTransformation;
import org.bundlemaker.core.modules.IGroup;
import org.bundlemaker.core.modules.IModule;
import org.bundlemaker.core.modules.IModuleIdentifier;
import org.bundlemaker.core.modules.ModuleIdentifier;
import org.bundlemaker.core.modules.modifiable.IModifiableModularizedSystem;
import org.bundlemaker.core.modules.modifiable.IModifiableResourceModule;
import org.bundlemaker.core.modules.transformation.ITransformation;
import org.bundlemaker.core.modules.transformation.IUndoableTransformation;
import org.bundlemaker.core.projectdescription.IProjectContentEntry;
import org.bundlemaker.core.projectdescription.IProjectDescription;
import org.bundlemaker.core.projectdescription.VariablePath;
import org.bundlemaker.core.resource.TypeEnum;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;

/**
 * <p>
 * </p>
 * 
 * @author Gerd W&uuml;therich (gerd@gerd-wuetherich.de)
 */
public abstract class AbstractTransformationAwareModularizedSystem extends AbstractModularizedSystem {

  /**
   * <p>
   * Creates a new instance of type {@link AbstractTransformationAwareModularizedSystem}.
   * </p>
   * 
   * @param name
   * @param projectDescription
   */
  public AbstractTransformationAwareModularizedSystem(String name, IProjectDescription projectDescription) {
    super(name, projectDescription);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void applyTransformations(IProgressMonitor progressMonitor, List<ITransformation> transformations) {

    //
    Assert.isNotNull(transformations);

    //
    if (progressMonitor == null) {
      progressMonitor = new NullProgressMonitor();
    }

    SubMonitor subMonitor = SubMonitor.convert(progressMonitor);
    subMonitor.beginTask("Transforming Module '" + getName() + "'", 100);
    _applyTransformations(subMonitor, transformations.toArray(new ITransformation[0]));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void undoTransformations(IProgressMonitor progressMonitor) {
    undoUntilTransformation(progressMonitor, null);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.bundlemaker.core.modules.IModularizedSystem#undoUntilTransformation(org.eclipse.core.runtime.IProgressMonitor,
   * org.bundlemaker.core.transformation.ITransformation)
   */
  @Override
  public void undoUntilTransformation(IProgressMonitor progressMonitor, ITransformation toTransformation) {
    //
    boolean disableModelModifiedNotification = isModelModifiedNotificationDisabled();

    try {

      //
      disableModelModifiedNotification(true);

      //
      for (ITransformation transformation : getTransformations()) {
        if (!(transformation instanceof IUndoableTransformation)) {
          throw new RuntimeException("TODO");
        }
      }

      // We have to undo the transformations in reverse order
      List<ITransformation> transformationList = getModifiableTransformationList();

      while (!transformationList.isEmpty()) {
        // Get last transformation
        IUndoableTransformation undoableTransformation = (IUndoableTransformation) transformationList
            .get(transformationList.size() - 1);

        // check
        if (toTransformation != null && toTransformation.equals(undoableTransformation)) {
          break;
        }

        // undo transformation
        undoableTransformation.undo();

        // remove from transformation list
        transformationList.remove(undoableTransformation);
      }

    } finally {

      //
      disableModelModifiedNotification(disableModelModifiedNotification);
    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void undoLastTransformation() {

    // get the last transformation
    ITransformation lastTransformation = getTransformations().get(getTransformations().size() - 1);

    // check if we have an undoable transformation
    if (!(lastTransformation instanceof IUndoableTransformation)) {
      throw new RuntimeException("TODO");
    }

    // remove transformation...
    getModifiableTransformationList().remove(getTransformations().size() - 1);

    // ...undo transformation
    IUndoableTransformation undoableTransformation = (IUndoableTransformation) lastTransformation;
    undoableTransformation.undo();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void applyTransformations(IProgressMonitor progressMonitor, ITransformation... transformations) {

    //
    if (progressMonitor == null) {
      progressMonitor = new NullProgressMonitor();
    }

    SubMonitor subMonitor = SubMonitor.convert(progressMonitor);
    subMonitor.beginTask("Transforming Module '" + getName() + "'", 100);
    _applyTransformations(subMonitor, transformations);
  }

  public void initialize(IProgressMonitor progressMonitor) {

    //
    if (progressMonitor == null) {
      progressMonitor = new NullProgressMonitor();
    }

    SubMonitor subMonitor = SubMonitor.convert(progressMonitor);
    subMonitor.beginTask("Transforming Module '" + getName() + "'", 100);

    // step 1: clear prior results
    getModifiableResourceModules().clear();
    getModifiableNonResourceModules().clear();
    preApplyTransformations();

    // // step 2: set up the JRE
    try {
      TypeModule jdkModule = JdkModuleCreator.getJdkModules(this);
      setExecutionEnvironment(jdkModule);
      getModifiableNonResourceModules().add((TypeModule) getExecutionEnvironment());
    } catch (CoreException e1) {
      e1.printStackTrace();
    }

    subMonitor.worked(10);

    // step 3: create the type modules
    for (IProjectContentEntry fileBasedContent : getProjectDescription().getContent()) {
      if (!fileBasedContent.isAnalyze()) {
        IModuleIdentifier identifier = new ModuleIdentifier(fileBasedContent.getName(), fileBasedContent.getVersion());
        // TODO!!
        try {
          TypeModule typeModule = createTypeModule(fileBasedContent.getId().toString(),
              identifier,
              // TODO!!
              new File[] { fileBasedContent.getBinaryRootPaths().toArray(
                  new VariablePath[0])[0]
                  .getAsFile() });
          getModifiableNonResourceModules().add(typeModule);
        } catch (CoreException ex) {
          // TODO
          ex.printStackTrace();
        }
      }
    }
    subMonitor.worked(10);
    //
    postApplyTransformations();
  }

  /**
   * <p>
   * </p>
   * 
   * @param subMonitor
   */
  private void _applyTransformations(SubMonitor subMonitor, ITransformation... transformations) {

    // step 4: transform modules
    SubMonitor transformationMonitor = subMonitor.newChild(70);
    transformationMonitor.beginTask("Begin", transformations.length * 4);

    for (ITransformation transformation : transformations) {

      // step 4.1: apply transformation
      transformation.apply((IModifiableModularizedSystem) this, transformationMonitor.newChild(1));

      // // step 4.2: clean up empty modules
      // for (Iterator<Entry<IModuleIdentifier, IModifiableResourceModule>> iterator = getModifiableResourceModulesMap()
      // .entrySet().iterator(); iterator.hasNext();) {
      //
      // // get next module
      // Entry<IModuleIdentifier, IModifiableResourceModule> module = iterator.next();
      //
      // // if the module is empty - remove it
      // if (module.getValue().getResources(ContentType.BINARY).isEmpty()
      // && module.getValue().getResources(ContentType.SOURCE).isEmpty()) {
      //
      // // remove the module
      // iterator.remove();
      // }
      // }

      //
      if (!(transformation instanceof BasicProjectContentTransformation)) {

        if (!getModifiableTransformationList().contains(transformation)) {
          //
          getModifiableTransformationList().add(transformation);
        }
      }

      //
      transformationMonitor.worked(1);
    }

    afterApplyTransformations();
  }

  protected void afterApplyTransformations() {
    //
  }

  /**
   * <p>
   * </p>
   * 
   * @param path
   * @return
   */
  public Group getOrCreateGroup(IPath path) {

    Assert.isNotNull(path);
    Assert.isTrue(!path.isEmpty(), "Path must not be emtpy.");

    //
    Group group = getGroup(path);
    if (group != null) {
      return group;
    }

    //
    if (path.segmentCount() == 1) {
      Group result = new Group(path.lastSegment(), null, this);
      internalGroups().add(result);
      groupAdded(result);
      return result;
    } else {
      Group parent = getOrCreateGroup(path.removeLastSegments(1));
      Group result = new Group(path.lastSegment(), parent, this);
      internalGroups().add(result);
      groupAdded(result);
      return result;
    }
  }

  /**
   * <p>
   * </p>
   * 
   * @param path
   * @return
   */
  @Override
  public void removeGroup(IGroup group) {

    Assert.isNotNull(group);

    internalGroups().remove(group);
    groupRemoved(group);
  }

  @Override
  public void removeGroup(IPath path) {

    Assert.isNotNull(path);

    IGroup group = getGroup(path);

    if (group == null) {
      // TODO
      throw new RuntimeException(String.format("Group '%s' does not exist.", group));
    }

    removeGroup(group);
  }

  /**
   * <p>
   * </p>
   * 
   * @param path
   * @return
   */
  public Group getGroup(IPath path) {

    // We can not use a hash map here, because it is possible to change the path of a group (which would be the key in
    // the map). So we have to iterate over all groups and find the right one...
    for (Group group : internalGroups()) {
      if (group.getPath().equals(path)) {
        return group;
      }
    }

    return null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public IModifiableResourceModule createResourceModule(IModuleIdentifier createModuleIdentifier) {

    // create the result
    ResourceModule resourceModule = new ResourceModule(createModuleIdentifier, this);

    // add it to the internal hash map
    getModifiableResourceModules().add(resourceModule);

    // notify
    resourceModuleAdded(resourceModule);

    // return the result
    return resourceModule;
  }

  @Override
  public IModifiableResourceModule createResourceModule(ModuleIdentifier moduleIdentifier, IPath path) {
    throw new UnsupportedOperationException();
  }

  /**
   * {@inheritDoc}
   */
  @SuppressWarnings("rawtypes")
  @Override
  public void addModule(IModule module) {
    Assert.isNotNull(module);

    if (module instanceof IModifiableResourceModule) {

      //
      Assert.isTrue(!hasResourceModule(module.getModuleIdentifier()));

      //
      IModifiableResourceModule resourceModule = (IModifiableResourceModule) module;

      //
      ((AbstractModule) resourceModule).attach(this);
      getModifiableResourceModules().add(resourceModule);

      // notify
      resourceModuleAdded(resourceModule);

    } else if (module instanceof TypeModule) {

      //
      Assert.isTrue(!hasTypeModule(module.getModuleIdentifier()));

      //
      TypeModule typeModule = (TypeModule) module;

      //
      ((AbstractModule) typeModule).attach(this);
      getNonResourceModules().add(typeModule);

      // notify
      typeModuleAdded(typeModule);
    }
  }

  /**
   * {@inheritDoc}
   */

  @SuppressWarnings("rawtypes")
  @Override
  public void removeModule(IModuleIdentifier identifier) {
    Assert.isNotNull(identifier);

    if (hasResourceModule(identifier)) {

      // remove the entry
      AbstractModule resourceModule = (AbstractModule) getResourceModule(identifier);
      getModifiableResourceModules().remove(resourceModule);
      resourceModule.detach();

      // notify
      resourceModuleRemoved((IModifiableResourceModule) resourceModule);

    } else if (hasTypeModule(identifier)) {

      // remove the entry
      AbstractModule module = (AbstractModule) getModule(identifier);
      getModifiableNonResourceModules().remove(module);
      ((AbstractModule) module).detach();

      // notify
      typeModuleRemoved((TypeModule) module);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void removeModule(IModule module) {
    Assert.isNotNull(module);

    // remove the module
    removeModule(module.getModuleIdentifier());
  }

  // /**
  // * {@inheritDoc}
  // */
  // @Override
  // public Collection<IModifiableResourceModule> getModifiableResourceModules() {
  // // return an unmodifiable copy
  // return Collections.unmodifiableCollection(getModifiableResourceModulesMap().values());
  // }

  /**
   * <p>
   * </p>
   * 
   */
  protected void preApplyTransformations() {
    // do nothing...
  }

  /**
   * <p>
   * </p>
   * 
   */
  protected void postApplyTransformations() {
    // do nothing...
  }

  /**
   * <p>
   * </p>
   * 
   * @param group
   */
  protected void groupAdded(IGroup group) {
    // do nothing...
  }

  /**
   * <p>
   * </p>
   * 
   * @param group
   */
  protected void groupRemoved(IGroup group) {
    // do nothing...
  }

  /**
   * <p>
   * </p>
   * 
   * @param resourceModule
   */
  protected void resourceModuleAdded(IModifiableResourceModule resourceModule) {
    // do nothing...
  }

  /**
   * <p>
   * </p>
   * 
   * @param resourceModule
   */
  protected void resourceModuleRemoved(IModifiableResourceModule resourceModule) {
    // do nothing...
  }

  /**
   * <p>
   * </p>
   * 
   * @param module
   */
  protected void typeModuleAdded(TypeModule module) {
    // do nothing
  }

  /**
   * <p>
   * </p>
   * 
   * @param module
   */
  protected void typeModuleRemoved(TypeModule module) {
    // do nothing...
  }

  /**
   * <p>
   * </p>
   * 
   * @param identifier
   * @param files
   * @return
   */
  private TypeModule createTypeModule(String contentId, IModuleIdentifier identifier, File... files) {

    // create the type module
    TypeModule typeModule = new TypeModule(identifier, this);

    //
    for (int i = 0; i < files.length; i++) {

      // add all the contained types
      try {

        // TODO DIRECTORIES!!
        // TODO:PARSE!!
        List<String> types = getContainedTypesFromJarFile(files[i]);

        for (String type : types) {

          // TODO: TypeEnum!!
          Type type2 = new Type(type, TypeEnum.CLASS, contentId, false);

          // type2.setTypeModule(typeModule);

          ((TypeContainer) typeModule.getModifiableSelfResourceContainer()).add(type2);
        }

      } catch (IOException e) {

        //
        e.printStackTrace();
      }
    }
    // return the module
    return typeModule;
  }

  /**
   * <p>
   * </p>
   * 
   * @param file
   * @return
   * @throws IOException
   */
  private static List<String> getContainedTypesFromJarFile(File file) throws IOException {

    // create the result list
    List<String> result = new LinkedList<String>();

    // create the jar file
    JarFile jarFile = new JarFile(file);

    // get the entries
    Enumeration<JarEntry> entries = jarFile.entries();
    while (entries.hasMoreElements()) {
      JarEntry jarEntry = (JarEntry) entries.nextElement();
      if (jarEntry.getName().endsWith(".class")) {
        result.add(jarEntry.getName().substring(0, jarEntry.getName().length() - ".class".length()).replace('/', '.'));
      }
    }

    // return the result
    return result;
  }
}