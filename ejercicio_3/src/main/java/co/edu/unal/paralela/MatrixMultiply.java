// Declaración del paquete donde se encuentra la clase
package co.edu.unal.paralela;

// Importación estática del método forseq2d de la biblioteca PCDP (Parallel, Concurrent, and Distributed Programming)
// forseq2d permite ejecutar bucles bidimensionales de forma secuencial con sintaxis lambda
import static edu.rice.pcdp.PCDP.forseq2d;

// Importación estática del método forallChunked de PCDP
// forallChunked divide el trabajo en chunks y los ejecuta en paralelo
import static edu.rice.pcdp.PCDP.forallChunked;

// Importación estática del método forall2dChunked de PCDP
// forall2dChunked divide bucles bidimensionales en chunks y los ejecuta en paralelo
import static edu.rice.pcdp.PCDP.forall2dChunked;

/**
 * Comentario de documentación JavaDoc que describe la clase
 * Clase envolvente (wrapper) para implementar multiplicación de matrices paralela eficiente
 */
public final class MatrixMultiply {

    /**
     * Constructor privado por defecto
     * Se hace privado para evitar la instanciación de esta clase utilitaria
     * Esta clase solo contiene métodos estáticos, no necesita ser instanciada
     */
    private MatrixMultiply() {
        // Constructor vacío - no se permite crear instancias de esta clase
    }

    /**
     * Método público estático que realiza multiplicación de matrices de forma secuencial
     * Utiliza la biblioteca PCDP para estructura de bucles pero ejecuta secuencialmente
     *
     * @param A Matriz de entrada A con dimensiones NxN (multiplicando)
     * @param B Matriz de entrada B con dimensiones NxN (multiplicador)
     * @param C Matriz de salida C con dimensiones NxN (producto A x B)
     * @param N Tamaño de las matrices cuadradas (número de filas y columnas)
     */
    public static void seqMatrixMultiply(final double[][] A, final double[][] B,
                                         final double[][] C, final int N) {

        // forseq2d ejecuta un bucle bidimensional secuencial
        // Parámetros: (inicio_i, fin_i, inicio_j, fin_j, lambda_function)
        // Itera desde (0,0) hasta (N-1, N-1) de forma secuencial
        // (i, j) -> { ... } es una expresión lambda que recibe los índices i, j
        forseq2d(0, N - 1, 0, N - 1, (i, j) -> {

            // Inicializa el elemento C[i][j] en 0.0
            // Este será el acumulador para el producto punto de la fila i de A con la columna j de B
            C[i][j] = 0.0;

            // Bucle tradicional for que realiza el producto punto
            // k itera sobre las columnas de A y las filas de B
            for (int k = 0; k < N; k++) {

                // Suma el producto A[i][k] * B[k][j] al acumulador C[i][j]
                // Esta es la operación fundamental: elemento de fila i de A por elemento de columna j de B
                C[i][j] += A[i][k] * B[k][j];

            }

        }); // Fin de la expresión lambda y del forseq2d

    } // Fin del método seqMatrixMultiply

