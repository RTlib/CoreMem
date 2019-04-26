package coremem.buffers;

import static java.lang.Math.min;

import java.util.ArrayDeque;

import coremem.ContiguousMemoryInterface;
import coremem.exceptions.InvalidNativeMemoryAccessException;
import coremem.interfaces.SizedInBytes;
import coremem.offheap.OffHeapMemory;
import coremem.offheap.OffHeapMemoryAccess;

/**
 * ContiguousBuffer is a more handy way to read and write to and from instances
 * of ContiguousMemoryInterface. It holds a 'position' that is automatically
 * incremented for read and writes of different primitive types. This position
 * can be moved, pushed and popped.
 * 
 * Range checks are only performed for block memory copies or sets, writing or
 * reading for single primitive types is not protected.
 * 
 * @author royer
 */
public class ContiguousBuffer implements SizedInBytes
{
  /**
   * Wrapped ContiguousMemoryInterface instance.
   */
  private final ContiguousMemoryInterface mContiguousMemoryInterface;

  /**
   * This caches the first valid and invalid positions in the contiguous buffer:
   */
  private final long mFirstValidPosition;
  private final long mFirstInvalidPosition;

  /**
   * Current position
   */
  private volatile long mPosition;

  /**
   * Queue for pushing and popping positions.
   */
  private final ArrayDeque<Long> mStack = new ArrayDeque<Long>();

  /**
   * Allocates a ContiguousBuffer (using OffHeapMemory) of given length.
   * 
   * @param pLengthInBytes
   *          length in bytes
   * @return contiguous buffer
   */
  public static ContiguousBuffer allocate(long pLengthInBytes)
  {
    final OffHeapMemory lAllocatedBytes =
                                        OffHeapMemory.allocateBytes(pLengthInBytes);
    final ContiguousBuffer lContiguousBuffer =
                                             new ContiguousBuffer(lAllocatedBytes);
    return lContiguousBuffer;
  }

  /**
   * Wraps a ContiguousMemoryInterface with a ContiguousBuffer.
   * 
   * @param pContiguousMemoryInterface
   *          contiguous memory to wrap
   * @return contiguous buffer
   */
  public static ContiguousBuffer wrap(ContiguousMemoryInterface pContiguousMemoryInterface)
  {
    return new ContiguousBuffer(pContiguousMemoryInterface);
  }

  /**
   * Constructs a ContiguousBuffer by wrapping a ContiguousMemoryInterface.
   * 
   * @param pContiguousMemoryInterface
   *          contiguous memory to wrap
   */
  public ContiguousBuffer(ContiguousMemoryInterface pContiguousMemoryInterface)
  {
    super();
    mContiguousMemoryInterface = pContiguousMemoryInterface;
    mFirstValidPosition = pContiguousMemoryInterface.getAddress();
    mPosition = mFirstValidPosition;
    mFirstInvalidPosition = mFirstValidPosition
                            + pContiguousMemoryInterface.getSizeInBytes();
  }

  /**
   * Returns the underlying ContiguousMemoryInterface.
   * 
   * @return contiguous memory
   */
  public ContiguousMemoryInterface getContiguousMemory()
  {
    return mContiguousMemoryInterface;
  }

  @Override
  public long getSizeInBytes()
  {
    return mContiguousMemoryInterface.getSizeInBytes();
  }

  /**
   * Sets the current position to a new value.
   * 
   * @param pNewPosition
   *          new position value.
   */
  public void setPosition(long pNewPosition)
  {
    mPosition = mFirstValidPosition + pNewPosition;
  }

  /**
   * Rewinds the position to the first valid position in the buffer.
   */
  public void rewind()
  {
    mPosition = mFirstValidPosition;
  }

  /**
   * Clears the position stack.
   */
  public void clearStack()
  {
    mStack.clear();
  }

  /**
   * Pushes current position to stack.
   */
  public void pushPosition()
  {
    mStack.push(mPosition);
  }

  /**
   * Pops position at the top of the stack and sets it as the new current
   * position.
   */
  public void popPosition()
  {
    mPosition = mStack.pop();
  }

  /**
   * Checks whether the current position is valid.
   * 
   * @return true if valid, false otherwise.
   */
  public boolean isPositionValid()
  {
    final long lAddress = mContiguousMemoryInterface.getAddress();
    final long lSizeInBytes =
                            mContiguousMemoryInterface.getSizeInBytes();
    return lAddress <= mPosition
           && mPosition < lAddress + lSizeInBytes;
  }

  /**
   * Returns the remaining bytes that can be read or written from the current position.
   * 
   * @return remaining bytes
   */
  public long remainingBytes()
  {
    return mFirstInvalidPosition - mPosition;
  }

  /**
   * Returns true if there are enough remaining bytes to read/write a single byte.
   * 
   * @return true if at least byte remains
   */
  public boolean hasRemainingByte()
  {
    return mPosition <= mFirstInvalidPosition - Byte.BYTES;
  }

  /**
   * Returns true if there are enough remaining bytes to read/write a single char.
   * 
   * @return true if at least char remains
   */
  public boolean hasRemainingChar()
  {
    return mPosition <= mFirstInvalidPosition - Character.BYTES;
  }

