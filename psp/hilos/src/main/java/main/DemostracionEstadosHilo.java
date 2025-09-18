package main;

/**
 * Demostración de los estados de un hilo y cuándo se ejecuta realmente
 */
public class DemostracionEstadosHilo {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== DEMOSTRACIÓN DE ESTADOS DE 10 HILOS ===\n");

        System.out.println("Hilo principal ejecutándose en: " + Thread.currentThread().getName());
        System.out.println("ID del hilo principal: " + Thread.currentThread().getId());
        System.out.println();

        // Crear 10 hilos pero NO iniciarlos aún
        Thread[] hilos = new Thread[10];
        for (int i = 0; i < 10; i++) {
            hilos[i] = new Thread(new TareaEjemplo(i + 1), "Hilo-Trabajador-" + (i + 1));
        }

        System.out.println("1. DESPUÉS DE CREAR LOS 10 HILOS (antes de start()):");
        for (int i = 0; i < 10; i++) {
            System.out.println("   Hilo " + (i + 1) + " - Estado: " + hilos[i].getState() +
                             " | Vivo: " + hilos[i].isAlive() + " | ID: " + hilos[i].getId());
        }
        System.out.println();

        System.out.println("2. LLAMANDO A start() EN TODOS LOS HILOS...");
        long tiempoInicioTotal = System.nanoTime();

        // Iniciar todos los hilos (¡SIN ESPERAR!)
        for (int i = 0; i < 10; i++) {
            long tiempoStart = System.nanoTime();
            hilos[i].start(); // AQUÍ ES DONDE OCURRE LA MAGIA
            long tiempoPostStart = System.nanoTime();
            System.out.println("   start() del hilo " + (i + 1) + " tomó: " + (tiempoPostStart - tiempoStart) + " ns");
        }

        long tiempoFinTotal = System.nanoTime();
        System.out.println("\n   Tiempo TOTAL para iniciar 10 hilos: " + (tiempoFinTotal - tiempoInicioTotal) + " nanosegundos");
        System.out.println("   ¡El hilo principal NO se bloqueó y continuó inmediatamente!");
        System.out.println();

        // Inmediatamente después de start() - verificar estados
        System.out.println("3. ESTADOS INMEDIATAMENTE DESPUÉS DE start():");
        for (int i = 0; i < 10; i++) {
            System.out.println("   Hilo " + (i + 1) + " - Estado: " + hilos[i].getState() +
                             " | Vivo: " + hilos[i].isAlive());
        }
        System.out.println();

        // El hilo principal sigue ejecutándose mientras los otros trabajan
        System.out.println("4. HILO PRINCIPAL CONTINÚA (mientras los 10 hilos ejecutan EN PARALELO):");
        for (int i = 1; i <= 8; i++) {
            System.out.println("   Hilo principal trabajando... paso " + i);
            System.out.print("   Estados actuales: ");
            for (int j = 0; j < 10; j++) {
                System.out.print("H" + (j + 1) + ":" + hilos[j].getState() + " ");
            }
            System.out.println();
            Thread.sleep(150); // Pausa para ver la concurrencia
        }
        System.out.println();

        System.out.println("5. ESPERANDO A QUE TERMINEN TODOS LOS HILOS (join()):");
        for (int i = 0; i < 10; i++) {
            System.out.println("   Esperando al hilo " + (i + 1) + "...");
            hilos[i].join(); // AQUÍ SÍ esperamos
            System.out.println("   Hilo " + (i + 1) + " terminado - Estado: " + hilos[i].getState());
        }
        System.out.println();

        System.out.println("=== RESUMEN ===");
        System.out.println("• Thread.start() NO bloquea al hilo que lo llama");
        System.out.println("• Los 10 hilos pasaron a estado RUNNABLE (ejecutable)");
        System.out.println("• El sistema operativo decidió CUÁNDO darle CPU a cada uno");
        System.out.println("• TODOS los hilos ejecutaron CONCURRENTEMENTE");
        System.out.println("• Cada hilo puede ejecutarse en un CORE diferente de la CPU");
        System.out.println("• El hilo principal siguió ejecutándose sin esperar");
    }
}

class TareaEjemplo implements Runnable {
    private final int idTarea;

    public TareaEjemplo(int id) {
        this.idTarea = id;
    }

    @Override
    public void run() {
        System.out.println("   → HILO TRABAJADOR " + idTarea + " INICIADO en: " + Thread.currentThread().getName());
        System.out.println("   → ID del hilo trabajador " + idTarea + ": " + Thread.currentThread().getId());

        for (int i = 1; i <= 3; i++) {
            System.out.println("   → Hilo trabajador " + idTarea + " ejecutando tarea " + i);
            try {
                // Tiempo de trabajo aleatorio entre 100-300ms para ver mejor la concurrencia
                Thread.sleep(100 + (int)(Math.random() * 200));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }

        System.out.println("   → HILO TRABAJADOR " + idTarea + " TERMINADO");
    }
}
