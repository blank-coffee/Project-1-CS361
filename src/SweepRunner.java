import java.io.*;
import java.util.*;
import java.nio.file.*;

public class SweepRunner {
  // reuse loader and computeM/computeK
  public static List<String> loadHashes(String path) throws IOException {
    List<String> lines = Files.readAllLines(Paths.get(path));
    List<String> cleaned = new ArrayList<>();
    for (String l : lines) {
      String s = l.trim();
      if (s.isEmpty()) continue;
      if (s.contains(",")) s = s.split(",")[0].trim();
      cleaned.add(s);
    }
    return cleaned;
  }

  public static int computeM(int n, double p) {
    double m = - (n * Math.log(p)) / (Math.pow(Math.log(2), 2));
    return (int) Math.ceil(m);
  }
  public static int computeK(int n, int m) {
    double k = (m / (double) n) * Math.log(2);
    return (int) Math.max(1, Math.round(k));
  }

  public static void main(String[] args) throws Exception {
    if (args.length < 1) {
      System.out.println("Usage: java SweepRunner <hashes.txt>");
      return;
    }
    String path = args[0];
    List<String> all = loadHashes(path);
    int n = 900_000;
    if (n > all.size()) n = all.size();

    List<Double> pList = Arrays.asList(0.001, 0.01, 0.05);
    List<Integer> kOffsets = Arrays.asList(-2, 0, 2);
    String csvFile = "sweep_results.csv";

    boolean csvExists = Files.exists(Paths.get(csvFile));
    try (PrintWriter csv = new PrintWriter(new FileWriter(csvFile, true))) {
      if (!csvExists) csv.println("p,m,k,n,fp_rate,avg_query_us,insert_us_per,mem_delta_MB,approx_bits_bytes");
      for (double p : pList) {
        int m = computeM(n, p);
        int kBase = computeK(n, m);
        for (int off : kOffsets) {
          int k = Math.max(1, kBase + off);
          System.out.printf("Running p=%.5f m=%d k=%d n=%d%n", p, m, k, n);

          // Use packed Bloom filter for sweep (memory close to theoretical)
          BloomFilterPacked bf = new BloomFilterPacked(m, k);

          // measure memory before/after in this JVM (for rough number)
          Runtime rt = Runtime.getRuntime();
          System.gc(); Thread.sleep(200);
          long memBefore = rt.totalMemory() - rt.freeMemory();

          long t0 = System.nanoTime();
          for (int i = 0; i < n; i++) bf.insert(all.get(i));
          long t1 = System.nanoTime();
          double insertUs = (t1 - t0) / 1e3 / (double) n;

          System.gc(); Thread.sleep(200);
          long memAfter = rt.totalMemory() - rt.freeMemory();
          double memDeltaMB = (memAfter - memBefore) / 1024.0 / 1024.0;

          // negative test: next 100k or as many as available
          int negN = Math.min(100_000, all.size() - n);
          int fp = 0;
          long tq0 = System.nanoTime();
          for (int i = 0; i < negN; i++) if (bf.query(all.get(n + i))) fp++;
          long tq1 = System.nanoTime();
          double avgQueryUs = (tq1 - tq0) / 1e3 / (double) negN;
          double fpRate = fp / (double) negN;

          long approxBitsBytes = bf.approximateMemoryBytes();

          csv.printf(Locale.US, "%.6f,%d,%d,%d,%.6f,%.3f,%.3f,%.3f,%d%n",
                     p, m, k, n, fpRate, avgQueryUs, insertUs, memDeltaMB, approxBitsBytes);
          csv.flush();

          System.out.printf("Done p=%.5f k=%d fp=%.6f avgQueryUs=%.3f insertUs=%.3f memDeltaMB=%.3f bitsBytes=%d%n",
                            p, k, fpRate, avgQueryUs, insertUs, memDeltaMB, approxBitsBytes);
        }
      }
    }
    System.out.println("Sweep complete. Results appended to " + csvFile);
  }
}