    /**
     * Método público estático que realiza multiplicación de matrices de forma paralela
     * Utiliza técnicas de blocking/tiling para optimizar el rendimiento y la localidad de caché
     *
     * @param A Matriz de entrada A con dimensiones NxN (multiplicando)
     * @param B Matriz de entrada B con dimensiones NxN (multiplicador)  
     * @param C Matriz de salida C con dimensiones NxN (producto A x B)
     * @param N Tamaño de las matrices cuadradas (número de filas y columnas)
     */
    public static void parMatrixMultiply(final double[][] A, final double[][] B,
                                         final double[][] C, final int N) {

        // Comentario que explica el primer paso: inicialización paralela
        // Inicializar la matriz de resultado a cero.

        // forall2dChunked ejecuta un bucle bidimensional en paralelo dividido en chunks
        // Parámetros: (inicio_i, fin_i, inicio_j, fin_j, lambda_function)
        // Cada thread procesa un chunk de la matriz en paralelo
        forall2dChunked(0, N - 1, 0, N - 1, (i, j) -> {

            // Inicializa cada elemento C[i][j] en 0.0 de forma paralela
            // Múltiples threads ejecutan esta operación simultáneamente en diferentes elementos
            C[i][j] = 0.0;

        }); // Fin de la inicialización paralela

        // Comentario que explica la estrategia de blocking
        // Tamaño de bloque. Puede ajustarse para optimizar el rendimiento. 32 es un buen punto de partida.

        // Constante final que define el tamaño de cada bloque (tile)
        // 32 es un valor empíricamente bueno que balancea paralelismo y localidad de caché
        // Los bloques de 32x32 suelen caber bien en la caché L1 de la mayoría de procesadores
        final int BLOCK_SIZE = 32;

        // Comentario que explica la paralelización de bloques
        // Se paralelizan los bucles que iteran sobre los bloques de la matriz C

        // forall2dChunked para paralelizar la iteración sobre bloques
        // (N / BLOCK_SIZE) - 1 calcula el número de bloques en cada dimensión menos 1
        // ib y jb representan los índices de los bloques, no de elementos individuales
        forall2dChunked(0, (N / BLOCK_SIZE) - 1, 0, (N / BLOCK_SIZE) - 1, (ib, jb) -> {

            // Comentario que aclara qué representan los parámetros
            // ib y jb son los índices de los bloques

            // Bucle secuencial sobre los bloques en la dimensión k
            // kb es el índice del bloque en la dimensión k
            // Este bucle NO se paraleliza para mantener la correctitud del algoritmo
            for (int kb = 0; kb < (N / BLOCK_SIZE); kb++) {

                // Comentario que explica el cálculo de límites
                // Límites del bloque actual

                // Calcula el índice de inicio de filas para el bloque actual
                // ib * BLOCK_SIZE convierte el índice de bloque a índice de elemento
                final int iStart = ib * BLOCK_SIZE;

                // Calcula el índice de fin de filas para el bloque actual
                // iStart + BLOCK_SIZE define el límite superior (exclusivo)
                final int iEnd = iStart + BLOCK_SIZE;

                // Calcula el índice de inicio de columnas para el bloque actual
                final int jStart = jb * BLOCK_SIZE;

                // Calcula el índice de fin de columnas para el bloque actual
                final int jEnd = jStart + BLOCK_SIZE;

                // Calcula el índice de inicio en la dimensión k para el bloque actual
                final int kStart = kb * BLOCK_SIZE;

                // Calcula el índice de fin en la dimensión k para el bloque actual
                final int kEnd = kStart + BLOCK_SIZE;

                // Comentario que explica la multiplicación interna
                // Multiplicación secuencial dentro del bloque
                // Esto tiene una excelente localidad de caché

                // Bucle sobre las filas dentro del bloque actual
                // i va desde iStart hasta iEnd-1 (iEnd es exclusivo)
                for (int i = iStart; i < iEnd; i++) {

                    // Bucle sobre las columnas dentro del bloque actual
                    // j va desde jStart hasta jEnd-1 (jEnd es exclusivo)
                    for (int j = jStart; j < jEnd; j++) {

                        // Comentario que explica la optimización de acceso a memoria
                        // Cargar valor existente

                        // Carga el valor actual de C[i][j] en una variable local
                        // Esto optimiza el acceso a memoria al evitar múltiples accesos al arreglo
                        // accum actúa como acumulador temporal
                        double accum = C[i][j];

                        // Bucle interno sobre la dimensión k dentro del bloque
                        // k va desde kStart hasta kEnd-1 (kEnd es exclusivo)
                        for (int k = kStart; k < kEnd; k++) {

                            // Suma el producto A[i][k] * B[k][j] al acumulador local
                            // Esta operación se repite BLOCK_SIZE veces antes de escribir de vuelta a C[i][j]
                            // Mejora la localidad de caché al mantener accum en registros del procesador
                            accum += A[i][k] * B[k][j];

                        } // Fin del bucle k interno

                        // Comentario que explica la escritura final
                        // Guardar resultado final del bloque

                        // Escribe el resultado acumulado de vuelta a la matriz C
                        // Solo se hace una escritura por cada BLOCK_SIZE operaciones
                        // Esto reduce significativamente el tráfico de memoria
                        C[i][j] = accum;

                    } // Fin del bucle j interno

                } // Fin del bucle i interno

            } // Fin del bucle kb secuencial

        }); // Fin del forall2dChunked paralelo

    } // Fin del método parMatrixMultiply

} // Fin de la clase MatrixMultiply