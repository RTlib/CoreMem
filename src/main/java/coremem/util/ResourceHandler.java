package coremem.util;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;


public class ResourceHandler
{

    /**
     * Simple wrapper function to compare contents of two files
     */
    public boolean twoFilesAreSame(File pCopied, File pRead)
    {
        // Check if both files exist
        if (!pCopied.exists() || !pRead.exists())
            return false;

        // Both file's that are passed are to be of file type and not directory.
        if (!pCopied.isFile() || !pRead.isFile())
            return false;

        if (pCopied.length() != pRead.length())
            return false;

        // Then compare the contents.
        try (
                InputStream is1 = new FileInputStream(pCopied);
                InputStream is2 = new FileInputStream(pRead)
        )
        {
            // Compare byte-by-byte - if performance issues occur solution is buffering
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
     * Method to copy a .dll resource from a jar
     */
    public File copyDLLfromJarToTempFile(String pRelativePathToFileInJar) throws IOException
    {
        String lFullFileName = new File(pRelativePathToFileInJar).getName();
        int lIndex = lFullFileName.lastIndexOf('.');
        String lFileName = lFullFileName.substring(0, lIndex);
        String lFileExtension = lFullFileName.substring(lIndex);

        File lFile = File.createTempFile(lFileName, lFileExtension);
        lFile.deleteOnExit();

        try (
                InputStream an = getClass().getResourceAsStream(pRelativePathToFileInJar)
        )
        {
            Files.copy(an, lFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return lFile;
    }

    /**
     * Method to load a .dll resource from a jar after copying into a TempFile
     */
    public File loadDLLFromJar(String pRelativePathToFileInJar) {
        File lResultFile = null;
        try {
            lResultFile = this.copyDLLfromJarToTempFile(pRelativePathToFileInJar);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.load(lResultFile.getAbsolutePath());
        return lResultFile;
    }
}