  /**
   * Returns true if there are enough remaining bytes to read/write a single short.
   * 
   * @return true if at least short remains
   */
  public boolean hasRemainingShort()
  {
    return mPosition <= mFirstInvalidPosition - Short.BYTES;
  }

  /**
   * Returns true if there are enough remaining bytes to read/write a single int.
   * 
   * @return true if at least int remains
   */
  public boolean hasRemainingInt()
  {
    return mPosition <= mFirstInvalidPosition - Integer.BYTES;
  }

  /**
   * Returns true if there are enough remaining bytes to read/write a single long.
   * 
   * @return true if at least long remains
   */
  public boolean hasRemainingLong()
  {
    return mPosition <= mFirstInvalidPosition - Long.BYTES;
  }

  /**
   * Returns true if there are enough remaining bytes to read/write a single float.
   * 
   * @return true if at least float remains
   */
  public boolean hasRemainingFloat()
  {
    return mPosition <= mFirstInvalidPosition - Float.BYTES;
  }

  /**
   * Returns true if there are enough remaining bytes to read/write a single double.
   * 
   * @return true if at least double remains
   */
  public boolean hasRemainingDouble()
  {
    return mPosition <= mFirstInvalidPosition - Double.BYTES;
  }

  /**
   * Returns true if there are pNumberOfBytes remaining bytes to read/write.
   * 
   * @param pNumberOfBytes
   *          number of bytes that would remain
   * @return true if pNumberOfBytes remain in the buffer to read.
   */
  public boolean hasRemaining(long pNumberOfBytes)
  {
    return mPosition <= mFirstInvalidPosition - pNumberOfBytes;
  }

  /**
   * Writes the entire contents of a ContiguousBuffer into this buffer. The
   * position for _both buffers- is incremented accordingly.
   * 
   * @param pContiguousBuffer
   *          buffer
   */
  public void writeContiguousBuffer(ContiguousBuffer pContiguousBuffer)
  {
    long lSizeInBytes = min(pContiguousBuffer.remainingBytes(),
                            this.remainingBytes());
    if (mPosition + lSizeInBytes > mFirstInvalidPosition)
      throw new InvalidNativeMemoryAccessException("Attemting to write past the end of this buffer");

    OffHeapMemoryAccess.copyMemory(pContiguousBuffer.mPosition,
                                   mPosition,
                                   lSizeInBytes);
    mPosition += lSizeInBytes;
    pContiguousBuffer.mPosition += lSizeInBytes;
  }

  /**
   * Writes the entire contents of a ContiguousMemoryInterface into this buffer.
   * The position is incremented accordingly.
   * 
   * @param pContiguousMemoryInterface
   *          memory
   */
  public void writeContiguousMemory(ContiguousMemoryInterface pContiguousMemoryInterface)
  {
    long lSizeInBytes = pContiguousMemoryInterface.getSizeInBytes();
    if (mPosition + lSizeInBytes > mFirstInvalidPosition)
      throw new InvalidNativeMemoryAccessException("Attemting to write past the end of this buffer");

    OffHeapMemoryAccess.copyMemory(pContiguousMemoryInterface.getAddress(),
                                   mPosition,
                                   lSizeInBytes);
    mPosition += lSizeInBytes;
  }

  /**
   * Fills the remaining space in the buffer with a given byte.
   * 
   * @param pByte
   *          byte value.
   */
  public void fillBytes(byte pByte)
  {
    writeBytes(remainingBytes(), pByte);
  }

  /**
   * Write a sequence of identical bytes into this buffer. The position is
   * incremented accordingly.
   * 
   * @param pNumberOfBytes
   *          number of bytes to write
   * @param pByte
   *          byte to write repeatedly.
   */
  public void writeBytes(long pNumberOfBytes, byte pByte)
  {
    if (mPosition + pNumberOfBytes > mFirstInvalidPosition)
      throw new InvalidNativeMemoryAccessException("Attemting to write past the end of this buffer");
    OffHeapMemoryAccess.fillMemory(mPosition, pNumberOfBytes, pByte);
    mPosition += pNumberOfBytes;
  }

  /**
   * Write a single byte. The position is incremented accordingly.
   * 
   * @param pByte
   *          value
   */
  public void writeByte(byte pByte)
  {
    OffHeapMemoryAccess.setByte(mPosition, pByte);
    mPosition += 1;
  }

  /**
   * Write a single short. The position is incremented accordingly.
   * 
   * @param pShort
   *          value
   */
  public void writeShort(short pShort)
  {
    OffHeapMemoryAccess.setShort(mPosition, pShort);
    mPosition += 2;
  }

  /**
   * Write a single char. The position is incremented accordingly.
   * 
   * @param pChar
   *          value
   */
  public void writeChar(char pChar)
  {
    OffHeapMemoryAccess.setChar(mPosition, pChar);
    mPosition += 2;
  }

  /**
   * Write a single int. The position is incremented accordingly.
   * 
   * @param pInt
   *          value
   */
  public void writeInt(int pInt)
  {
    OffHeapMemoryAccess.setInt(mPosition, pInt);
    mPosition += 4;
  }

