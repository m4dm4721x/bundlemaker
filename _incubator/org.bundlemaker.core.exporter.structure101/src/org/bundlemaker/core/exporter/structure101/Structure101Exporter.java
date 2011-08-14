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
package org.bundlemaker.core.exporter.structure101;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bundlemaker.core.exporter.IModularizedSystemExporter;
import org.bundlemaker.core.exporter.IModuleExporterContext;
import org.bundlemaker.core.exporter.structure101.xml.DataType;
import org.bundlemaker.core.exporter.structure101.xml.DependenciesType;
import org.bundlemaker.core.exporter.structure101.xml.DependencyType;
import org.bundlemaker.core.exporter.structure101.xml.ModuleType;
import org.bundlemaker.core.exporter.structure101.xml.ModulesType;
import org.bundlemaker.core.modules.AmbiguousElementException;
import org.bundlemaker.core.modules.IModularizedSystem;
import org.bundlemaker.core.modules.IModule;
import org.bundlemaker.core.modules.IResourceModule;
import org.bundlemaker.core.modules.query.IQueryFilter;
import org.bundlemaker.core.projectdescription.ContentType;
import org.bundlemaker.core.resource.IReference;
import org.bundlemaker.core.resource.IResource;
import org.bundlemaker.core.resource.IType;
import org.bundlemaker.core.util.GenericCache;
import org.bundlemaker.core.util.StopWatch;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * <p>
 * </p>
 * 
 * @author Gerd W&uuml;therich (gerd@gerd-wuetherich.de)
 */
public class Structure101Exporter implements IModularizedSystemExporter, Structure101ExporterConstants {

  /** - */
  private DataType              _result;

  /** - */
  private IdentifierMap         _identifierMap;

  /** - */
  private IQueryFilter<IModule> _moduleFilter;

  /** - */
  private boolean               _excludeModuleSelfReferences;

  /**
   * <p>
   * Creates a new instance of type {@link Structure101Exporter}.
   * </p>
   */
  public Structure101Exporter() {

    //
    _excludeModuleSelfReferences = true;
  }

  /**
   * <p>
   * </p>
   * 
   * @param moduleFilter
   */
  public void setModuleFilter(IQueryFilter<IModule> moduleFilter) {
    _moduleFilter = moduleFilter;
  }

  /**
   * <p>
   * </p>
   * 
   * @return
   */
  public boolean isExcludeModuleSelfReferences() {
    return _excludeModuleSelfReferences;
  }

