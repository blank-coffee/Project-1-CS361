// HashSetMemoryCompare.java
// Usage: compile and run in a separate JVM to avoid heap reuse:
// javac HashSetMemoryCompare.java
// java -Xmx2g HashSetMemoryCompare passwords/hashes.txt
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class HashSetMemoryCompare {
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
    if (args.length < 1) {
      System.out.println("Usage: java -Xmx2g HashSetMemoryCompare <hashes.txt>");
      return;
    }
    String path = args[0];
    List<String> all = loadHashes(path);
    int n = 900_000;
    if (n > all.size()) n = all.size();
    List<String> subset = new ArrayList<>(all.subList(0, n));
    Runtime rt = Runtime.getRuntime();
    System.gc();
    Thread.sleep(200);
    long before = rt.totalMemory() - rt.freeMemory();
    System.out.printf("JVM used before building HashSet: %.3f MB%n", before/1024.0/1024.0);

    long t0 = System.nanoTime();
    HashSet<String> set = new HashSet<>(n * 2);
    for (String s : subset) set.add(s);
    long t1 = System.nanoTime();
    System.gc();
    Thread.sleep(200);
    long after = rt.totalMemory() - rt.freeMemory();
    System.out.printf("Built HashSet of %d items in %.3f s%n", set.size(), (t1 - t0)/1e9);
    System.out.printf("JVM used after building HashSet: %.3f MB%n", after/1024.0/1024.0);
    System.out.printf("Delta MB: %.3f MB%n", (after - before)/1024.0/1024.0);

    // writes a small sample file for TA/grader
    try (PrintWriter pw = new PrintWriter(new FileWriter("hashset_sample.txt"))) {
      for (int i = 0; i < Math.min(1000, subset.size()); i++) pw.println(subset.get(i));
    }
    System.out.println("Wrote hashset_sample.txt (1000 entries).");
  }
}