  /**
   * Write a single long. The position is incremented accordingly.
   * 
   * @param pLong
   *          value
   */
  public void writeLong(long pLong)
  {
    OffHeapMemoryAccess.setLong(mPosition, pLong);
    mPosition += 8;
  }

  /**
   * Write a single float. The position is incremented accordingly.
   * 
   * @param pFloat
   *          value
   */
  public void writeFloat(float pFloat)
  {
    OffHeapMemoryAccess.setFloat(mPosition, pFloat);
    mPosition += 4;
  }

  /**
   * Write a single double. The position is incremented accordingly.
   * 
   * @param pDouble
   *          value
   */
  public void writeDouble(double pDouble)
  {
    OffHeapMemoryAccess.setDouble(mPosition, pDouble);
    mPosition += 8;
  }

  /**
   * Reads a single byte. The position is incremented accordingly.
   * 
   * @return value
   */
  public byte readByte()
  {
    final byte lByte = OffHeapMemoryAccess.getByte(mPosition);
    mPosition += 1;
    return lByte;
  }

  /**
   * Reads a single short. The position is incremented accordingly.
   * 
   * @return value
   */
  public short readShort()
  {
    final short lShort = OffHeapMemoryAccess.getShort(mPosition);
    mPosition += 2;
    return lShort;
  }

  /**
   * Reads a single char. The position is incremented accordingly.
   * 
   * @return value
   */
  public char readChar()
  {
    final char lChar = OffHeapMemoryAccess.getChar(mPosition);
    mPosition += 2;
    return lChar;
  }

  /**
   * Reads a single int. The position is incremented accordingly.
   * 
   * @return value
   */
  public int readInt()
  {
    final int lInt = OffHeapMemoryAccess.getInt(mPosition);
    mPosition += 4;
    return lInt;
  }

  /**
   * Reads a single long. The position is incremented accordingly.
   * 
   * @return value
   */
  public long readLong()
  {
    final long lLong = OffHeapMemoryAccess.getLong(mPosition);
    mPosition += 8;
    return lLong;
  }

  /**
   * Reads a single float. The position is incremented accordingly.
   * 
   * @return value
   */
  public float readFloat()
  {
    final float lFloat = OffHeapMemoryAccess.getFloat(mPosition);
    mPosition += 4;
    return lFloat;
  }

  /**
   * Reads a single double. The position is incremented accordingly.
   * 
   * @return value
   */
  public double readDouble()
  {
    final double lDouble = OffHeapMemoryAccess.getDouble(mPosition);
    mPosition += 8;
    return lDouble;
  }

  /**
   * Skips multiple bytes. The position is incremented accordingly.
   * 
   * @param pNumberOfBytesToSkip
   *          number of bytes to skip
   */
  public void skipBytes(long pNumberOfBytesToSkip)
  {
    mPosition += 1 * pNumberOfBytesToSkip;
  }

  /**
   * Skips multiple shorts. The position is incremented accordingly.
   * 
   * @param pNumberOfShortsToSkip
   *          number of shorts to skip
   */
  public void skipShorts(long pNumberOfShortsToSkip)
  {
    mPosition += 2 * pNumberOfShortsToSkip;
  }

  /**
   * Skips multiple chars. The position is incremented accordingly.
   * 
   * @param pNumberOfCharsToSkip
   *          number of chars to skip
   */
  public void skipChars(long pNumberOfCharsToSkip)
  {
    mPosition += 2 * pNumberOfCharsToSkip;
  }

  /**
   * Skips multiple ints. The position is incremented accordingly.
   * 
   * @param pNumberofIntsToSkip
   *          number of ints to skip
   */
  public void skipInts(long pNumberofIntsToSkip)
  {
    mPosition += 4 * pNumberofIntsToSkip;
  }

  /**
   * Skips multiple longs. The position is incremented accordingly.
   * 
   * @param pNumberOfLongsToSkip
   *          number of longs to skip
   */
  public void skipLongs(long pNumberOfLongsToSkip)
  {
    mPosition += 8 * pNumberOfLongsToSkip;
  }

  /**
   * Skips multiple floats. The position is incremented accordingly.
   * 
   * @param pNumberOfFloatsToSkip
   *          number of floats to skip
   */
  public void skipFloats(long pNumberOfFloatsToSkip)
  {
    mPosition += 4 * pNumberOfFloatsToSkip;
  }

  /**
   * Skips multiple doubles. The position is incremented accordingly.
   * 
   * @param pNumberOfDoublesToSkip
   *          number of doubles to skip
   */
  public void skipDoubles(long pNumberOfDoublesToSkip)
  {
    mPosition += 8 * pNumberOfDoublesToSkip;
  }

  @Override
  public String toString()
  {
    return String.format("ContiguousBuffer [mContiguousMemoryInterface=%s, mFirstValidPosition=%s, mFirstInvalidPosition=%s, mPosition=%s, mStack=%s]",
                         mContiguousMemoryInterface,
                         mFirstValidPosition,
                         mFirstInvalidPosition,
                         mPosition,
                         mStack);
  }

}
