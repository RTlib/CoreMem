package coremem.util;

import org.apache.commons.io.FileUtils;

import java.io.*;

public class ResourceHandler
{

    /**
     * Simple wrapper function to compare contents of two files
     */
    public boolean twoFilesAreSame(File pCopied, File pRead)
    {
        boolean result = false;
        try {
            result = FileUtils.contentEquals(pCopied, pRead);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Method to copy a .dll resource from a jar
     */
    public File copyDLLfromJarToTempFile(String pRelativePathToFileInJar) throws IOException {
        File lFile = File.createTempFile("coremem",".dll");
        lFile.deleteOnExit();

        try (
                InputStream an = getClass().getResourceAsStream(pRelativePathToFileInJar);
                OutputStream out = new BufferedOutputStream(new FileOutputStream(lFile))
        )
        {
            byte[] buffer = new byte[4096];
            for (;;) {
                int nBytes = an.read(buffer);
                if (nBytes <= 0)
                    break;
                out.write(buffer, 0 ,nBytes);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return lFile;
    }
}
