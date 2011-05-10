package org.bundlemaker.core.internal.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.FutureTask;

import org.bundlemaker.core.internal.Activator;
import org.bundlemaker.core.internal.BundleMakerProject;
import org.bundlemaker.core.internal.projectdescription.FileBasedContent;
import org.bundlemaker.core.internal.resource.Resource;
import org.bundlemaker.core.internal.resource.ResourceStandin;
import org.bundlemaker.core.internal.store.IDependencyStore;
import org.bundlemaker.core.internal.store.IPersistentDependencyStore;
import org.bundlemaker.core.parser.IParser;
import org.bundlemaker.core.parser.IParserFactory;
import org.bundlemaker.core.resource.IResourceKey;
import org.bundlemaker.core.util.GenericCache;
import org.bundlemaker.core.util.StopWatch;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;

/**
 * <p>
 * </p>
 * 
 * @author Gerd W&uuml;therich (gerd@gerd-wuetherich.de)
 */
public class ModelSetup {

  public static final boolean LOG                      = true;

  /** THREAD_COUNT */
  private static final int    THREAD_COUNT             = Runtime.getRuntime().availableProcessors();

  /** the bundle maker project */
  private BundleMakerProject  _bundleMakerProject;

  /**  */
  private List<IParser[]>     _parsers4threads;

  /** - */
  private boolean             _parseIndirectReferences = true;

  /**
   * <p>
   * Creates a new instance of type {@link ProjectParser}.
   * </p>
   * 
   * @param bundleMakerProject
   *          the bundle maker project
   */
  public ModelSetup(BundleMakerProject bundleMakerProject) {
    Assert.isNotNull(bundleMakerProject);

    // set the project
    _bundleMakerProject = bundleMakerProject;
  }

  /**
   * <p>
   * </p>
   * 
   * @param modifiableFileBasedContent
   * @param dependencyStore
   */
  public void setup(final List<FileBasedContent> fileBasedContents, final IPersistentDependencyStore dependencyStore,
      IProgressMonitor mainMonitor) throws OperationCanceledException, CoreException {

    Assert.isNotNull(fileBasedContents);
    Assert.isNotNull(dependencyStore);

    // create new null monitor if necessary
    if (mainMonitor == null) {
      mainMonitor = new NullProgressMonitor();
    }

    // create the sub-monitor
    final SubMonitor progressMonitor = SubMonitor.convert(mainMonitor, 100);

    //
    setupParsers();

    //
    notifyParseStart();

    try {

      // ***********************************************************************************************
      // STEP 1: Read all the resources from the underlying dependency store and put it in a map
      // ***********************************************************************************************
      mainMonitor.subTask("Reading from datastore...");

      // execute as loggable action...
      final Map<IResourceKey, Resource> storedResourcesMap = StaticLog.log(LOG, "Reading from datastore",
          new LoggableAction<Map<IResourceKey, Resource>>() {
            @Override
            public Map<IResourceKey, Resource> execute() {
              return readFromDependencyStore(dependencyStore, progressMonitor.newChild(10));
            }
          });

      // ***********************************************************************************************
      // STEP 2: Perform up-to-date check and parse new or modified resources
      // ***********************************************************************************************
      // create the resource cache that holds all resources that must be stored
      mainMonitor.subTask("Reparsing...");
      final ResourceCache resourceCache = new ResourceCache(dependencyStore);

      // execute as loggable action...
      StaticLog.log(LOG, "Compare and update...", new LoggableAction<Void>() {
        @Override
        public Void execute() {
          compareAndUpdate(fileBasedContents, storedResourcesMap, resourceCache, progressMonitor.newChild(60));
          return null;
        }
      });

      // ***********************************************************************************************
      // STEP 3: Update dependency store
      // ***********************************************************************************************

      mainMonitor.subTask("Writing to disc...");
      resourceCache.commit(progressMonitor.newChild(25));
      deleteResourcesFromDependencyStore(storedResourcesMap.values(), dependencyStore, progressMonitor.newChild(5));

      // ***********************************************************************************************
      // STEP 4: Setup the resource content
      // ***********************************************************************************************
      mainMonitor.subTask("Set up model...");
      Map<IResourceKey, Resource> newMap = resourceCache.getCombinedMap();

      // set up binary resources
      FunctionalHelper.associateResourceStandinsWithResources(_bundleMakerProject.getBinaryResourceStandins(), newMap,
          false, progressMonitor);

      // set up binary resources
      FunctionalHelper.associateResourceStandinsWithResources(_bundleMakerProject.getSourceResourceStandins(), newMap,
          true, progressMonitor);

      progressMonitor.worked(1);

    } finally {
      progressMonitor.done();
    }

    //
    notifyParseStop();
  }

