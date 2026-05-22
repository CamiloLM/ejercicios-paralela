// Declaración del paquete donde se encuentra la clase
package co.edu.unal.paralela;

// Importación de la clase Random para generar números aleatorios
import java.util.Random;

// Importación de TestCase de JUnit para crear pruebas unitarias
import junit.framework.TestCase;

// Declaración de la clase pública MatrixMultiplyTest que extiende TestCase
// Esto permite usar métodos de testing de JUnit
public class MatrixMultiplyTest extends TestCase {

    // Constante final estática privada que define cuántas veces se repetirá cada test
    // Se usa final para que no se pueda modificar
    // Se usa static para que pertenezca a la clase, no a instancias específicas
    // Se usa private para que solo sea accesible desde esta clase
    final static private int REPEATS = 20;

    // Método privado estático que obtiene el número de núcleos disponibles
    // Retorna un entero con la cantidad de núcleos
    private static int getNCores() {

        // Obtiene la variable de entorno "COURSERA_GRADER_NCORES"
        // System.getenv() retorna null si la variable no existe
        String ncoresStr = System.getenv("COURSERA_GRADER_NCORES");

        // Verifica si la variable de entorno es null (no existe)
        if (ncoresStr == null) {

            // Si no existe la variable, obtiene el número de procesadores disponibles
            // Runtime.getRuntime() obtiene la instancia del runtime actual
            // availableProcessors() retorna el número de procesadores disponibles para la JVM
            return Runtime.getRuntime().availableProcessors();

        } else {

            // Si existe la variable de entorno, la convierte de String a int
            // Integer.parseInt() convierte un String a entero
            return Integer.parseInt(ncoresStr);

        }

    }

    /**
     * Método privado que crea una matriz cuadrada de doubles de tamaño N x N
     * con valores aleatorios para usar como entrada en los tests
     *
     * @param N Tamaño de la matriz a crear (N filas x N columnas)
     * @return Matriz double[][] inicializada con valores aleatorios de longitud N x N
     */
    private double[][] createMatrix(final int N) {

        // Crea una matriz bidimensional de doubles de tamaño N x N
        // final significa que la referencia no puede cambiar después de la inicialización
        final double[][] input = new double[N][N];

        // Crea un objeto Random con semilla 314 para generar números pseudoaleatorios
        // La semilla fija asegura que siempre se generen los mismos números (reproducibilidad)
        final Random rand = new Random(314);

        // Bucle externo que itera sobre las filas de la matriz (desde 0 hasta N-1)
        for (int i = 0; i < N; i++) {

            // Bucle interno que itera sobre las columnas de la matriz (desde 0 hasta N-1)
            for (int j = 0; j < N; j++) {

                // Asigna a cada posición [i][j] un número entero aleatorio entre 0 y 99
                // rand.nextInt(100) genera un entero entre 0 (inclusive) y 100 (exclusivo)
                // Se convierte automáticamente a double
                input[i][j] = rand.nextInt(100);

            }

        }

        // Retorna la matriz completamente inicializada
        return input;

    }

    /**
     * Método privado que verifica si hay diferencias entre la matriz de referencia
     * y la matriz de salida generada por el algoritmo a probar
     */
    private void checkResult(final double[][] ref, final double[][] output, final int N) {

        // Bucle externo que itera sobre las filas de ambas matrices
        for (int i = 0; i < N; i++) {

            // Bucle interno que itera sobre las columnas de ambas matrices
            for (int j = 0; j < N; j++) {

                // Crea un mensaje de error específico indicando la posición donde se encontró la diferencia
                String msg = "Error detected on cell (" + i + ", " + j + ")";

                // Compara el valor en la posición [i][j] de ambas matrices
                // assertEquals es un método de JUnit que verifica si dos valores son iguales
                // Si no son iguales, lanza una excepción con el mensaje especificado
                assertEquals(msg, ref[i][j], output[i][j]);

            }

        }

    }

    /**
     * Implementación de referencia secuencial para multiplicación de matrices
     * Se usa como "gold standard" para verificar la correctitud de otras implementaciones
     */
    public void seqMatrixMultiply(final double[][] A, final double[][] B, final double[][] C, final int N) {

        // Bucle externo: itera sobre las filas de la matriz resultado C
        for (int i = 0; i < N; i++) {

            // Bucle medio: itera sobre las columnas de la matriz resultado C
            for (int j = 0; j < N; j++) {

                // Inicializa el elemento C[i][j] en 0.0
                // Este será el acumulador para el producto punto
                C[i][j] = 0.0;

                // Bucle interno: realiza el producto punto entre la fila i de A y la columna j de B
                for (int k = 0; k < N; k++) {

                    // Suma el producto A[i][k] * B[k][j] al acumulador C[i][j]
                    // Esta es la operación fundamental de la multiplicación de matrices
                    C[i][j] += A[i][k] * B[k][j];

                }

            }

        }

    }

