interface TestInterface {
    public void asd();
}

class SimpleClass {

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
      System.out.println("I'm hungry");

      int [] array = new int [1024];

      for (int i = 0; i < array.length; i++) {
        array[i] = array.length - i;
      }

      for (int i = 0; i < 100; i++) {
        for (int j = 0; j < array.length; j++) {
          array[j] = array.length - j;
        }
        SimpleClass.bubbleSort(array, 0, array.length - 1);
      }

      String s = "";
      for (int i = 0; i < array.length; i++) {
        s += array[i] + " ";
      }
      System.out.println(s);
    }
}
