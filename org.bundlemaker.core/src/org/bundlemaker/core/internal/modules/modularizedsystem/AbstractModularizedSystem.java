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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bundlemaker.core.IBundleMakerProject;
import org.bundlemaker.core.internal.modules.TypeModule;
import org.bundlemaker.core.modules.IModularizedSystem;
import org.bundlemaker.core.modules.IModule;
import org.bundlemaker.core.modules.IModuleIdentifier;
import org.bundlemaker.core.modules.IResourceModule;
import org.bundlemaker.core.modules.ModuleIdentifier;
import org.bundlemaker.core.modules.modifiable.IModifiableModularizedSystem;
import org.bundlemaker.core.modules.modifiable.IModifiableResourceModule;
import org.bundlemaker.core.modules.query.IQueryFilter;
import org.bundlemaker.core.projectdescription.IBundleMakerProjectDescription;
import org.bundlemaker.core.transformation.ITransformation;
import org.eclipse.core.runtime.Assert;

/**
 * <p>
 * Abstract base class for {@link IModularizedSystem IModularizedSystems}.
 * </p>
 * 
 * @author Gerd W&uuml;therich (gerd@gerd-wuetherich.de)
 */
public abstract class AbstractModularizedSystem implements IModifiableModularizedSystem {

  /** the name of working copy */
  private String                                            _name;

  /** the user attributes */
  private Map<String, Object>                               _userAttributes;

  /** the project description */
  private IBundleMakerProjectDescription                    _projectDescription;

  /** the list of defined transformations */
  private List<ITransformation>                             _transformations;

  /** the defined resource modules */
  private Map<IModuleIdentifier, IModifiableResourceModule> _resourceModules;

  /** the defined type modules */
  private Map<IModuleIdentifier, TypeModule>                _nonResourceModules;

  /** the execution environment type module */
  private TypeModule                                        _executionEnvironment;

  /**
   * <p>
   * Creates a new instance of type {@link AbstractModularizedSystem}.
   * </p>
   * 
   * @param name
   * @param projectDescription
   */
  public AbstractModularizedSystem(String name, IBundleMakerProjectDescription projectDescription) {

    Assert.isNotNull(name);
    Assert.isNotNull(projectDescription);

    // set the name
    _name = name;

    // set the project description
    _projectDescription = projectDescription;

    // initialize fields
    _userAttributes = new HashMap<String, Object>();
    _transformations = new ArrayList<ITransformation>();
    _resourceModules = new HashMap<IModuleIdentifier, IModifiableResourceModule>();
    _nonResourceModules = new HashMap<IModuleIdentifier, TypeModule>();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final String getName() {
    return _name;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Map<String, Object> getUserAttributes() {
    return _userAttributes;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public IBundleMakerProject getBundleMakerProject() {
    return _projectDescription.getBundleMakerProject();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final IBundleMakerProjectDescription getProjectDescription() {
    return _projectDescription;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final List<ITransformation> getTransformations() {
    return _transformations;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final IModule getExecutionEnvironment() {
    return _executionEnvironment;
  }

  /**
   * {@inheritDoc}
   */
  public final Set<IModule> getAllModules() {

    // create the result list
    Set<IModule> result = new HashSet<IModule>(_nonResourceModules.size() + _resourceModules.size());

    // all all modules
    result.addAll(_nonResourceModules.values());
    result.addAll(_resourceModules.values());

    // return an unmodifiable copy
    return Collections.unmodifiableSet(result);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final IModule getModule(IModuleIdentifier identifier) {

    // assert
    Assert.isNotNull(identifier);

    // try to find the module
    if (_resourceModules.containsKey(identifier)) {
      return _resourceModules.get(identifier);
    } else {
      return _nonResourceModules.get(identifier);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Collection<IModule> getModules(String name) {

    // assert
    Assert.isNotNull(name);

    // create result list
    Collection<IModule> result = new LinkedList<IModule>();

    // search in resource modules
    for (IModule iModule : _resourceModules.values()) {
      if (name.equals(iModule.getModuleIdentifier().getName())) {
        result.add(iModule);
      }
    }

    // search in non resource modules
    for (IModule iModule : _nonResourceModules.values()) {
      if (name.equals(iModule.getModuleIdentifier().getName())) {
        result.add(iModule);
      }
    }

    // return result
    return result;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final Collection<IModule> getNonResourceModules() {

    // return the (unmodifiable) result
    return Collections.unmodifiableCollection((Collection<? extends IModule>) _nonResourceModules.values());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public IModule getModule(String name, String version) {
    return getModule(new ModuleIdentifier(name, version));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public IResourceModule getResourceModule(String name, String version) {
    return getResourceModule(new ModuleIdentifier(name, version));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final IResourceModule getResourceModule(IModuleIdentifier identifier) {
    return getModifiableResourceModule(identifier);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final Collection<IResourceModule> getResourceModules() {

    // return the unmodifiable collection
    return Collections.unmodifiableCollection((Collection<? extends IResourceModule>) _resourceModules.values());
  }

  /**
   * {@inheritDoc}
   */
  public final IModifiableResourceModule getModifiableResourceModule(IModuleIdentifier identifier) {

    Assert.isNotNull(identifier);

    //
    return _resourceModules.get(identifier);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Collection<IModule> getAllModules(IQueryFilter<IModule> filter) {

    // create the result list
    Set<IModule> result = new HashSet<IModule>(_nonResourceModules.size() + _resourceModules.size());

    // all all modules
    for (IModule iModule : _nonResourceModules.values()) {
      if (filter.matches(iModule)) {
        result.add(iModule);
      }
    }

    //
    for (IModule iModule : _resourceModules.values()) {
      if (filter.matches(iModule)) {
        result.add(iModule);
      }
    }

    // return an unmodifiable copy
    return Collections.unmodifiableSet(result);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Collection<IModule> getNonResourceModules(IQueryFilter<IModule> filter) {

    // create the result list
    Set<IModule> result = new HashSet<IModule>(_nonResourceModules.size() + _resourceModules.size());

    // all all modules
    for (IModule iModule : _nonResourceModules.values()) {
      if (filter.matches(iModule)) {
        result.add(iModule);
      }
    }

    // return an unmodifiable copy
    return Collections.unmodifiableSet(result);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Collection<IResourceModule> getResourceModules(IQueryFilter<IResourceModule> filter) {

    // create the result list
    Set<IResourceModule> result = new HashSet<IResourceModule>(_nonResourceModules.size() + _resourceModules.size());

    //
    for (IResourceModule iModule : _resourceModules.values()) {
      if (filter.matches(iModule)) {
        result.add(iModule);
      }
    }

    // return an unmodifiable copy
    return Collections.unmodifiableSet(result);
  }

  /**
   * <p>
   * </p>
   * 
   * @return
   */
  protected final Map<IModuleIdentifier, TypeModule> getModifiableNonResourceModulesMap() {
    return _nonResourceModules;
  }

  /**
   * <p>
   * </p>
   * 
   * @return
   */
  protected final Map<IModuleIdentifier, IModifiableResourceModule> getModifiableResourceModulesMap() {
    return _resourceModules;
  }

  /**
   * <p>
   * </p>
   * 
   * @param executionEnvironment
   */
  protected void setExecutionEnvironment(TypeModule executionEnvironment) {

    Assert.isNotNull(executionEnvironment);

    _executionEnvironment = executionEnvironment;
  }
}
