package coremem.util;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class NativeLibResourceHandler
{

  /**
   * Simple wrapper function to compare contents of two files
   *
   * @param pCopied
   * @param pRead
   * @return :result of comparison as boolean
   */
  public boolean twoFilesAreSame(File pCopied, File pRead)
  {
    // Check if both files exist
    if (!pCopied.exists() || !pRead.exists())
      return false;

    // Both file's that are passed are to be of file type and not directory.
    if (!pCopied.isFile() || !pRead.isFile())
      return false;

    // Also they need to have the same length
    if (pCopied.length() != pRead.length())
      return false;

    // Then compare the contents.
    try (InputStream is1 = new FileInputStream(pCopied);
        InputStream is2 = new FileInputStream(pRead))
    {
      // Compare byte-by-byte - if performance issues occur solution is
      // buffering
      int data;
      while ((data = is1.read()) != -1)
        if (data != is2.read())
          return false;
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }

    return true;
  }

  /**
   * Method to copy a .dll resource from a jar into a TempFile
   *
   * @param pClass
   * @param pRelativePathToFileInJar
   * @return lFile :copied resource file
   * @throws IOException
   */
  public File copyResourceFromJarToTempFile(Class pClass,
                                            String pRelativePathToFileInJar) throws IOException
  {
    String lFullFileName =
                         new File(pRelativePathToFileInJar).getName();
    int lIndex = lFullFileName.lastIndexOf('.');
    String lFileName = lFullFileName.substring(0, lIndex);
    String lFileExtension = lFullFileName.substring(lIndex);

    File lFile = File.createTempFile(lFileName, lFileExtension);
    lFile.deleteOnExit();

    try (
        InputStream an =
                       pClass.getResourceAsStream(pRelativePathToFileInJar))
    {
      Files.copy(an,
                 lFile.toPath(),
                 StandardCopyOption.REPLACE_EXISTING);
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }

    return lFile;
  }

  /**
   * Method to load a .dll resource from a jar after copying into a TempFile
   *
   * @param pClass
   * @param pRelativePathToFileInJar
   * @return lResultFile :loaded resource file
   */
  public File loadResourceFromJar(Class pClass,
                                  String pRelativePathToFileInJar)
  {
    File lResultFile = null;
    try
    {
      lResultFile =
                  this.copyResourceFromJarToTempFile(pClass,
                                                     pRelativePathToFileInJar);
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }

    System.load(lResultFile.getAbsolutePath());

    return lResultFile;
  }
}
