package net.haesleinhuepf.clij.coremem.memmap;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;

import net.haesleinhuepf.clij.coremem.exceptions.MemoryMapFileException;
import net.haesleinhuepf.clij.coremem.memmap.MemoryMappedFileAccessMode;
import net.haesleinhuepf.clij.coremem.offheap.OffHeapMemoryAccess;

/**
 *
 *
 * @author royer
 */
public final class MemoryMappedFileUtils
{
  static final long cAllocationGranularity = 65536;
  static final long cPageSize = 4096;

  private static final ByteBuffer cZeroBuffer =
                                              ByteBuffer.allocateDirect(1);

  /**
   * Maps a region of a file.
   * 
   * @param pFileChannel
   *          file channel
   * @param pAccessMode
   *          access mode
   * @param pFilePosition
   *          file position in bytes
   * @param pMappedRegionLength
   *          mapped region length
   * @param pExtendIfNeeded
   *          extend file length if needed (file not as long as request mapping
   *          length).
   * @return mapped address
   * @throws MemoryMapFileException
   *           thrown if problem occurs while mapping.
   */
  public static final long map(FileChannel pFileChannel,
                               MemoryMappedFileAccessMode pAccessMode,
                               final long pFilePosition,
                               final long pMappedRegionLength,
                               final boolean pExtendIfNeeded) throws MemoryMapFileException
  {
    Method lMemoryMapMethod;
    long lMappedAddress;

    try
    {
      if (!pFileChannel.isOpen())
        throw new ClosedChannelException();

      if ((pFilePosition % cPageSize) != 0)
        throw new IllegalArgumentException("File position must be page aligned (4096 byte boundaries)");
      if (pFilePosition < 0L)
        throw new IllegalArgumentException("Negative position");
      if (pMappedRegionLength < 0L)
        throw new IllegalArgumentException("Negative size");
      if (pFilePosition + pMappedRegionLength < 0L)
        throw new IllegalArgumentException("Position + size overflow");
      if (pMappedRegionLength > Long.MAX_VALUE)
        throw new IllegalArgumentException("Size exceeds Long.MAX_VALUE");

      if (pExtendIfNeeded)
      {
        if (pFileChannel.size() < pFilePosition + pMappedRegionLength)
        {
          final long lCurrentPosition = pFileChannel.position();
          pFileChannel.position(pFilePosition + pMappedRegionLength
                                - 1);
          // The following ensures that the file has the size requested in the
          // mapping
          cZeroBuffer.clear();
          pFileChannel.write(cZeroBuffer);
          pFileChannel.force(false);

          pFileChannel.position(lCurrentPosition);
        }
      }

      lMemoryMapMethod = pFileChannel.getClass()
                                     .getDeclaredMethod("map0",
                                                        Integer.TYPE,
                                                        Long.TYPE,
                                                        Long.TYPE);
      lMemoryMapMethod.setAccessible(true);
      final Object lReturnValue =
                                lMemoryMapMethod.invoke(pFileChannel,
                                                        pAccessMode.getValue(),
                                                        pFilePosition,
                                                        pMappedRegionLength);

      final Long lAddressAsLong = (Long) lReturnValue;

      final long lAddress = lAddressAsLong.longValue();
      net.haesleinhuepf.clij.coremem.offheap.OffHeapMemoryAccess.registerMemoryRegion(lAddress,
                                               pMappedRegionLength);

      lMappedAddress = lAddressAsLong;

    }
    catch (final Throwable e)
    {
      final String lErrorMessage =
                                 String.format("Cannot memory map file: %s at file position %d with length %d (%s)",
                                               pFileChannel.toString(),
                                               pFilePosition,
                                               pMappedRegionLength,
                                               e.getLocalizedMessage() != null ? e.getLocalizedMessage()
                                                                               : e.getCause()
                                                                                  .getLocalizedMessage());
      new MemoryMappedFileUtils().error("Native", lErrorMessage);
      throw new MemoryMapFileException(lErrorMessage, e);
    }

    return lMappedAddress;
  }

