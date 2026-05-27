package co.edu.unal.paralela;

import static edu.rice.pcdp.PCDP.forseq2d;
import static edu.rice.pcdp.PCDP.forall2dChunked;

public final class MatrixMultiply {

  private MatrixMultiply() {
  }

  /**
   * Multiplicación de matrices secuencial (A x B = C).
   * 
   * @param A Matriz de entrada con dimensiones NxN
   * @param B Matriz de entrada con dimensiones NxN
   * @param C Matriz de salida
   * @param N Tamaño de las matrices
   */
  public static void seqMatrixMultiply(final double[][] A, final double[][] B,
      final double[][] C, final int N) {
    forseq2d(0, N - 1, 0, N - 1, (i, j) -> {
      C[i][j] = 0.0;
      for (int k = 0; k < N; k++) {
        C[i][j] += A[i][k] * B[k][j];
      }
    });
  }

  /**
   * Multiplicación de matrices paralela (A x B = C) usando blocking/tiling.
   * Cada bloque de C es calculado por un thread independiente.
   * 
   * @param A Matriz de entrada con dimensiones NxN
   * @param B Matriz de entrada con dimensiones NxN
   * @param C Matriz de salida
   * @param N Tamaño de las matrices
   */
  public static void parMatrixMultiply(final double[][] A, final double[][] B, final double[][] C, final int N) {

    // Cada tarea paralela calcula un bloque de BLOCK_SIZE x BLOCK_SIZE de C
    final int BLOCK_SIZE = Math.max(16, N / (4 * Runtime.getRuntime().availableProcessors()));
    final int nBlocks = (N + BLOCK_SIZE - 1) / BLOCK_SIZE;

    forall2dChunked(0, nBlocks - 1, 0, nBlocks - 1, (ib, jb) -> {
      final int iStart = ib * BLOCK_SIZE, iEnd = Math.min(iStart + BLOCK_SIZE, N);
      final int jStart = jb * BLOCK_SIZE, jEnd = Math.min(jStart + BLOCK_SIZE, N);

      // Recorrer los bloques en la dimensión k de forma secuencial
      for (int kb = 0; kb < nBlocks; kb++) {
        final int kStart = kb * BLOCK_SIZE, kEnd = Math.min(kStart + BLOCK_SIZE, N);

        // Multiplicación dentro del bloque; accum evita accesos repetidos a C[i][j]
        for (int i = iStart; i < iEnd; i++) {
          for (int j = jStart; j < jEnd; j++) {
            double accum = 0.0;
            for (int k = kStart; k < kEnd; k++) {
              accum += A[i][k] * B[k][j];
            }
            C[i][j] += accum;
          }
        }
      }
    });
  }
}
