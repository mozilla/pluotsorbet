import com.sun.cldchi.jvm.JVM;

class BubbleSort {
  private static void bubbleSort(int [] a, int left, int right) {
    for (int i = right; i > 1; i--) {
      for (int j = left; j < i; j++) {
        if(a[j] > a[j + 1]) {
          int temp = a[j];
          a[j] = a[j + 1];
          a[j + 1] = temp;
        }
      }
    }
  }

  public static void main(String[] args) {
    int[] array = new int[1024];

    for (int i = 0; i < array.length; i++) {
      array[i] = array.length - i;
    }

    long total = 0L;

    for (int i = 0; i < 100; i++) {
      for (int j = 0; j < array.length; j++) {
        array[j] = array.length - j;
      }
      long start = JVM.monotonicTimeMillis();
      BubbleSort.bubbleSort(array, 0, array.length - 1);
      total += JVM.monotonicTimeMillis() - start;
    }

    String s = "";
    for (int i = 0; i < array.length; i++) {
      s += array[i] + " ";
    }
    System.out.println(s);

    System.out.println("BubbleSort: " + total);
  }
}
