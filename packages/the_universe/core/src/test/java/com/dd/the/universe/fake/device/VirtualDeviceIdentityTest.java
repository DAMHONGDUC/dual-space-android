package com.dd.the.universe.fake.device;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.Random;

import com.dd.the.universe.utils.Md5Utils;

import org.junit.Test;

public class VirtualDeviceIdentityTest {
    @Test
    public void legacyIdKeepsTheFormulaExistingCopiesWereCreatedWith() {
        String expected = Md5Utils.md5("bbb_default_1").substring(0, 16);

        assertEquals(expected, VirtualDeviceIdentity.legacyAndroidId(1));
        assertEquals(expected, VirtualDeviceIdentity.initialAndroidId(1, true, new Random(7)));
    }

    @Test
    public void newUsersGetRandomIdsInsteadOfTheSharedDerivedOne() {
        String first = VirtualDeviceIdentity.initialAndroidId(1, false, new Random(1));
        String second = VirtualDeviceIdentity.initialAndroidId(1, false, new Random(2));

        assertTrue(VirtualDeviceIdentity.isValidAndroidId(first));
        assertNotEquals(VirtualDeviceIdentity.legacyAndroidId(1), first);
        assertNotEquals(first, second);
    }

    @Test
    public void validationRejectsMalformedIds() {
        assertFalse(VirtualDeviceIdentity.isValidAndroidId(null));
        assertFalse(VirtualDeviceIdentity.isValidAndroidId(""));
        assertFalse(VirtualDeviceIdentity.isValidAndroidId("xyz0000000000000"));
        assertTrue(VirtualDeviceIdentity.isValidAndroidId("0123456789ABCDEF"));
    }
}
