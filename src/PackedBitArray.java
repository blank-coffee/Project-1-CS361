public class PackedBitArray {
  private final long[] data;
  private final int m;
  public PackedBitArray(int m) { this.m = m; data = new long[(m + 63) >>> 6]; }
  public void set(int i) { data[i >>> 6] |= (1L << (i & 63)); }
  public boolean get(int i) { return (data[i >>> 6] & (1L << (i & 63))) != 0; }
  public long bytes() { return (long) data.length * 8L; }
}