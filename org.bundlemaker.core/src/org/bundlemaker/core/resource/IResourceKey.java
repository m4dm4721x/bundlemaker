package org.bundlemaker.core.resource;

import org.bundlemaker.core.projectdescription.IFileBasedContent;

/**
 * <p>
 * An {@link IResourceKey} implements an identifier for a resource. It contains
 * a <code>contentId</code>, a <code>root</code> directory or archive file, and
 * a <code>path</code>.
 * </p>
 * 
 * @author Gerd W&uuml;therich (gerd@gerd-wuetherich.de)
 */
public interface IResourceKey {

	/**
	 * <p>
	 * Returns the identifier of the {@link IFileBasedContent} that defines the
	 * resource.
	 * </p>
	 * 
	 * @return the identifier of the {@link IFileBasedContent} that defines the
	 *         resource.
	 */
	String getContentId();

	/**
	 * <p>
	 * Returns the root directory or archive file that contains the resource
	 * (e.g. 'c:/dev/classes.zip' or 'c:/dev/source'). Note that resource paths
	 * are always slash-delimited ('/').
	 * </p>
	 * 
	 * @return the root directory or archive file that contains the resource.
	 */
	String getRoot();

	/**
	 * <p>
	 * Returns the path of the resource. Note that resource paths are always
	 * slash-delimited ('/').
	 * </p>
	 * 
	 * @return the path of the resource.
	 */
	String getPath();

	// /**
	// * <p>
	// * </p>
	// *
	// * @return
	// */
	// String getHashValue();
}
