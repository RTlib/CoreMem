package coremem.util.test;

import static org.junit.Assert.*;

import coremem.util.ResourceHandler;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * ResourceHandler tests
 *
 * @author AhmetCanSolak
 */
public class ResourceHandlerTests
{
    /**
     * Private instance of {@link ResourceHandler}
     */
    private ResourceHandler mRH = new ResourceHandler();

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
        File fRead1 = new File("coremem/util/test/testartifact_jnidispatch.dll");
        File fRead2 = new File("coremem/util/test/testartifact_jnidispatch.dll");

        // Compare with two instance of same file
        assertTrue(mRH.twoFilesAreSame(fRead1,fRead2));
    }
}
