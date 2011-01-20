package org.bundlemaker.core.model.projectdescription;

import org.bundlemaker.core.projectdescription.FileBasedContent;
import org.bundlemaker.core.projectdescription.ResourceContent;
import org.eclipse.core.runtime.Path;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * <p>
 * </p>
 * 
 * @author Gerd W&uuml;therich (gerd@gerd-wuetherich.de)
 */
public class FileBasedContentTest {

	/**
	 * <p>
	 * </p>
	 */
	@Test
	public void test() {

		//
		FileBasedContent fileBasedContent = new FileBasedContent();
		fileBasedContent.setResourceContent(new ResourceContent());

		//
		fileBasedContent.setId("123");
		
		fileBasedContent
				.getModifiableBinaryPaths()
				.add(new Path(System.getProperty("user.dir") + "/../target.platform/bundlor-1.0.0.RELEASE/com.springsource.bundlor-1.0.0.RELEASE.jar"));

		//
		fileBasedContent
				.getModifiableResourceContent()
				.getModifiableSourcePaths()
				.add(new Path(System.getProperty("user.dir") + "/../target.platform/bundlor-1.0.0.RELEASE/com.springsource.bundlor-sources-1.0.0.RELEASE.jar"));

		//
		fileBasedContent.initialize(null);

		//
		assertEquals(1, fileBasedContent.getBinaryPaths().size());
		assertEquals(1, fileBasedContent.getResourceContent().getSourcePaths()
				.size());

		//
		assertEquals(98, fileBasedContent.getResourceContent()
				.getBinaryResources().size());

		assertEquals(85, fileBasedContent.getResourceContent()
				.getSourceResources().size());

		//
		assertEquals(
				"com/springsource/bundlor/ClassPath.class",
				fileBasedContent
						.getResourceContent()
						.getBinaryResource(
								new Path(
										"com/springsource/bundlor/ClassPath.class"))
						.getPath());

		//
		assertEquals(
				"com/springsource/bundlor/ClassPath.java",
				fileBasedContent
						.getResourceContent()
						.getSourceResource(
								new Path(
										"com/springsource/bundlor/ClassPath.java"))
						.getPath());
	}
}
