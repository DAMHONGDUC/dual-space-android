package com.dd.the.universe.utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class BzFileUtilsTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void deleteDirFullyReportsCompleteRemoval() throws IOException {
        File root = folder.newFolder("data");
        assertTrue(new File(root, "nested").mkdirs());
        assertTrue(new File(root, "nested/file.txt").createNewFile());

        assertTrue(BzFileUtils.deleteDirFully(root));
        assertFalse(root.exists());
    }

    @Test
    public void deleteDirFullyReportsLeftovers() throws IOException {
        File root = folder.newFolder("locked");
        File child = new File(root, "file.txt");
        assertTrue(child.createNewFile());
        assertTrue(root.setWritable(false));
        try {
            assertFalse(BzFileUtils.deleteDirFully(root));
            assertTrue(child.exists());
        } finally {
            root.setWritable(true);
        }
    }

    @Test
    public void deleteDirFullyTreatsMissingPathAsRemoved() {
        assertTrue(BzFileUtils.deleteDirFully(new File(folder.getRoot(), "missing")));
    }
}
