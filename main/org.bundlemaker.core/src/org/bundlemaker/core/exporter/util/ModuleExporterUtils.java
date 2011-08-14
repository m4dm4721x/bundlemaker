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
package org.bundlemaker.core.exporter.util;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bundlemaker.core.modules.IResourceContainer;
import org.bundlemaker.core.modules.IResourceModule;
import org.bundlemaker.core.projectdescription.ContentType;
import org.bundlemaker.core.resource.IResource;
import org.bundlemaker.core.resource.ResourceKey;
import org.bundlemaker.core.util.FileUtils;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;

/**
 * <p>
 * </p>
 * 
 * @author Gerd W&uuml;therich (gerd@gerd-wuetherich.de)
 * 
 * @noextend This class is not intended to be subclassed by clients.
 */
public class ModuleExporterUtils {

  /**
   * <p>
   * </p>
   * 
   * @param templateDirectory
   * @return
   */
  public static boolean hasAdditionalResources(File templateDirectory) {
    return !getAdditionalResources(templateDirectory).isEmpty();
  }

  /**
   * <p>
   * </p>
   * 
   * @param templateDirectory
   * @return
   */
  public static Set<ResourceKey> getAdditionalResources(File templateDirectory) {

    //
    Set<ResourceKey> result = new HashSet<ResourceKey>();

    if (templateDirectory == null) {
      return result;
    }

    try {
      //
      for (String child : FileUtils.getAllChildren(templateDirectory)) {

        // create the resource standin
        ResourceKey resourceKey = new ResourceKey("ADDITIONAL_CONTENT_DUMMY_ID", templateDirectory.getAbsolutePath(),
            child);

        // add the resource
        result.add(resourceKey);
      }
    } catch (CoreException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    // return the result
    return result;
  }

  /**
   * <p>
   * </p>
   * 
   * @param resourceStandins
   * @return
   */
  public static boolean requiresRepackaging(IResourceModule resourceModule, File currentModuleTemplateDirectory,
      ContentType contentType) {

    Assert.isNotNull(resourceModule, "Parameter 'resourceModule' has to be set!");
    Assert.isNotNull(contentType, "Parameter 'type' has to be set!");

    // step 1: requires repackaging if contained containers not empty
    if (!resourceModule.getContainedResourceContainers().isEmpty()) {
      return true;
    }

    // step 3: get the root file (or return true)
    return requiresRepackaging(resourceModule.getSelfResourceContainer(), contentType);
  }

  /**
   * <p>
   * </p>
   * 
   * @param resourceContainer
   * @param contentType
   * @return
   */
  public static boolean requiresRepackaging(IResourceContainer resourceContainer, ContentType contentType) {

    Assert.isNotNull(resourceContainer, "Parameter 'resourceContainer' has to be set!");
    Assert.isNotNull(contentType, "Parameter 'type' has to be set!");

    // step 2: get the root file (or return true)
    String root = null;
    for (IResource resourceStandin : resourceContainer.getResources(contentType)) {
      if (root == null) {
        root = resourceStandin.getRoot();
      } else if (!root.equals(resourceStandin.getRoot())) {
        return true;
      }
    }

    // TODO: root == null -> no content

    if (new File(root).isDirectory()) {
      return true;
    }

    // step 3: check the content
    try {

      // get all children
      List<String> content = FileUtils.getAllChildren(new File(root));

      // get resources count
      if (resourceContainer.getResources(contentType).size() != content.size()) {
        return true;
      }

      //
      for (String entry : content) {
        if (resourceContainer.getResource(entry, contentType) == null) {
          return true;
        }
      }

    } catch (CoreException e) {
      return true;
    }

    // step 4: finally return false
    return false;
  }

  /**
   * <p>
   * </p>
   * 
   * @param resourceModule
   * @return
   * @throws IOException
   */
  public static File getRootFile(IResourceModule resourceModule, ContentType contentType) {

    //
    Assert.isNotNull(resourceModule);
    Assert.isNotNull(contentType);

    //
    if (ModuleExporterUtils.requiresRepackaging(resourceModule, contentType)) {
      return null;
    }

    // get resource standin
    IResource resourceStandin = resourceModule.getResources(contentType).toArray(new IResource[0])[0];

    // return the root
    return new File(resourceStandin.getRoot());
  }
}