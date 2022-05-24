package org.swasth.common.utils;

import org.junit.Assert;
import org.junit.Test;

public class SlugUtilsTest {

    @Test
    public void testMakeSlug() {
        String sluggified =  SlugUtils.makeSlug("-provider--apollo hospital-");
        Assert.assertEquals("provider-apollo-hospital", sluggified);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMakeSlugException() throws Exception {
        SlugUtils.makeSlug(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMakeSlugInvalidOutput() throws Exception {
        SlugUtils.makeSlug("--");
    }

}
