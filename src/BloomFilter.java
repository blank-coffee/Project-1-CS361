import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.BitSet;

public class BloomFilter {
  private final BitSet bits;
  private final int m;
  private final int k;

  public BloomFilter(int m, int k) {
    this.m = m;
    this.k = k;
    this.bits = new BitSet(m);
  }

  // Compute two 64-bit base hashes from the hex SHA string using SHA-256
  private long[] baseHashes(String hexSha) {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      byte[] bytes = hexToBytes(hexSha.trim());
      byte[] digest = md.digest(bytes);
      long h1 = bytesToLong(digest, 0);
      long h2 = bytesToLong(digest, 8);
      // Ensure non-negative values
      h1 = h1 & 0x7fffffffffffffffL;
      h2 = h2 & 0x7fffffffffffffffL;
      return new long[]{h1, h2};
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
  }

  private static long bytesToLong(byte[] b, int offset) {
    long v = 0;
    for (int i = 0; i < 8; i++) {
      v = (v << 8) | (b[offset + i] & 0xffL);
    }
    return v;
  }

  private static byte[] hexToBytes(String s) {
    int len = s.length();
    if (len % 2 != 0) throw new IllegalArgumentException("Invalid hex string");
    byte[] data = new byte[len / 2];
    for (int i = 0; i < len; i += 2) {
      data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                           + Character.digit(s.charAt(i + 1), 16));
    }
    return data;
  }

  public void insert(String hexSha) {
    long[] h = baseHashes(hexSha);
    long h1 = h[0], h2 = h[1];
    for (int i = 0; i < k; i++) {
      long combined = h1 + (long) i * h2;
      int idx = (int) Math.floorMod(combined, m);
      bits.set(idx);
    }
  }

  public boolean query(String hexSha) {
    long[] h = baseHashes(hexSha);
    long h1 = h[0], h2 = h[1];
    for (int i = 0; i < k; i++) {
      long combined = h1 + (long) i * h2;
      int idx = (int) Math.floorMod(combined, m);
      if (!bits.get(idx)) return false;
    }
    return true;
  }
  // inside BloomFilter
  public int[] getIndicesForDebug(String hexSha) {
    long[] h = baseHashes(hexSha);
    int[] idx = new int[k];
    for (int i = 0; i < k; i++) idx[i] = (int) Math.floorMod(h[0] + (long)i * h[1], m);
    return idx;
  }


  public int bitSize() { return m; }

  public long approximateMemoryBytes() {
    // BitSet uses long[] internally; approximate as (bits.length()/8)
    return (long) Math.ceil((double) m / 8.0);
  }
}