import java.io.*;
import java.util.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Driver {
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
      System.out.println("Usage: java Driver <path/to/passwords/sources/named_hashes.txt>");
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
    Collections.shuffle(all, new Random(42));

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
    long memBefore = rt.totalMemory() - rt.freeMemory();

    long t0 = System.nanoTime();
    for (String h : train) bf.insert(h);
    long t1 = System.nanoTime();
    double insertSeconds = (t1 - t0) / 1e9;
    System.out.printf("Inserted %d items in %.3f seconds (%.3f µs per insert)%n",
                      train.size(), insertSeconds, (insertSeconds * 1e6) / train.size());

    System.gc();
    long memAfter = rt.totalMemory() - rt.freeMemory();
    System.out.printf("Approx memory used by JVM (after insert): %.2f MB%n", (memAfter - memBefore) / 1024.0 / 1024.0);

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
    System.out.printf("Bloom filter bit size m=%d, k=%d, approx memory bytes for bits=%d%n",
                      m, k, bf.approximateMemoryBytes());
    System.out.println("Driver finished.");
  }
}