  /**
   * <p>
   * </p>
   * 
   * @param excludeModuleSelfReferences
   */
  public void setExcludeModuleSelfReferences(boolean excludeModuleSelfReferences) {
    _excludeModuleSelfReferences = excludeModuleSelfReferences;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void export(IModularizedSystem modularizedSystem, IModuleExporterContext context, IProgressMonitor progressMonitor) throws Exception {

    // set up the DataType element
    _result = new DataType();
    _result.setFlavor(STRUCTURE_101_FLAVOR);
    _result.setModules(new ModulesType());
    _result.setDependencies(new DependenciesType());

    //
    _identifierMap = new IdentifierMap();

    StopWatch stopWatch = new StopWatch();
    stopWatch.start();

    //
    for (IModule module : modularizedSystem.getAllModules()) {

      //
      if (_moduleFilter == null || _moduleFilter.matches(module)) {

        // create the entries
        createEntries(module, modularizedSystem);
      }
    }

    stopWatch.stop();
    System.out.println("Elapsed Time : " + stopWatch.getElapsedTime());

    //
    FileOutputStream outputStream = new FileOutputStream(
        new File(context.getDestinationDirectory(), "structure101.xml"));

    //
    Structure101ExporterUtils.marshal(_result, outputStream);
  }

  /**
   * <p>
   * </p>
   * 
   * @param typeModule
   * @param packageList
   */
  private void createEntries(IModule typeModule, IModularizedSystem modularizedSystem) {

    Assert.isNotNull(typeModule);
    Assert.isNotNull(modularizedSystem);

    // step 1: create and add the ModuleType
    createModuleWithResources(typeModule, ContentType.SOURCE);

    // step 3: add the dependencies
    createDependencies(typeModule, modularizedSystem);
  }

  /**
   * <p>
   * </p>
   * 
   * @param typeModule
   * @param modularizedSystem
   */
  private void createDependencies(IModule typeModule, IModularizedSystem modularizedSystem) {

    // only handle resource modules
    if (typeModule instanceof IResourceModule) {

      //
      IResourceModule resourceModule = (IResourceModule) typeModule;

      // IReferencedModulesQueryResult queryResult = modularizedSystem
      // .getReferencedModules(resourceModule, false, true);

      Set<TypeToTypeDependency> dependencies = new HashSet<TypeToTypeDependency>();

      // TODO: Make configurable
      ContentType contentType = resourceModule.containsSources() ? ContentType.SOURCE : ContentType.BINARY;

      for (IResource resource : resourceModule.getResources(contentType)) {

        for (IReference reference : resource.getReferences()) {

          IModule referencedModule = getReferencedModule(modularizedSystem, reference, resourceModule);

          if (referencedModule == null) {
            continue;
          }

          // TODO: CONFIGURATION
          // exclude self references
          if (isExcludeModuleSelfReferences() && referencedModule.equals(typeModule)) {
            continue;
          }

          // from
          String from = _identifierMap.getResourceId(resourceModule, resource.getPath());
          // to
          String to = _identifierMap.getClassId(referencedModule, reference.getFullyQualifiedName());

          // dependency
          TypeToTypeDependency dependency = new TypeToTypeDependency(from, to, reference.isImplements(),
              reference.isExtends());
          //
          dependencies.add(dependency);
        }

        // process types
        for (IType type : resource.getContainedTypes()) {

          for (IReference reference : type.getReferences()) {

            IModule referencedModule = getReferencedModule(modularizedSystem, reference, resourceModule);

            if (referencedModule == null) {
              continue;
            }

            // TODO: CONFIGURATION
            // exclude self references
            if (isExcludeModuleSelfReferences() && referencedModule.equals(typeModule)) {
              continue;
            }

            // from
            String from = _identifierMap.getClassId(resourceModule, type.getFullyQualifiedName());
            // to
            String to = _identifierMap.getClassId(referencedModule, reference.getFullyQualifiedName());

            // dependency
            TypeToTypeDependency dependency = new TypeToTypeDependency(from, to, reference.isImplements(),
                reference.isExtends());
            //
            dependencies.add(dependency);
          }
        }
      }

      //
      for (TypeToTypeDependency typeToTypeDependency : dependencies) {

        DependencyType dependency = new DependencyType();
        dependency.setType(TYPE_REFERENCES);
        dependency.setFrom(typeToTypeDependency.getFrom());
        dependency.setTo(typeToTypeDependency.getTo());

        _result.getDependencies().getDependency().add(dependency);
      }
    }
  }

  private IModule getReferencedModule(IModularizedSystem modularizedSystem, IReference reference,
      IResourceModule resourceModule) {

    //
    IModule referencedModule = null;

    //
    try {
      referencedModule = modularizedSystem.getTypeContainingModule(reference.getFullyQualifiedName(), resourceModule);
    } catch (AmbiguousElementException e) {
      System.out.println("~~~~~~~");
      System.out.println(reference.getFullyQualifiedName());
      for (IModule iModule : modularizedSystem.getTypeContainingModules(reference.getFullyQualifiedName())) {
        System.out.println(" - " + iModule.getModuleIdentifier());
      }
      return null;
    }

    // if (referencedModules.size() > 1) {
    // System.out.println("~~~~~~~");
    // System.out.println(reference.getFullyQualifiedName());
    // for (IModule iModule : referencedModules) {
    // System.out.println(" - " + iModule.getModuleIdentifier());
    // }
    // return null;
    // }

    if (referencedModule == null) {
      System.out.println("MISSING TYPE " + reference.getFullyQualifiedName());
      return null;
    }

    //
    return referencedModule;
  }

  /**
   * <p>
   * Creates a {@link ModuleType} for the given {@link IModule}
   * </p>
   * 
   * @param module
   * @param packageList
   * @return
   */
  private void createModuleWithTypes(IModule module) {

    // step 1: get all types ordered by package
    Map<String, List<IType>> packageList = extractTypePackageList(module);

    // step 2: add the module
    ModuleType moduleSubmodule = addModule(module);

    //
    for (Entry<String, List<IType>> entry : packageList.entrySet()) {

      // add the package to the module
      ModuleType packageSubmodule = addPackage(moduleSubmodule, module, entry.getKey());

      // add all class names to to package
      for (IType type : entry.getValue()) {
        addType(packageSubmodule, module, type);
      }
    }
  }

  /**
   * <p>
   * </p>
   * 
   * @param module
   * @param contentType
   */
  private void createModuleWithResources(IModule module, ContentType contentType) {

    //
    if (!(module instanceof IResourceModule)) {

      //
      createModuleWithTypes(module);
      return;

    } else {

      // step 1: get all types ordered by package
      Map<String, List<IResource>> packageList = extractResourcePackageList((IResourceModule) module, contentType);

      // step 2: add the module
      ModuleType moduleSubmodule = addModule(module);

      //
      for (Entry<String, List<IResource>> entry : packageList.entrySet()) {

        // add the package to the module
        ModuleType packageSubmodule = addPackage(moduleSubmodule, module, entry.getKey());

        // add all class names to to package
        for (IResource resource : entry.getValue()) {
          addResource(packageSubmodule, module, resource);
        }
      }
    }
  }

  private ModuleType addModule(IModule module) {
    // step 1: create the result
    ModuleType moduleType = new ModuleType();
    moduleType.setType(TYPE_OSGIBUNDLE);
    moduleType.setName(getTypeModuleName(module));
    moduleType.setId(_identifierMap.getModuleId(module));

    // add the module to the modules
    _result.getModules().getModule().add(moduleType);
    return moduleType;
  }

  /**
   * <p>
   * </p>
   * 
   * @param parent
   * @param typeModule
   * @param packageName
   * @return
   */
  private ModuleType addPackage(ModuleType parent, IModule typeModule, String packageName) {

    // add an entry for each package
    ModuleType packageSubmodule = new ModuleType();
    packageSubmodule.setType(TYPE_PACKAGE);
    packageSubmodule.setName(packageName);
    packageSubmodule.setId(_identifierMap.getPackageId(typeModule, packageName));

    // add the package modules
    parent.getSubmodule().add(packageSubmodule);
    return packageSubmodule;
  }

  /**
   * <p>
   * </p>
   * 
   * @param packageSubmodule
   * @param module
   * @param resource
   */
  private void addResource(ModuleType parent, IModule module, IResource resource) {

    // create class sub module
    ModuleType resourceSubmodule = new ModuleType();

    // set the type
    if (resource.getPath().endsWith(".java")) {
      resourceSubmodule.setType(TYPE_JAVACLASSFILE);
    } else if (resource.getPath().endsWith(".class")) {
      resourceSubmodule.setType(TYPE_CLASSFILE);
    } else {
      resourceSubmodule.setType(TYPE_GENERICFILE);
    }

    // set the class name
    resourceSubmodule.setName(resource.getName());

    // set the id
    resourceSubmodule.setId(_identifierMap.getResourceId(module, resource.getPath()));

    // add the submodule
    parent.getSubmodule().add(resourceSubmodule);

    // add the contained types
    for (IType type : resource.getContainedTypes()) {

      //
      addType(resourceSubmodule, module, type);
    }

  }

  /**
   * <p>
   * </p>
   * 
   * @param parent
   * @param typeModule
   * @param type
   */
  private void addType(ModuleType parent, IModule typeModule, IType type) {

    // create class sub module
    ModuleType classSubmodule = new ModuleType();

    // set the type
    switch (type.getType()) {
    case CLASS: {
      classSubmodule.setType(TYPE_CLASS);
      break;
    }
    case INTERFACE: {
      classSubmodule.setType(TYPE_INTERFACE);
      break;
    }
    case ANNOTATION: {
      classSubmodule.setType(TYPE_ANNOTATION);
      break;
    }
    case ENUM: {
      classSubmodule.setType(TYPE_ENUM);
      break;
    }
    default:
      classSubmodule.setType(TYPE_CLASS);
      break;
    }

    // set the class name
    classSubmodule.setName(type.getName());

    // set the id
    classSubmodule.setId(_identifierMap.getClassId(typeModule, type.getFullyQualifiedName()));

    // add the submodule
    parent.getSubmodule().add(classSubmodule);
  }

  /**
   * <p>
   * </p>
   * 
   * @param typeModule
   * @return
   */
  private Map<String, List<IType>> extractTypePackageList(IModule typeModule) {

    // create the package list
    GenericCache<String, List<IType>> packageCache = new GenericCache<String, List<IType>>() {
      @Override
      protected List<IType> create(String key) {
        return new LinkedList<IType>();
      }
    };

    // get the contained types
    for (IType type : typeModule.getContainedTypes()) {

      // get the package
      packageCache.getOrCreate(type.getPackageName()).add(type);
    }

    // return the package list
    return packageCache.getMap();
  }

  /**
   * <p>
   * </p>
   * 
   * @param resourceModule
   * @param contentType
   * @return
   */
  private Map<String, List<IResource>> extractResourcePackageList(IResourceModule resourceModule,
      ContentType contentType) {

    // create the package list
    GenericCache<String, List<IResource>> packageCache = new GenericCache<String, List<IResource>>() {
      @Override
      protected List<IResource> create(String key) {
        return new LinkedList<IResource>();
      }
    };

    //
    if (contentType.equals(ContentType.SOURCE) && resourceModule.getResources(ContentType.SOURCE).isEmpty()) {
      //
      contentType = ContentType.BINARY;
    }

    // get the contained types
    for (IResource resource : resourceModule.getResources(contentType)) {

      // get the package
      packageCache.getOrCreate(resource.getPackageName()).add(resource);
    }

    // return the package list
    return packageCache.getMap();
  }

  /**
   * <p>
   * </p>
   * 
   * @param typeModule
   * @return
   */
  protected String getTypeModuleName(IModule typeModule) {

    //
    return typeModule.getClassification() != null ? typeModule.getClassification().toString() + "/"
        + typeModule.getModuleIdentifier().toString() : typeModule.getModuleIdentifier().toString();
  }
}