    /**
     * Método helper privado que prueba la implementación paralela y calcula el speedup
     *
     * @param N El tamaño de las matrices a multiplicar (N x N)
     * @return El speedup logrado (tiempo secuencial / tiempo paralelo)
     */
    private double parTestHelper(final int N) {

        // Crea la matriz A de tamaño N x N con valores aleatorios
        final double[][] A = createMatrix(N);

        // Crea la matriz B de tamaño N x N con valores aleatorios
        final double[][] B = createMatrix(N);

        // Crea la matriz C de tamaño N x N inicializada en ceros (para resultado paralelo)
        final double[][] C = new double[N][N];

        // Crea la matriz refC de tamaño N x N inicializada en ceros (para resultado secuencial de referencia)
        final double[][] refC = new double[N][N];

        // Calcula el resultado correcto usando la implementación secuencial de referencia
        seqMatrixMultiply(A, B, refC, N);

        // Calcula el resultado usando la implementación paralela a probar
        // MatrixMultiply.parMatrixMultiply es un método estático de otra clase
        MatrixMultiply.parMatrixMultiply(A, B, C, N);

        // Verifica que ambos resultados sean idénticos
        // Si hay diferencias, el test fallará aquí
        checkResult(refC, C, N);

        /*
         * Sección de medición de rendimiento:
         * Ejecuta múltiples repeticiones de ambas versiones para obtener
         * mediciones de tiempo más precisas y estables
         */

        // Obtiene el tiempo actual en milisegundos antes de ejecutar las repeticiones secuenciales
        final long seqStartTime = System.currentTimeMillis();

        // Ejecuta la versión secuencial REPEATS veces (20 veces)
        for (int r = 0; r < REPEATS; r++) {

            // Cada iteración ejecuta la multiplicación secuencial completa
            seqMatrixMultiply(A, B, C, N);

        }

        // Obtiene el tiempo actual después de completar todas las repeticiones secuenciales
        final long seqEndTime = System.currentTimeMillis();

        // Obtiene el tiempo actual antes de ejecutar las repeticiones paralelas
        final long parStartTime = System.currentTimeMillis();

        // Ejecuta la versión paralela REPEATS veces (20 veces)
        for (int r = 0; r < REPEATS; r++) {

            // Cada iteración ejecuta la multiplicación paralela completa
            MatrixMultiply.parMatrixMultiply(A, B, C, N);

        }

        // Obtiene el tiempo actual después de completar todas las repeticiones paralelas
        final long parEndTime = System.currentTimeMillis();

        // Calcula el tiempo promedio de ejecución secuencial
        // Divide el tiempo total entre el número de repeticiones
        final long seqTime = (seqEndTime - seqStartTime) / REPEATS;

        // Calcula el tiempo promedio de ejecución paralela
        // Divide el tiempo total entre el número de repeticiones
        final long parTime = (parEndTime - parStartTime) / REPEATS;

        // Calcula y retorna el speedup (aceleración)
        // Speedup = tiempo_secuencial / tiempo_paralelo
        // Un speedup > 1 indica que la versión paralela es más rápida
        return (double)seqTime / (double)parTime;

    }

    /**
     * Método de test público que prueba el rendimiento de la implementación paralela
     * con matrices de tamaño 512x512
     */
    public void testPar512_x_512() {

        // Obtiene el número de núcleos disponibles
        final int ncores = getNCores();

        // Ejecuta el test helper y obtiene el speedup logrado
        double speedup = parTestHelper(512);

        // Calcula el speedup mínimo esperado
        // Se espera al menos 60% de la aceleración teórica máxima (número de núcleos)
        double minimalExpectedSpeedup = (double)ncores * 0.6;

        // Crea un mensaje de error descriptivo usando String.format para formateo
        final String errMsg = String.format("It was expected that the parallel implementation would run at " +
                "least %fx faster, but it only achieved %fx speedup", minimalExpectedSpeedup, speedup);

        // Verifica que el speedup obtenido sea al menos el mínimo esperado
        // assertTrue es un método de JUnit que verifica que una condición sea verdadera
        // Si la condición es falsa, el test falla con el mensaje de error especificado
        assertTrue(errMsg, speedup >= minimalExpectedSpeedup);

    }

    /**
     * Método de test público que prueba el rendimiento de la implementación paralela
     * con matrices de tamaño 768x768 (matriz más grande para mayor carga computacional)
     */
    public void testPar768_x_768() {

        // Obtiene el número de núcleos disponibles
        final int ncores = getNCores();

        // Ejecuta el test helper con matrices más grandes y obtiene el speedup logrado
        double speedup = parTestHelper(768);

        // Calcula el speedup mínimo esperado (mismo criterio: 60% del máximo teórico)
        double minimalExpectedSpeedup = (double)ncores * 0.6;

        // Crea un mensaje de error descriptivo
        final String errMsg = String.format("It was expected that the parallel implementation would run at " +
                "least %fx faster, but it only achieved %fx speedup", minimalExpectedSpeedup, speedup);

        // Verifica que el speedup obtenido sea al menos el mínimo esperado
        assertTrue(errMsg, speedup >= minimalExpectedSpeedup);

    }

}