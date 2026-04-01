// Updated driver that prints concise "3.1:", "3.2:", "3.3:", "3.4:", "3.5:" lines
// and uses BloomFilter.getIndicesForDebug(...) to show determinism and sample indices.
//
// Usage: javac DriverWithSectionPrints.java BloomFilter.java
//        java DriverWithSectionPrints passwords/hashes.txt

import java.io.*;
import java.util.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class DriverWithSectionPrints {
  // Compute m and k from n and p
  public static int computeM(int n, double p) {
    double m = - (n * Math.log(p)) / (Math.pow(Math.log(2), 2));
    return (int) Math.ceil(m);
  }
  public static int computeK(int n, int m) {
    double k = (m / (double) n) * Math.log(2);
    return (int) Math.round(k);
  }

  public static List<String> loadHashes(String path) throws IOException {
    List<String> lines = Files.readAllLines(Paths.get(path));
    List<String> cleaned = new ArrayList<>(lines.size());
    for (String l : lines) {
      String s = l.trim();
      if (s.isEmpty()) continue;
      // If file has "hash,count" format, take first token
      if (s.contains(",")) s = s.split(",")[0].trim();
      cleaned.add(s);
    }
    return cleaned;
  }

  public static void main(String[] args) throws Exception {
    if (args.length < 1) {
      System.out.println("Usage: java DriverWithSectionPrints <path/to/passwords/sources/named_hashes.txt>");
      return;
    }
    String path = args[0];
    System.out.println("Loading hashes from: " + path);
    List<String> all = loadHashes(path);
    System.out.println("Total lines loaded: " + all.size());

    // Parameters - adjust as needed
    int n = 900_000;            // number to insert (choose <= all.size())
    double p = 0.01;            // target false positive rate
    int negativeTestSize = 100_000;

    if (n + negativeTestSize > all.size()) {
      // fallback: split 80/20
      n = (int) (all.size() * 0.8);
      negativeTestSize = all.size() - n;
      System.out.println("Adjusted n to " + n + " and negativeTestSize to " + negativeTestSize);
    }

    // Deterministic shuffle
    long shuffleSeed = 42L;
    Collections.shuffle(all, new Random(shuffleSeed));

    List<String> train = new ArrayList<>(all.subList(0, n));
    List<String> negative = new ArrayList<>(all.subList(n, n + negativeTestSize));

    int m = computeM(n, p);
    int k = computeK(n, m);
    System.out.printf("Parameters: n=%d, p=%.5f, m=%d bits (~%.2f MB), k=%d%n",
                      n, p, m, m / 8.0 / 1024.0 / 1024.0, k);

    BloomFilter bf = new BloomFilter(m, k);

    // Memory before insert
    Runtime rt = Runtime.getRuntime();
    System.gc();
    Thread.sleep(200);
    long memBefore = rt.totalMemory() - rt.freeMemory();

    long t0 = System.nanoTime();
    for (String h : train) bf.insert(h);
    long t1 = System.nanoTime();
    double insertSeconds = (t1 - t0) / 1e9;
    double insertUsPer = (insertSeconds * 1e6) / train.size();
    System.out.printf("Inserted %d items in %.3f seconds (%.3f µs per insert)%n",
                      train.size(), insertSeconds, insertUsPer);

    System.gc();
    Thread.sleep(200);
    long memAfter = rt.totalMemory() - rt.freeMemory();
    double memDeltaMB = (memAfter - memBefore) / 1024.0 / 1024.0;
    System.out.printf("JVM used before=%.3f MB, after=%.3f MB, delta=%.3f MB%n",
                      memBefore/1024.0/1024.0, memAfter/1024.0/1024.0, memDeltaMB);

    // Positive test: sample 1000 from train
    Random rnd = new Random(123);
    int posSample = Math.min(1000, train.size());
    int falseNegatives = 0;
    for (int i = 0; i < posSample; i++) {
      String s = train.get(rnd.nextInt(train.size()));
      if (!bf.query(s)) falseNegatives++;
    }
    System.out.println("Positive sample size: " + posSample + ", false negatives: " + falseNegatives);

    // Negative test: query all negatives and compute false positive rate
    int fp = 0;
    long tStartNeg = System.nanoTime();
    for (String s : negative) {
      if (bf.query(s)) fp++;
    }
    long tEndNeg = System.nanoTime();
    double avgQueryUs = ((tEndNeg - tStartNeg) / 1e3) / (double) negative.size();
    double fpRate = fp / (double) negative.size();
    System.out.printf("Negative test size: %d, false positives: %d, FP rate: %.6f%n", negative.size(), fp, fpRate);
    System.out.printf("Average query time (negative set): %.3f µs%n", avgQueryUs);

    // Microbenchmark: 1000 random queries (mix of positives and negatives)
    int micro = 1000;
    List<String> microKeys = new ArrayList<>(micro);
    for (int i = 0; i < micro; i++) {
      if (i % 2 == 0) microKeys.add(train.get(rnd.nextInt(train.size())));
      else microKeys.add(negative.get(rnd.nextInt(negative.size())));
    }
    long tMicroStart = System.nanoTime();
    int found = 0;
    for (String s : microKeys) if (bf.query(s)) found++;
    long tMicroEnd = System.nanoTime();
    double avgMicroUs = (tMicroEnd - tMicroStart) / 1e3 / (double) micro;
    System.out.printf("Microbenchmark %d queries: avg %.3f µs, found true: %d%n", micro, avgMicroUs, found);

    // Print Bloom filter bit info
    long approxBytes = bf.approximateMemoryBytes();
    double approxMB = approxBytes / 1024.0 / 1024.0;
    System.out.printf("Bloom filter bit size m=%d, k=%d, approx memory bytes for bits=%d (%.3f MB)%n",
                      m, k, approxBytes, approxMB);

    // --------------------------
    // Sectioned prints for assignment sections 3.1 - 3.5
    // Each line begins with "3.x:" followed by the requested data summary.
    // --------------------------

    // 3.1: Dataset and splits
    System.out.println();
    System.out.printf("3.1: dataset_lines=%d; train_n=%d; negative_n=%d; shuffle_seed=%d%n",
                      all.size(), train.size(), negative.size(), shuffleSeed);

    // 3.2: Hash function properties & timing
    // We report k, m, determinism check (sample indices), and average query time for 1000 queries
    String sampleHash = train.get(0);
    int[] sampleIndicesA = null;
    int[] sampleIndicesB = null;
    boolean determinismPass = false;
    try {
      sampleIndicesA = bf.getIndicesForDebug(sampleHash);
      sampleIndicesB = bf.getIndicesForDebug(sampleHash); // call twice to check determinism within-run
      determinismPass = Arrays.equals(sampleIndicesA, sampleIndicesB);
    } catch (Exception e) {
      // If getIndicesForDebug is not present or throws, leave indices null and determinism false
      sampleIndicesA = null;
      sampleIndicesB = null;
      determinismPass = false;
    }

    String sampleIdxStr = (sampleIndicesA == null) ? "indices_unavailable" : Arrays.toString(sampleIndicesA);
    String determinismStr = determinismPass ? "PASS" : "FAIL";
    System.out.printf("3.2: k=%d; m=%d; avg_query_us=%.3f; determinism=%s; sample_indices=%s%n",
                      k, m, avgMicroUs, determinismStr, sampleIdxStr);

    // Save sample indices to file for reproducibility
    if (sampleIndicesA != null) {
      try (PrintWriter pw = new PrintWriter(new FileWriter("sample_indices.txt", false))) {
        pw.printf("sample_hash=%s%n", sampleHash);
        pw.printf("indices=%s%n", Arrays.toString(sampleIndicesA));
        pw.printf("determinism=%s%n", determinismStr);
        pw.printf("shuffle_seed=%d%n", shuffleSeed);
      } catch (IOException ioe) {
        System.err.println("Warning: could not write sample_indices.txt: " + ioe.getMessage());
      }
    }

    // 3.3: Bit array details
    System.out.printf("3.3: bit_array_size_m=%d; bit_array_bytes=%d; bit_array_MB=%.3f%n",
                      m, approxBytes, approxMB);

    // 3.4: Insert/query logic & tests
    System.out.printf("3.4: insert_total_seconds=%.3f; insert_us_per=%.3f; positive_sample=%d; false_negatives=%d; negative_test=%d; false_positives=%d; fp_rate=%.6f%n",
                      insertSeconds, insertUsPer, posSample, falseNegatives, negative.size(), fp, fpRate);

    // 3.5: Measurements to report (FP, effect of k/m, query time, memory, deletion note)
    String deletionNote = "\n(cannot delete bits by flipping to 0 (use counting BloomFilter to support deletions)";
    System.out.printf("3.5: measured_fp_rate=%.6f; avg_query_us=%.3f; jvm_mem_delta_MB=%.3f; approx_bits_bytes=%d; deletion_note=%s%n",
                      fpRate, avgQueryUs, memDeltaMB, approxBytes, deletionNote);

    System.out.println("Driver finished.");
  }
}