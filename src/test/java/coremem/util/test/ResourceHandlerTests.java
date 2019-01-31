package coremem.util.test;

import static org.junit.Assert.*;

import coremem.util.ResourceHandler;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Vector;
import java.util.List;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.LinkedList;


/**
 * ResourceHandler tests
 *
 * @author AhmetCanSolak
 */
public class ResourceHandlerTests
{
    private static java.lang.reflect.Field LIBRARIES;

    static {
        try {
            LIBRARIES = ClassLoader.class.getDeclaredField("loadedLibraryNames");
            LIBRARIES.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    /**
     * Private instance of {@link ResourceHandler}
     */
    private ResourceHandler mRH = new ResourceHandler();

    /**
     * Test for checking if given two files has identically same content or not
     */
    @Test
    public void testLoadDLLFromJar()
    {
        File fCopied = null;
        try {
            fCopied = mRH.copyDLLfromJarToTempFile("/com/sun/jna/sunos-x86-64/libjnidispatch.so");
            System.load(fCopied.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }

        ClassLoader appLoader = ClassLoader.getSystemClassLoader();
        ClassLoader currentLoader = ResourceHandlerTests.class.getClassLoader();

        ClassLoader[] loaders = new ClassLoader[] { appLoader, currentLoader };
        final String[] libraries = ClassScope.getLoadedLibraries(loaders);
        for (String library : libraries) {
            System.out.println(library);
        }



        Path path = Paths.get("src/test/java/coremem/util/test/testartifact_jnidispatch.dll");
        File fRead1 = new File(path.toAbsolutePath().toString());
        File fRead2 = new File(path.toAbsolutePath().toString());

        // Compare with two instance of same file
        assertTrue(mRH.twoFilesAreSame(fRead1,fRead2));
    }

    /**
     * Test for copying file from loaded Jar to given destination
     */
    @Test
    public void testDLLfromJar ()
    {
        // Copy the file from JAR
        File fCopied = null;
        try {
            fCopied = mRH.copyDLLfromJarToTempFile("/com/sun/jna/win32-x86-64/jnidispatch.dll");
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Get reference file
        Path path = Paths.get("src/test/java/coremem/util/test/testartifact_jnidispatch.dll");
        File fRead = new File(path.toAbsolutePath().toString());

        // Check if they have same content
        assertTrue(mRH.twoFilesAreSame(fCopied, fRead));
    }

    /**
     * Test for checking if given two files has identically same content or not
     */
    @Test
    public void testTwoFilesAreSame()
    {
        Path path = Paths.get("src/test/java/coremem/util/test/testartifact_jnidispatch.dll");
        File fRead1 = new File(path.toAbsolutePath().toString());
        File fRead2 = new File(path.toAbsolutePath().toString());

        // Compare with two instance of same file
        assertTrue(mRH.twoFilesAreSame(fRead1,fRead2));
    }
}

