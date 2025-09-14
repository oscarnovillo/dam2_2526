package util;

import java.util.Random;

public class GeneradorNumeros {

    private static final long SEMILLA = 12345L; // Semilla fija para reproducibilidad
    private static final int CANTIDAD_NUMEROS = 1000;
    private static final int MAX_NUMERO = 100;

    private GeneradorNumeros() {
        // Constructor privado para clase utilitaria
    }

    /**
     * Genera un array de números aleatorios usando una semilla fija
     * para garantizar que siempre sean los mismos números
     */
    public static int[] generarNumerosAleatorios() {
        Random random = new Random(SEMILLA);
        int[] numeros = new int[CANTIDAD_NUMEROS];

        for (int i = 0; i < CANTIDAD_NUMEROS; i++) {
            numeros[i] = random.nextInt(MAX_NUMERO) + 1; // Números del 1 al 100
        }

        return numeros;
    }

    /**
     * Calcula la suma esperada de los números generados
     */
    public static long calcularSumaEsperada() {
        int[] numeros = generarNumerosAleatorios();
        long suma = 0;
        for (int numero : numeros) {
            suma += numero;
        }
        return suma;
    }
}
