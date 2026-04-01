import java.io.*;
import java.nio.file.*;
import java.util.*;

public class HistogramExporter {
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

  public static void main(String[] args) throws Exception {
    if (args.length < 2) {
      System.out.println("Usage: java HistogramExporter <hashes.txt> <out_histogram.csv>");
      return;
    }
    String path = args[0];
    String outCsv = args[1];
    List<String> all = loadHashes(path);
    int n = 900_000;
    if (n > all.size()) n = all.size();
    double p = 0.01;
    int m = (int) Math.ceil(- (n * Math.log(p)) / (Math.pow(Math.log(2), 2)));
    int k = (int) Math.max(1, Math.round((m / (double) n) * Math.log(2)));
    System.out.printf("Histogram run: n=%d p=%.5f m=%d k=%d%n", n, p, m, k);

    int[] counts = new int[m];
    BloomFilterPacked bf = new BloomFilterPacked(m, k);
    for (int i = 0; i < n; i++) {
      String h = all.get(i);
      int[] idx = bf.getIndicesForDebug(h);
      for (int j = 0; j < idx.length; j++) counts[idx[j]]++;
      bf.insert(h); // optional, already done inside getIndicesForDebug logic
    }

    long sum = 0, sumSq = 0;
    int nonZero = 0;
    for (int c : counts) {
      sum += c;
      sumSq += (long) c * c;
      if (c > 0) nonZero++;
    }
    double mean = sum / (double) m;
    double variance = (sumSq / (double) m) - mean * mean;
    double stddev = Math.sqrt(Math.max(0.0, variance));
    System.out.printf("Histogram mean=%.5f stddev=%.5f nonzero=%d/%d%n", mean, stddev, nonZero, m);

    try (PrintWriter pw = new PrintWriter(new FileWriter(outCsv))) {
      pw.println("index,count");
      for (int i = 0; i < counts.length; i++) pw.printf("%d,%d%n", i, counts[i]);
    }
    System.out.println("Wrote histogram to " + outCsv);
  }
}