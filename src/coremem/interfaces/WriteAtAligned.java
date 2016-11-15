package coremem.interfaces;

/**
 * Memory objects implementing this interface provide methods to write single
 * primitive types. Offsets are aligned to the written type.
 * 
 * @author royer
 */
public interface WriteAtAligned extends MemoryTyped
{
  /**
   * Writes a value at a given offset. The offset unit is 1 byte.
   * 
   * @param pOffset
   *          offset
   * @return value
   */
  public void setByteAligned(final long pOffset, final byte pValue);

  /**
   * Writes a value at a given offset. The offset unit is 1 byte.
   * 
   * @param pOffset
   *          offset
   * @return value
   */
  public void setCharAligned(final long pOffset, final char pValue);

  /**
   * Writes a value at a given offset. The offset unit is 1 byte.
   * 
   * @param pOffset
   *          offset
   * @return value
   */
  public void setShortAligned(final long pOffset, final short pValue);

  /**
   * Writes a value at a given offset. The offset unit is 1 byte.
   * 
   * @param pOffset
   *          offset
   * @return value
   */
  public void setIntAligned(final long pOffset, final int pValue);

  /**
   * Writes a value at a given offset. The offset unit is 1 byte.
   * 
   * @param pOffset
   *          offset
   * @return value
   */
  public void setLongAligned(final long pOffset, final long pValue);

  /**
   * Writes a value at a given offset. The offset unit is 1 byte.
   * 
   * @param pOffset
   *          offset
   * @return value
   */
  public void setFloatAligned(final long pOffset, final float pValue);

  /**
   * Writes a value at a given offset. The offset unit is 1 byte.
   * 
   * @param pOffset
   *          offset
   * @return value
   */
  public void setDoubleAligned(final long pOffset,
                               final double pValue);

}