  /**
   * <p>
   * </p>
   * 
   * @param fileBasedContents
   * @param storedResourcesMap
   * @param resourceCache
   * @param mainMonitor
   */
  private void compareAndUpdate(List<FileBasedContent> fileBasedContents,
      Map<IResourceKey, Resource> storedResourcesMap, ResourceCache resourceCache, IProgressMonitor mainMonitor) {

    //
    StopWatch stopWatch = null;

    //
    int contentCount = fileBasedContents.size();
    SubMonitor subMonitor = SubMonitor.convert(mainMonitor, contentCount);

    try {

      //
      for (FileBasedContent fileBasedContent : fileBasedContents) {

        SubMonitor contentMonitor = subMonitor.newChild(1);

        // we only have check resource content
        if (fileBasedContent.isResourceContent()) {

          //
          if (LOG) {
            stopWatch = new StopWatch();
            stopWatch.start();
          }

          SubMonitor resourceContentMonitor = SubMonitor.convert(contentMonitor, (fileBasedContent
              .getModifiableBinaryResources().size() + fileBasedContent.getModifiableSourceResources().size()));

          // step 4.1: compute new and modified resources
          Set<ResourceStandin> newAndModifiedBinaryResources = FunctionalHelper.computeNewAndModifiedResources(
              fileBasedContent.getModifiableBinaryResources(), storedResourcesMap, resourceCache,
              new NullProgressMonitor());

          Set<ResourceStandin> newAndModifiedSourceResources = FunctionalHelper.computeNewAndModifiedResources(
              fileBasedContent.getModifiableSourceResources(), storedResourcesMap, resourceCache,
              new NullProgressMonitor());

          //
          if (LOG) {
            StaticLog.log(String.format(" - compare and update '%s_%s' - computeNewAndModifiedResources [%s ms]",
                fileBasedContent.getName(), fileBasedContent.getVersion(), stopWatch.getElapsedTime()));

            StaticLog
                .log(String.format("   - new/modified binary resources: %s", newAndModifiedBinaryResources.size()));
            StaticLog
                .log(String.format("   - new/modified source resources: %s", newAndModifiedSourceResources.size()));
          }

          // step 4.2:
          for (ResourceStandin resourceStandin : newAndModifiedBinaryResources) {
            resourceCache.getOrCreateResource(resourceStandin);
          }
          for (ResourceStandin resourceStandin : newAndModifiedSourceResources) {
            resourceCache.getOrCreateResource(resourceStandin);
          }

          resourceCache.setupTypeCache(fileBasedContent);

          // adjust work remaining
          int remaining = newAndModifiedSourceResources.size() + newAndModifiedBinaryResources.size();

          resourceContentMonitor.setWorkRemaining(remaining);

          multiThreadedReparse(storedResourcesMap, newAndModifiedSourceResources, newAndModifiedBinaryResources,
              resourceCache, fileBasedContent, resourceContentMonitor.newChild(remaining));

        }

        // adjust monitor in case that fileBasedContent is NOT resource content
        subMonitor.setWorkRemaining(contentCount--);
      }
    } finally {
      subMonitor.done();
    }
  }

