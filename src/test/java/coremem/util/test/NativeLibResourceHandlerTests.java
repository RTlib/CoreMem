package coremem.util.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import coremem.test.ClassScope;
import coremem.util.NativeLibResourceHandler;

import org.junit.Test;

/**
 * NativeLibResourceHandler tests
 *
 * @author AhmetCanSolak
 */
public class NativeLibResourceHandlerTests
{
  /**
   * Private instance of {@link NativeLibResourceHandler}
   */
  private NativeLibResourceHandler mRH =
                                       new NativeLibResourceHandler();

  /**
   * Test for checking if given two files has identically same content or not
   */
  @Test
  public void testCopyAndLoadDLLFromJar()
  {
    if (!System.getProperty("os.name").contains("Windows"))
      return;

    File fCopied = null;
    fCopied =
            mRH.loadResourceFromJar(NativeLibResourceHandlerTests.class,
                                    "/com/sun/jna/win32-x86-64/jnidispatch.dll");
    try
    {
      System.out.println("For debug: " + fCopied.getCanonicalPath());

      // Get the both system and current classloaders
      ClassLoader appLoader = ClassLoader.getSystemClassLoader();
      ClassLoader currentLoader =
                                NativeLibResourceHandlerTests.class.getClassLoader();

      // Get the list of loaded libraries
      ClassLoader[] loaders = new ClassLoader[]
      { appLoader, currentLoader };
      final String[] libraries =
                               ClassScope.getLoadedLibraries(loaders);

      // Check if it is loaded
      assertTrue(Arrays.stream(libraries)
                       .anyMatch(fCopied.getCanonicalPath()::equals));
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
  }

  /**
   * Test for copying file from loaded Jar to given destination
   */
  @Test
  public void testDLLfromJar()
  {
    if (!System.getProperty("os.name").contains("Windows"))
      return;

    // Copy the file from JAR
    File fCopied = null;
    try
    {
      fCopied =
              mRH.copyResourceFromJarToTempFile(NativeLibResourceHandlerTests.class,
                                                "/com/sun/jna/win32-x86-64/jnidispatch.dll");
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }

    // Get reference file
    Path path =
              Paths.get("src/test/java/coremem/util/test/testartifact_jnidispatch.dll");
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
    if (!System.getProperty("os.name").contains("Windows"))
      return;

    Path path =
              Paths.get("src/test/java/coremem/util/test/testartifact_jnidispatch.dll");
    File fRead1 = new File(path.toAbsolutePath().toString());
    File fRead2 = new File(path.toAbsolutePath().toString());

    // Compare with two instance of same file
    assertTrue(mRH.twoFilesAreSame(fRead1, fRead2));
  }
}
