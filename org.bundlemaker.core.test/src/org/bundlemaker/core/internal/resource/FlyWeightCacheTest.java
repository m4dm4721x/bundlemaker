package org.bundlemaker.core.internal.resource;

import junit.framework.Assert;

import org.bundlemaker.core.internal.resource.FlyWeightCache;
import org.bundlemaker.core.internal.resource.Reference;
import org.bundlemaker.core.resource.ReferenceType;
import org.bundlemaker.core.resource.modifiable.ReferenceAttributes;
import org.junit.Test;

public class FlyWeightCacheTest {

  public static final int TIMEOUT = 750;

  /**
   * <p>
   * OK
   * </p>
   */
  @Test(timeout = TIMEOUT)
  public void testGetFlyWeightString() {

    //
    FlyWeightCache cache = new FlyWeightCache();

    //
    for (int i = 0; i < 100000; i++) {

      //
      FlyWeightString flyWeightString_1 = cache.getFlyWeightString("abc" + i);
      //
      FlyWeightString flyWeightString_2 = cache.getFlyWeightString("abc");
    }

    //
    Assert.assertEquals(100001, cache._flyWeightStrings.size());
  }

  /**
   * <p>
   * </p>
   */
  @Test(timeout = TIMEOUT)
  public void testGetReferenceAttributes() {

    //
    FlyWeightCache cache = new FlyWeightCache();

    //
    for (int i = 0; i < 100000; i++) {

      //
      ReferenceAttributes attributes = cache.getReferenceAttributes(new ReferenceAttributes(
          i % 11 == 0 ? ReferenceType.TYPE_REFERENCE : ReferenceType.PACKAGE_REFERENCE, i % 2 == 0, i % 3 == 0,
          i % 5 == 0, i % 7 == 0, i % 11 == 0, i % 13 == 0, i % 17 == 0));
    }

    //
    Assert.assertEquals(32, cache._referenceAttributesCache.size());
  }

  @Test(timeout = 9000)
  public void testGetReference() {

    //
    FlyWeightCache cache = new FlyWeightCache();

    //
    for (int i = 0; i < 1000000; i++) {

      //
      Reference reference = cache.getReference("fullyQualifiedName" + i, new ReferenceAttributes(
          i % 11 == 0 ? ReferenceType.TYPE_REFERENCE : ReferenceType.PACKAGE_REFERENCE, i % 2 == 0, i % 3 == 0,
          i % 5 == 0, i % 7 == 0, i % 11 == 0, i % 13 == 0, i % 17 == 0));

      reference = cache.getReference("fullyQualifiedName", new ReferenceAttributes(
          i % 11 == 0 ? ReferenceType.TYPE_REFERENCE : ReferenceType.PACKAGE_REFERENCE, i % 2 == 0, i % 3 == 0,
          i % 5 == 0, i % 7 == 0, i % 11 == 0, i % 13 == 0, i % 17 == 0));
    }

    //
    Assert.assertEquals(1000032, cache._referenceCache.size());
  }
}
