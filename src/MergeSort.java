public class MergeSort {

    public static void sort(int[] arr, int[] temp){
        if (arr.length < 2) return;
        System.arraycopy(arr, 0, temp, 0, arr.length);
        sortRecursive(temp, arr, 0, arr.length);
    }

    private static void sortRecursive(int[] arr, int[] tmp, int low, int high){
        int len = high - low;
        if (len < 2) return;

        int m1 = low + len / 3;
        int m2 = low + 2 * len / 3;

        sortRecursive(tmp, arr, low, m1);
        sortRecursive(tmp, arr, m1, m2);
        sortRecursive(tmp, arr, m2, high);

        merge(arr, tmp, low, m1, m2, high);
    }

    private static void merge(int[] arr, int[] tmp, int low, int m1, int m2, int hi){
        int fstPtr = low, midPtr = m1, lstPtr = m2, tmpPtr = low;

        while (fstPtr < m1 || midPtr < m2 || lstPtr < hi) {
            if (fstPtr < m1
                    && (midPtr == m2 || arr[fstPtr] < arr[midPtr])
                    && (lstPtr == hi || arr[fstPtr] < arr[lstPtr]))
                tmp[tmpPtr++] = arr[fstPtr++];

            else if (midPtr < m2
                    && (lstPtr == hi || arr[midPtr] < arr[lstPtr]))
                tmp[tmpPtr++] = arr[midPtr++];

            else tmp[tmpPtr++] = arr[lstPtr++];
        }
    }
    
    // ------------------------------- //
    // SAME CODE COPIED FOR PRIMITIVES //
    // ------------------------------- //

    public static void sort(double[] arr, double[] temp){
        if (arr.length < 2) return;
        System.arraycopy(arr, 0, temp, 0, arr.length);
        sortRecursive(temp, arr, 0, arr.length);
    }

    private static void sortRecursive(double[] arr, double[] tmp, int low, int high){
        int len = high - low;
        if (len < 2) return;

        int m1 = low + len / 3;
        int m2 = low + 2 * len / 3;

        sortRecursive(tmp, arr, low, m1);
        sortRecursive(tmp, arr, m1, m2);
        sortRecursive(tmp, arr, m2, high);

        merge(arr, tmp, low, m1, m2, high);
    }

    private static void merge(double[] arr, double[] tmp, int low, int m1, int m2, int hi){
        int fstPtr = low, midPtr = m1, lstPtr = m2, tmpPtr = low;

        while (fstPtr < m1 || midPtr < m2 || lstPtr < hi) {
            if (fstPtr < m1
                    && (midPtr == m2 || arr[fstPtr] < arr[midPtr])
                    && (lstPtr == hi || arr[fstPtr] < arr[lstPtr]))
                tmp[tmpPtr++] = arr[fstPtr++];

            else if (midPtr < m2
                    && (lstPtr == hi || arr[midPtr] < arr[lstPtr]))
                tmp[tmpPtr++] = arr[midPtr++];

            else tmp[tmpPtr++] = arr[lstPtr++];
        }
    }
}