package org.bundlemaker.core.internal.parser;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

import org.bundlemaker.core.internal.parser.ModelSetup.Directory;
import org.bundlemaker.core.parser.IProblem;
import org.bundlemaker.core.project.IProjectContentEntry;
import org.bundlemaker.core.spi.parser.IParser;
import org.bundlemaker.core.spi.parser.IParser.ParserType;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;

public class CallableReparse implements Callable<List<IProblem>> {

  /** - */
  private ResourceCache         _resourceCache;

  /** - */
  private IProgressMonitor      _progressMonitor;

  /** - */
  private IProjectContentEntry  _content;

  /** - */
  private Collection<Directory> _directories;

  // /** the list of all errors */
  // private List<IProblem> _errors;

  /** - */
  private IParser[]             _parser;

  /**
   * <p>
   * Creates a new instance of type {@link ParserCallable}.
   * </p>
   * 
   * @param content
   * @param resources
   * @param parser
   * @param resourceCache
   */
  public CallableReparse(IProjectContentEntry content, Collection<Directory> directories, IParser[] parser,
      ResourceCache resourceCache, IProgressMonitor progressMonitor) {

    //
    Assert.isNotNull(content);
    Assert.isNotNull(directories);
    Assert.isNotNull(parser);
    Assert.isNotNull(resourceCache);
    Assert.isNotNull(progressMonitor);

    //
    _content = content;

    // set the directories to parse
    _directories = directories;

    //
    _parser = parser;

    //
    _resourceCache = resourceCache;

    //
    _progressMonitor = progressMonitor;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<IProblem> call() throws Exception {

    List<IProblem> problems = new LinkedList<IProblem>();

    for (Directory directory : _directories) {
      problems.addAll(FunctionalHelper.parseNewOrModifiedResources(_content, directory.getSourceResources(),
          _resourceCache, ParserType.SOURCE, _parser, _progressMonitor));
    }

    for (Directory directory : _directories) {
      problems.addAll(FunctionalHelper.parseNewOrModifiedResources(_content, directory.getBinaryResources(),
          _resourceCache, ParserType.BINARY, _parser, _progressMonitor));
    }

    return problems;
  }
}
