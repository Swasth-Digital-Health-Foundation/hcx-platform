package org.swasth.common.utils;

import org.junit.Assert;
import org.junit.Test;

public class SlugUtilsTest {

    @Test
    public void testMakeSlug() {
        String sluggified =  SlugUtils.makeSlug("provider apollo hospital");
        Assert.assertEquals("provider-apollo-hospital", sluggified);
    }

}
