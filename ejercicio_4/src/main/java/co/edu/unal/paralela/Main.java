package co.edu.unal.paralela;

public class Main {

  public static void main(String[] args) {
    int n = 10;
    int iterations = 3;
    int tasks = 2;

    // Arreglos de tamaño n+2 para incluir los extremos fijos
    double[] myVal = new double[n + 2];
    double[] myNew = new double[n + 2];

    // Valores iniciales: extremos fijos en 0 y 1, interior en 0
    myVal[0] = 0.0;
    myVal[n + 1] = 1.0;
    for (int i = 1; i <= n; i++) {
      myVal[i] = 0.0;
      myNew[i] = 0.0;
    }

    System.out.println("=== Estado inicial ===");
    printArray(myVal, n);

    // Copias independientes para cada método
    double[] myValSeq = myVal.clone();
    double[] myNewSeq = myNew.clone();

    double[] myValBarrier = myVal.clone();
    double[] myNewBarrier = myNew.clone();

    double[] myValFuzzy = myVal.clone();
    double[] myNewFuzzy = myNew.clone();

    // Secuencial
    OneDimAveragingPhaser.runSequential(iterations, myNewSeq, myValSeq, n);
    System.out.println("\n=== Resultado Secuencial ===");
    printResult(myNewSeq, myValSeq, iterations, n);

    // Paralelo con barrera
    OneDimAveragingPhaser.runParallelBarrier(iterations, myNewBarrier, myValBarrier, n, tasks);
    System.out.println("\n=== Resultado Parallel Barrier ===");
    printResult(myNewBarrier, myValBarrier, iterations, n);

    // Paralelo con fuzzy barrier
    OneDimAveragingPhaser.runParallelFuzzyBarrier(iterations, myNewFuzzy, myValFuzzy, n, tasks);
    System.out.println("\n=== Resultado Parallel Fuzzy Barrier ===");
    printResult(myNewFuzzy, myValFuzzy, iterations, n);
  }

  // Imprime el arreglo resultado según el número de iteraciones (par o impar)
  private static void printResult(double[] myNew, double[] myVal, int iterations, int n) {
    if (iterations % 2 == 0) {
      printArray(myVal, n);
    } else {
      printArray(myNew, n);
    }
  }

  private static void printArray(double[] arr, int n) {
    StringBuilder sb = new StringBuilder("[");
    for (int i = 0; i <= n + 1; i++) {
      sb.append(String.format("%.2f", arr[i]));
      if (i < n + 1)
        sb.append(", ");
    }
    sb.append("]");
    System.out.println(sb.toString());
  }
}
