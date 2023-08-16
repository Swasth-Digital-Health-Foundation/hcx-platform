package org.swasth.common.utils;

import org.junit.Assert;
import org.junit.Test;

public class SlugUtilsTest {

    @Test
    public void testMakeSlug() {
        String sluggified =  SlugUtils.makeSlug("settle" , "hosp", "012345","_", "swasth-hcx");
        Assert.assertEquals("hosp_settle_012345@swasth-hcx", sluggified);
    }

}
