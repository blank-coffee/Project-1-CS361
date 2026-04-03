import java.util.Random;

public class Tester {
    record Result(int power, String type, long time) {
        double seconds() { return time / 1_000_000_000.0; }
        void print() { System.out.printf("Size: 2^%-2d | Type: %-8s | Time: %10.6f s%n", power, type, seconds()); }
    }

    static final Random random = new Random(42);

    static void test(int power, String type, Runnable sort) {
        long start = System.nanoTime();
        sort.run();
        new Result(power, type, System.nanoTime() - start).print();
    }

    static void testSpeed(int power) {
        int n = 1 << power;
        try {
            int[] intArr = new int[n];
            for (int i = 0; i < n; i++) intArr[i] = random.nextInt();
            test(power, "int", () -> MergeSort.sort(intArr, new int[n]));
        } catch (OutOfMemoryError e) { System.out.printf("Size: 2^%-2d | int    | Out of memory%n", power); }

        try {
            double[] dblArr = new double[n];
            for (int i = 0; i < n; i++) dblArr[i] = random.nextDouble();
            test(power, "double", () -> MergeSort.sort(dblArr, new double[n]));
        } catch (OutOfMemoryError e) { System.out.printf("Size: 2^%-2d | double | Out of memory%n", power); }
    }

    public static void main(String[] args) {
        System.out.println("=== MergeSortSpeed ===");
        for (int power = 20; power <= 30; power++) { testSpeed(power); System.out.println(); }
    }
}