  /**
   * Unmaps memory mapped file.
   * 
   * @param pFileChannel
   *          file channel
   * @param pMemoryMapAddress
   *          mapped address
   * @param pMappedRegionLength
   *          mapped region length
   * @return return code
   * @throws MemoryMapFileException
   *           thrown if problem occurs while unmapping
   */
  public static final int unmap(FileChannel pFileChannel,
                                final long pMemoryMapAddress,
                                final long pMappedRegionLength) throws MemoryMapFileException
  {
    int lIntReturnValue = 0;
    try
    {

      final Method lMemoryUnMapMethod =
                                      pFileChannel.getClass()
                                                  .getDeclaredMethod("unmap0",
                                                                     Long.TYPE,
                                                                     Long.TYPE);
      lMemoryUnMapMethod.setAccessible(true);
      final Object lReturnValue =
                                lMemoryUnMapMethod.invoke(null,
                                                          pMemoryMapAddress,
                                                          pMappedRegionLength);

      OffHeapMemoryAccess.deregisterMemoryRegion(pMemoryMapAddress);

      final Integer lReturnAsInteger = (Integer) lReturnValue;

      lIntReturnValue = lReturnAsInteger.intValue();
    }
    catch (final Throwable e)
    {
      final String lErrorMessage =
                                 String.format("Cannot unmap memory at address %d with length %d (%s)",
                                               pMemoryMapAddress,
                                               pMappedRegionLength,
                                               e.getLocalizedMessage() != null ? e.getLocalizedMessage()
                                                                               : e.getCause()
                                                                                  .getLocalizedMessage());
      // e.printStackTrace();
      new MemoryMappedFileUtils().error("Native", lErrorMessage);
      throw new MemoryMapFileException(lErrorMessage, e);
    }

    return lIntReturnValue;
  }

  /*public static final void force(	final FileChannel pFileChannel,
  																boolean pFlushFileMetadataToo) throws IOException
  {
  	try
  	{
  		pFileChannel.force(pFlushFileMetadataToo);
  	}
  	catch (Throwable e)
  	{
  		String lErrorMessage = String.format(	"Cannot flush file %s contents (flushing metadata = %s, exception: %s)",
  																					pFileChannel,
  																					pFlushFileMetadataToo	? "true"
  																																: "false",
  																					e.getMessage());
  		new MemoryMappedFile().error("Native", lErrorMessage);
  		throw new IOException(e);
  	}
  
  }/**/

  /**
   * Returns the file size for a given file channel
   * 
   * @param pFileChannel
   *          file channel
   * @return file size in bytes
   * @throws IOException
   *           exception
   */
  public static final long filesize(FileChannel pFileChannel) throws IOException
  {
    return pFileChannel.size();
  }

  /**
   * Truncates file for given file channel
   * 
   * @param pFileChannel
   *          file channel
   * @param pLength
   *          truncation length
   * @throws IOException
   *           thrown if problem while truncating file
   */
  public static final void truncate(FileChannel pFileChannel,
                                    final long pLength) throws IOException
  {
    try
    {
      pFileChannel.truncate(pLength);
    }
    catch (final Throwable e)
    {
      final String lErrorMessage =
                                 String.format("Cannot truncate file %s at length %d (%s)",
                                               pFileChannel,
                                               pLength,
                                               e.getLocalizedMessage() != null ? e.getLocalizedMessage()
                                                                               : e.getCause()
                                                                                  .getLocalizedMessage());
      new MemoryMappedFileUtils().error("Native", lErrorMessage);
      throw new IOException(e);
    }

  }

  /**
   * Determines best access mode gigen standard open options.
   * 
   * @param pStandardOpenOption
   *          standard open options
   * @return best access mode
   */
  public static MemoryMappedFileAccessMode bestMode(StandardOpenOption[] pStandardOpenOption)
  {
    boolean lWrite = false;
    boolean lRead = false;

    for (final StandardOpenOption lStandardOpenOption : pStandardOpenOption)
    {
      lWrite |= lStandardOpenOption == StandardOpenOption.CREATE;
      lWrite |= lStandardOpenOption == StandardOpenOption.CREATE_NEW;
      lWrite |= lStandardOpenOption == StandardOpenOption.WRITE;
      lWrite |= lStandardOpenOption == StandardOpenOption.APPEND;
      lWrite |=
             lStandardOpenOption == StandardOpenOption.DELETE_ON_CLOSE;
      lWrite |= lStandardOpenOption == StandardOpenOption.SYNC;
      lRead |= lStandardOpenOption == StandardOpenOption.READ;
    }

    if (lWrite)
      return MemoryMappedFileAccessMode.ReadWrite;

    if (lRead)
      return MemoryMappedFileAccessMode.ReadOnly;

    return MemoryMappedFileAccessMode.ReadWrite;
  }

  private void error(String string, String lErrorMessage)
  {

  }

}
