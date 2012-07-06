package org.bundlemaker.core.mvn.content;

import java.io.File;

import org.bundlemaker.core.mvn.aether.Booter;
import org.eclipse.core.runtime.Assert;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.repository.RemoteRepository;

/**
 * <p>
 * </p>
 * 
 * @author Gerd W&uuml;therich (gerd@gerd-wuetherich.de)
 */
public class MvnRepositories implements IMvnRepositories {

  /** - */
  private RepositorySystem        _system;

  /** - */
  private RepositorySystemSession _session;

  /** - */
  private RemoteRepository        _repo;

  /**
   * {@inheritDoc}
   */
  @Override
  public void setMvnRepositories(File localRepo, String remoteRepoUrl) {

    Assert.isNotNull(localRepo);
    Assert.isNotNull(remoteRepoUrl);
    
    System.out.println("setMvnRepositories ");
    System.out.println("localRepo " + localRepo.getAbsolutePath());
    System.out.println("remoteRepoUrl " + remoteRepoUrl);

    try {
      //
      Booter booter = new Booter(localRepo, remoteRepoUrl);
      _system = booter.newRepositorySystem();
      _session = booter.newRepositorySystemSession(_system);
      _repo = booter.newCentralRepository();
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public RepositorySystem getRepositorySystem() {
    return _system;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public RepositorySystemSession getRepositorySystemSession() {
    return _session;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public RemoteRepository getRemoteRepository() {
    return _repo;
  }

}