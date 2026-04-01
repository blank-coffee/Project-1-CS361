import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class BloomFilterPacked {
  private final PackedBitArray bits;
  private final int m;
  private final int k;

  public BloomFilterPacked(int m, int k) {
    this.m = m;
    this.k = k;
    this.bits = new PackedBitArray(m);
  }

  // same baseHashes as before (SHA-256 split)
  private long[] baseHashes(String hexSha) {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      byte[] bytes = hexToBytes(hexSha.trim());
      byte[] digest = md.digest(bytes);
      long h1 = bytesToLong(digest, 0) & 0x7fffffffffffffffL;
      long h2 = bytesToLong(digest, 8) & 0x7fffffffffffffffL;
      return new long[]{h1, h2};
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
  }

  private static long bytesToLong(byte[] b, int offset) {
    long v = 0;
    for (int i = 0; i < 8; i++) v = (v << 8) | (b[offset + i] & 0xffL);
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

  public int[] getIndicesForDebug(String hexSha) {
    long[] h = baseHashes(hexSha);
    int[] idx = new int[k];
    for (int i = 0; i < k; i++) idx[i] = (int) Math.floorMod(h[0] + (long) i * h[1], m);
    return idx;
  }

  public long approximateMemoryBytes() { return bits.bytes(); }

}