  private void multiThreadedReparse(Map<IResourceKey, Resource> storedResourcesMap,
      Collection<ResourceStandin> sourceResources, Collection<ResourceStandin> binaryResources,
      ResourceCache resourceCache, FileBasedContent fileBasedContent, IProgressMonitor monitor) {

    //
    monitor.beginTask("PARSE ", sourceResources.size() + binaryResources.size());

    try {

      //
      GenericCache<String, Directory> directories = new GenericCache<String, ModelSetup.Directory>() {
        @Override
        protected Directory create(String key) {
          return new Directory();
        }
      };

      //
      for (ResourceStandin resourceStandin : binaryResources) {
        directories.getOrCreate(resourceStandin.getDirectory()).addBinaryResource(resourceStandin);
      }
      for (ResourceStandin resourceStandin : sourceResources) {
        directories.getOrCreate(resourceStandin.getDirectory()).addSourceResource(resourceStandin);
      }

      // compute the part size
      float partSizeAsFloat = directories.size() / (float) THREAD_COUNT;
      int partSize = (int) Math.ceil(partSizeAsFloat);

      // split the package list in n sublist (one for each thread)
      List<Directory> dirs = new ArrayList<ModelSetup.Directory>(directories.values());
      List<Directory>[] packageFragmentsParts = new List[THREAD_COUNT];
      for (int i = 0; i < THREAD_COUNT; i++) {
        if ((i + 1) * partSize <= directories.size()) {
          packageFragmentsParts[i] = dirs.subList(i * partSize, (i + 1) * partSize);
        } else if ((i) * partSize <= dirs.size()) {
          packageFragmentsParts[i] = dirs.subList(i * partSize, dirs.size());
        } else {
          packageFragmentsParts[i] = Collections.EMPTY_LIST;
        }
      }

      // set up the callables
      CallableReparse[] callables = new CallableReparse[THREAD_COUNT];
      for (int i = 0; i < callables.length; i++) {
        callables[i] = new CallableReparse(fileBasedContent, packageFragmentsParts[i], _parsers4threads.get(i),
            resourceCache, monitor);
      }

      // create the future tasks
      FutureTask<Void>[] futureTasks = new FutureTask[THREAD_COUNT];
      for (int i = 0; i < futureTasks.length; i++) {
        futureTasks[i] = new FutureTask<Void>(callables[i]);
        new Thread(futureTasks[i]).start();
      }

      // collect the result
      for (int i = 0; i < futureTasks.length; i++) {
        try {
          futureTasks[i].get();
        } catch (Exception e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }

    } finally {
      monitor.done();
    }
  }

  /**
   * <p>
   * </p>
   * 
   * @param values
   */
  private void deleteResourcesFromDependencyStore(Collection<Resource> values,
      IPersistentDependencyStore dependencyStore, IProgressMonitor progressMonitor) {

    //
    if (progressMonitor != null) {
      progressMonitor.beginTask("Clean up database...", values.size());
    }

    //
    for (Resource resource : values) {
      dependencyStore.delete(resource);

      //
      if (progressMonitor != null) {
        progressMonitor.worked(1);
      }
    }

    // commit the changes
    dependencyStore.commit();

    //
    if (progressMonitor != null) {
      progressMonitor.done();
    }
  }

  /**
   * <p>
   * Reads all resources from the underlying dependency store.
   * </p>
   * 
   * @param dependencyStore
   * @param monitor
   * @return
   */
  private static Map<IResourceKey, Resource> readFromDependencyStore(IDependencyStore dependencyStore,
      IProgressMonitor monitor) {

    Assert.isNotNull(dependencyStore);
    Assert.isNotNull(monitor);

    Map<IResourceKey, Resource> map = new HashMap<IResourceKey, Resource>();

    if (dependencyStore != null) {

      List<Resource> resources = dependencyStore.getResources();

      monitor.beginTask("Opening database ", resources.size());

      for (Resource resource : resources) {

        // check if canceled
        // checkIfCanceled(monitor);

        // put in the map
        map.put(resource, resource);

        // set monitor
        monitor.worked(1);
      }
    }

    // work is done
    monitor.done();

    // return the map
    return map;
  }

  /**
   * <p>
   * </p>
   * 
   * @throws CoreException
   */
  private void setupParsers() throws CoreException {

    // get the registered parser factories
    List<IParserFactory> parserFactories = Activator.getDefault().getParserFactoryRegistry().getParserFactories();

    // no parsers defined
    if (parserFactories.isEmpty()) {
      throw new RuntimeException("No parserFactories defined...");
    }

    // create one parser for each thread...
    List<IParser[]> parsers4threads = new LinkedList<IParser[]>();
    for (int i = 0; i < THREAD_COUNT; i++) {
      parsers4threads.add(new IParser[parserFactories.size()]);
    }

    // ... setup
    for (IParser[] parsers : parsers4threads) {
      for (int i = 0; i < parsers.length; i++) {
        parsers[i] = parserFactories.get(i).createParser(_bundleMakerProject, _parseIndirectReferences);
      }
    }

    // sort
    for (int i = 0; i < parsers4threads.size(); i++) {
      Arrays.sort(parsers4threads.get(i), new Comparator<IParser>() {
        @Override
        public int compare(IParser o1, IParser o2) {
          return o2.getParserType().compareTo(o1.getParserType());
        }
      });
    }

    _parsers4threads = parsers4threads;
  }

  /**
   * <p>
   * </p>
   * 
   * @throws CoreException
   */
  private void notifyParseStart() throws CoreException {

    //
    for (IParser[] parsers : _parsers4threads) {
      for (IParser parser : parsers) {

        // notify 'start'
        parser.parseBundleMakerProjectStart(_bundleMakerProject);
      }
    }
  }

  /**
   * <p>
   * </p>
   * 
   * @throws CoreException
   */
  private void notifyParseStop() throws CoreException {

    //
    for (IParser[] parsers : _parsers4threads) {
      for (IParser parser : parsers) {

        // notify 'stop'
        parser.parseBundleMakerProjectStop(_bundleMakerProject);
      }
    }
  }

  /**
   * <p>
   * </p>
   * 
   * @author Gerd W&uuml;therich (gerd@gerd-wuetherich.de)
   */
  public static class Directory {

    /** - */
    private List<ResourceStandin> _binaryResources;

    /** - */
    private List<ResourceStandin> _sourceResources;

    /** - */
    private int                   _count = 0;

    /**
     * <p>
     * Creates a new instance of type {@link Directory}.
     * </p>
     */
    public Directory() {
      _binaryResources = new LinkedList<ResourceStandin>();
      _sourceResources = new LinkedList<ResourceStandin>();
    }

    /**
     * <p>
     * </p>
     * 
     * @param resourceStandin
     */
    public void addBinaryResource(ResourceStandin resourceStandin) {
      _binaryResources.add(resourceStandin);
      _count++;
    }

    /**
     * <p>
     * </p>
     * 
     * @param resourceStandin
     */
    public void addSourceResource(ResourceStandin resourceStandin) {
      _sourceResources.add(resourceStandin);
      _count++;
    }

    /**
     * <p>
     * </p>
     * 
     * @return
     */
    public List<ResourceStandin> getBinaryResources() {
      return _binaryResources;
    }

    /**
     * <p>
     * </p>
     * 
     * @return
     */
    public List<ResourceStandin> getSourceResources() {
      return _sourceResources;
    }

    /**
     * <p>
     * </p>
     * 
     * @return
     */
    public int getCount() {
      return _count;
    }
  }
}