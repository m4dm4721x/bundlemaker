package org.bundlemaker.core.resource;

import org.eclipse.core.runtime.Assert;

/**
 * <p>
 * </p>
 * 
 * @author Gerd W&uuml;therich (gerd@gerd-wuetherich.de)
 */
public class ResourceKey implements IResourceKey {

	/** - */
	private String _contentId;

	/** - */
	private String _root;

	/** - */
	private String _path;

	/**
	 * <p>
	 * Creates a new instance of type {@link ResourceKey}.
	 * </p>
	 * 
	 * @param contentId
	 * @param root
	 * @param path
	 */
	public ResourceKey(String contentId, String root, String path) {
		Assert.isNotNull(contentId);
		Assert.isNotNull(root);
		Assert.isNotNull(path);

		_contentId = contentId;
		_root = root;
		_path = path;

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getContentId() {
		return _contentId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getRoot() {
		return _root;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getPath() {
		return _path;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + _contentId.hashCode();
		result = prime * result + _path.hashCode();
		result = prime * result + getRoot().hashCode();
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof IResourceKey))
			return false;
		IResourceKey other = (IResourceKey) obj;
		if (!_contentId.equals(other.getContentId()))
			return false;
		if (!_path.equals(other.getPath()))
			return false;
		if (!_root.equals(other.getRoot()))
			return false;
		return true;
	}
}
