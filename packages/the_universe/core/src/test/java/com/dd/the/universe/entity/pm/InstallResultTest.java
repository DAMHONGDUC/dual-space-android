package com.dd.the.universe.entity.pm;

import org.junit.Test;

import static org.junit.Assert.assertFalse;

public class InstallResultTest {
    @Test
    public void uncommittedInstallIsNotSuccessful() {
        assertFalse(new InstallResult().success);
    }
}
