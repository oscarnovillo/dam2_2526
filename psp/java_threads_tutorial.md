# Tutorial Completo: Hilos en Java 21 con Maven y Spring Boot 3.2

## Índice
1. [Configuración del Proyecto Maven](#1-configuración-del-proyecto-maven)
2. [Fundamentos de Hilos en Java](#2-fundamentos-de-hilos-en-java)
3. [ExecutorServices](#3-executorservices)
4. [Sincronización con Locks](#4-sincronización-con-locks)
5. [Semáforos](#5-semáforos)
6. [CountDownLatch](#6-countdownlatch)
7. [Otras Herramientas de Sincronización](#7-otras-herramientas-de-sincronización)
8. [Hilos con Spring Boot 3.2](#8-hilos-con-spring-boot-32)
9. [Ejercicios Prácticos](#9-ejercicios-prácticos)

---

## 1. Configuración del Proyecto Maven

### pom.xml
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.dam.threads</groupId>
    <artifactId>java21-threads-tutorial</artifactId>
    <version>1.0.0</version>
    <packaging>jar</packaging>

    <properties>
        <maven.compiler.source>21</maven.compiler.source>
        <maven.compiler.target>21</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <dependencies>
        <!-- JUnit 5 para testing -->
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter-engine</artifactId>
            <version>5.10.0</version>
            <scope>test</scope>
        </dependency>
        
        <!-- SLF4J para logging -->
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-simple</artifactId>
            <version>2.0.9</version>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.11.0</version>
                <configuration>
                    <source>21</source>
                    <target>21</target>
                    <compilerArgs>
                        <arg>--enable-preview</arg>
                    </compilerArgs>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

---

## 2. Fundamentos de Hilos en Java

### 2.1 Creación de Hilos Básicos

```java
package com.dam.threads.basic;

public class BasicThreads {
    
    // Método 1: Extendiendo Thread
    static class MyThread extends Thread {
        private final String name;
        
        public MyThread(String name) {
            this.name = name;
        }
        
        @Override
        public void run() {
            for (int i = 0; i < 5; i++) {
                System.out.println(name + " - Iteración: " + i);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }
    
    // Método 2: Implementando Runnable
    static class MyRunnable implements Runnable {
        private final String name;
        
        public MyRunnable(String name) {
            this.name = name;
        }
        
        @Override
        public void run() {
            for (int i = 0; i < 5; i++) {
                System.out.println(name + " - Contador: " + i + 
                    " [Thread: " + Thread.currentThread().getName() + "]");
                try {
                    Thread.sleep(800);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }
    
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== CREACIÓN DE HILOS BÁSICOS ===");
        
        // Usando herencia de Thread
        MyThread thread1 = new MyThread("Hilo-1");
        MyThread thread2 = new MyThread("Hilo-2");
        
        thread1.start();
        thread2.start();
        
        // Esperar que terminen
        thread1.join();
        thread2.join();
        
        System.out.println("\n=== USANDO RUNNABLE ===");
        
        // Usando Runnable
        Thread thread3 = new Thread(new MyRunnable("Runnable-1"));
        Thread thread4 = new Thread(new MyRunnable("Runnable-2"));
        
        thread3.start();
        thread4.start();
        
        thread3.join();
        thread4.join();
        
        System.out.println("\n=== USANDO LAMBDAS (Java 8+) ===");
        
        // Usando expresiones lambda
        Thread lambdaThread = new Thread(() -> {
            for (int i = 0; i < 3; i++) {
                System.out.println("Lambda Thread - " + i);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        
        lambdaThread.start();
        lambdaThread.join();
        
        System.out.println("Todos los hilos han terminado!");
    }
}
```

### 2.2 Estados de los Hilos

```java
package com.dam.threads.basic;

public class ThreadStates {
    
    public static void main(String[] args) throws InterruptedException {
        Thread thread = new Thread(() -> {
            System.out.println("Hilo iniciado - Estado: " + 
                Thread.currentThread().getState());
            
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            System.out.println("Hilo terminando - Estado: " + 
                Thread.currentThread().getState());
        });
        
        System.out.println("Estado inicial: " + thread.getState()); // NEW
        
        thread.start();
        System.out.println("Después de start(): " + thread.getState()); // RUNNABLE
        
        Thread.sleep(500);
        System.out.println("Durante sleep: " + thread.getState()); // TIMED_WAITING
        
        thread.join();
        System.out.println("Después de terminar: " + thread.getState()); // TERMINATED
    }
}
```

---

## 3. ExecutorServices

### 3.1 Tipos de ExecutorService

```java
package com.dam.threads.executors;

import java.util.concurrent.*;
import java.util.List;
import java.util.ArrayList;

public class ExecutorServiceExamples {
    
    public static void main(String[] args) {
        // Diferentes tipos de ExecutorService
        singleThreadExecutor();
        fixedThreadPool();
        cachedThreadPool();
        scheduledThreadPool();
        workStealingPool(); // Java 8+
    }
    
    private static void singleThreadExecutor() {
        System.out.println("\n=== SINGLE THREAD EXECUTOR ===");
        ExecutorService executor = Executors.newSingleThreadExecutor();
        
        for (int i = 0; i < 5; i++) {
            final int taskId = i;
            executor.submit(() -> {
                System.out.println("Tarea " + taskId + 
                    " ejecutada por: " + Thread.currentThread().getName());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
        
        executor.shutdown();
        try {
            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
    }
    
    private static void fixedThreadPool() {
        System.out.println("\n=== FIXED THREAD POOL ===");
        ExecutorService executor = Executors.newFixedThreadPool(3);
        
        for (int i = 0; i < 10; i++) {
            final int taskId = i;
            executor.submit(() -> {
                System.out.println("Tarea " + taskId + 
                    " ejecutada por: " + Thread.currentThread().getName());
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
        
        executor.shutdown();
        try {
            executor.awaitTermination(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
    }
    
    private static void cachedThreadPool() {
        System.out.println("\n=== CACHED THREAD POOL ===");
        ExecutorService executor = Executors.newCachedThreadPool();
        
        for (int i = 0; i < 20; i++) {
            final int taskId = i;
            executor.submit(() -> {
                System.out.println("Tarea " + taskId + 
                    " ejecutada por: " + Thread.currentThread().getName());
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
        
        executor.shutdown();
        try {
            executor.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
    }
    
    private static void scheduledThreadPool() {
        System.out.println("\n=== SCHEDULED THREAD POOL ===");
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);
        
        // Tarea que se ejecuta una vez después de 2 segundos
        executor.schedule(() -> {
            System.out.println("Tarea programada ejecutada después de 2 segundos");
        }, 2, TimeUnit.SECONDS);
        
        // Tarea que se ejecuta cada 3 segundos
        ScheduledFuture<?> periodicTask = executor.scheduleAtFixedRate(() -> {
            System.out.println("Tarea periódica: " + System.currentTimeMillis());
        }, 1, 3, TimeUnit.SECONDS);
        
        // Cancelar después de 10 segundos
        executor.schedule(() -> {
            periodicTask.cancel(false);
            executor.shutdown();
        }, 10, TimeUnit.SECONDS);
    }
    
    private static void workStealingPool() {
        System.out.println("\n=== WORK STEALING POOL ===");
        ExecutorService executor = Executors.newWorkStealingPool();
        
        List<Future<Integer>> futures = new ArrayList<>();
        
        for (int i = 0; i < 10; i++) {
            final int taskId = i;
            Future<Integer> future = executor.submit(() -> {
                System.out.println("Procesando tarea " + taskId + 
                    " en: " + Thread.currentThread().getName());
                Thread.sleep(1000 + (int)(Math.random() * 2000));
                return taskId * taskId;
            });
            futures.add(future);
        }
        
        // Recoger resultados
        for (int i = 0; i < futures.size(); i++) {
            try {
                Integer result = futures.get(i).get();
                System.out.println("Resultado tarea " + i + ": " + result);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        
        executor.shutdown();
    }
}
```

### 3.2 Callable y Future

```java
package com.dam.threads.executors;

import java.util.concurrent.*;
import java.util.List;
import java.util.ArrayList;

public class CallableAndFuture {
    
    // Callable que devuelve un resultado
    static class CalculatorTask implements Callable<Integer> {
        private final int number;
        
        public CalculatorTask(int number) {
            this.number = number;
        }
        
        @Override
        public Integer call() throws Exception {
            System.out.println("Calculando factorial de " + number + 
                " en: " + Thread.currentThread().getName());
            
            Thread.sleep(2000); // Simular trabajo pesado
            
            int result = 1;
            for (int i = 1; i <= number; i++) {
                result *= i;
            }
            
            return result;
        }
    }
    
    public static void main(String[] args) {
        System.out.println("=== CALLABLE Y FUTURE ===");
        
        ExecutorService executor = Executors.newFixedThreadPool(3);
        
        // Lista para almacenar los Future
        List<Future<Integer>> futures = new ArrayList<>();
        
        // Enviar tareas
        for (int i = 1; i <= 5; i++) {
            Future<Integer> future = executor.submit(new CalculatorTask(i));
            futures.add(future);
        }
        
        // Obtener resultados
        for (int i = 0; i < futures.size(); i++) {
            try {
                // get() es bloqueante
                Integer result = futures.get(i).get(5, TimeUnit.SECONDS);
                System.out.println("Factorial de " + (i + 1) + " = " + result);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                System.err.println("Error obteniendo resultado: " + e.getMessage());
            }
        }
        
        // CompletableFuture (Java 8+)
        completableFutureExample();
        
        executor.shutdown();
    }
    
    private static void completableFutureExample() {
        System.out.println("\n=== COMPLETABLE FUTURE ===");
        
        CompletableFuture<String> future1 = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return "Resultado de tarea 1";
        });
        
        CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return "Resultado de tarea 2";
        });
        
        // Combinar resultados
        CompletableFuture<String> combinedFuture = future1
            .thenCombine(future2, (result1, result2) -> {
                return result1 + " + " + result2;
            });
        
        try {
            String result = combinedFuture.get();
            System.out.println("Resultado combinado: " + result);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }
}
```

---

## 4. Sincronización con Locks

### 4.1 ReentrantLock

```java
package com.dam.threads.synchronization;

import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Lock;

public class ReentrantLockExample {
    private final Lock lock = new ReentrantLock();
    private int counter = 0;
    
    public void increment() {
        lock.lock();
        try {
            counter++;
            System.out.println(Thread.currentThread().getName() + 
                " - Counter: " + counter);
            Thread.sleep(100); // Simular trabajo
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            lock.unlock();
        }
    }
    
    public int getCounter() {
        lock.lock();
        try {
            return counter;
        } finally {
            lock.unlock();
        }
    }
    
    public static void main(String[] args) throws InterruptedException {
        ReentrantLockExample example = new ReentrantLockExample();
        
        // Crear varios hilos que incrementen el contador
        Thread[] threads = new Thread[5];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 10; j++) {
                    example.increment();
                }
            }, "Thread-" + i);
        }
        
        // Iniciar todos los hilos
        for (Thread thread : threads) {
            thread.start();
        }
        
        // Esperar que terminen
        for (Thread thread : threads) {
            thread.join();
        }
        
        System.out.println("Contador final: " + example.getCounter());
    }
}
```

### 4.2 ReadWriteLock

```java
package com.dam.threads.synchronization;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.HashMap;
import java.util.Map;

public class ReadWriteLockExample {
    private final Map<String, String> data = new HashMap<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    
    public void write(String key, String value) {
        lock.writeLock().lock();
        try {
            System.out.println(Thread.currentThread().getName() + 
                " escribiendo: " + key + " = " + value);
            data.put(key, value);
            Thread.sleep(1000); // Simular escritura lenta
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    public String read(String key) {
        lock.readLock().lock();
        try {
            System.out.println(Thread.currentThread().getName() + 
                " leyendo: " + key);
            Thread.sleep(500); // Simular lectura
            return data.get(key);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } finally {
            lock.readLock().unlock();
        }
    }
    
    public static void main(String[] args) throws InterruptedException {
        ReadWriteLockExample example = new ReadWriteLockExample();
        
        // Hilo escritor
        Thread writer = new Thread(() -> {
            for (int i = 0; i < 5; i++) {
                example.write("key" + i, "value" + i);
            }
        }, "Writer");
        
        // Hilos lectores
        Thread[] readers = new Thread[3];
        for (int i = 0; i < readers.length; i++) {
            readers[i] = new Thread(() -> {
                for (int j = 0; j < 5; j++) {
                    String value = example.read("key" + j);
                    System.out.println(Thread.currentThread().getName() + 
                        " obtuvo: " + value);
                }
            }, "Reader-" + i);
        }
        
        writer.start();
        for (Thread reader : readers) {
            reader.start();
        }
        
        writer.join();
        for (Thread reader : readers) {
            reader.join();
        }
    }
}
```

### 4.3 StampedLock (Java 8+)

```java
package com.dam.threads.synchronization;

import java.util.concurrent.locks.StampedLock;

public class StampedLockExample {
    private double x, y;
    private final StampedLock lock = new StampedLock();
    
    public void write(double newX, double newY) {
        long stamp = lock.writeLock();
        try {
            x = newX;
            y = newY;
            System.out.println(Thread.currentThread().getName() + 
                " escribió: (" + x + ", " + y + ")");
        } finally {
            lock.unlockWrite(stamp);
        }
    }
    
    public double distanceFromOrigin() {
        long stamp = lock.tryOptimisticRead();
        double curX = x, curY = y;
        
        if (!lock.validate(stamp)) {
            // Falback a read lock
            stamp = lock.readLock();
            try {
                curX = x;
                curY = y;
                System.out.println(Thread.currentThread().getName() + 
                    " usó read lock");
            } finally {
                lock.unlockRead(stamp);
            }
        } else {
            System.out.println(Thread.currentThread().getName() + 
                " usó optimistic read");
        }
        
        return Math.sqrt(curX * curX + curY * curY);
    }
    
    public static void main(String[] args) throws InterruptedException {
        StampedLockExample example = new StampedLockExample();
        
        // Hilo escritor
        Thread writer = new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                example.write(i, i * 2);
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }, "Writer");
        
        // Hilos lectores
        Thread[] readers = new Thread[3];
        for (int i = 0; i < readers.length; i++) {
            readers[i] = new Thread(() -> {
                for (int j = 0; j < 15; j++) {
                    double distance = example.distanceFromOrigin();
                    System.out.println(Thread.currentThread().getName() + 
                        " - Distancia: " + distance);
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }, "Reader-" + i);
        }
        
        writer.start();
        for (Thread reader : readers) {
            reader.start();
        }
        
        writer.join();
        for (Thread reader : readers) {
            reader.join();
        }
    }
}
```

---

## 5. Semáforos

### 5.1 Semaphore Básico

```java
package com.dam.threads.synchronization;

import java.util.concurrent.Semaphore;

public class SemaphoreExample {
    private final Semaphore semaphore;
    private final int maxConnections;
    
    public SemaphoreExample(int maxConnections) {
        this.maxConnections = maxConnections;
        this.semaphore = new Semaphore(maxConnections);
    }
    
    public void connect() {
        try {
            semaphore.acquire();
            System.out.println(Thread.currentThread().getName() + 
                " conectado. Conexiones disponibles: " + 
                semaphore.availablePermits());
            
            // Simular trabajo
            Thread.sleep(2000 + (int)(Math.random() * 3000));
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            System.out.println(Thread.currentThread().getName() + 
                " desconectado. Conexiones disponibles: " + 
                (semaphore.availablePermits() + 1));
            semaphore.release();
        }
    }
    
    public static void main(String[] args) throws InterruptedException {
        SemaphoreExample pool = new SemaphoreExample(3); // Máximo 3 conexiones
        
        // Crear 10 hilos que intentan conectarse
        Thread[] clients = new Thread[10];
        for (int i = 0; i < clients.length; i++) {
            clients[i] = new Thread(pool::connect, "Cliente-" + i);
        }
        
        // Iniciar todos los clientes
        for (Thread client : clients) {
            client.start();
        }
        
        // Esperar que terminen todos
        for (Thread client : clients) {
            client.join();
        }
        
        System.out.println("Todos los clientes han terminado");
    }
}
```

### 5.2 Semáforo para Recursos Limitados

```java
package com.dam.threads.synchronization;

import java.util.concurrent.Semaphore;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ResourcePoolExample {
    private final Semaphore semaphore;
    private final String[] resources;
    private final boolean[] used;
    
    public ResourcePoolExample(int poolSize) {
        semaphore = new Semaphore(poolSize);
        resources = new String[poolSize];
        used = new boolean[poolSize];
        
        for (int i = 0; i < poolSize; i++) {
            resources[i] = "Recurso-" + i;
        }
    }
    
    public String acquireResource() throws InterruptedException {
        semaphore.acquire();
        return getResource();
    }
    
    public void releaseResource(String resource) {
        if (markAsUnused(resource)) {
            semaphore.release();
        }
    }
    
    private synchronized String getResource() {
        for (int i = 0; i < used.length; i++) {
            if (!used[i]) {
                used[i] = true;
                return resources[i];
            }
        }
        return null; // No debería pasar
    }
    
    private synchronized boolean markAsUnused(String resource) {
        for (int i = 0; i < resources.length; i++) {
            if (resources[i].equals(resource)) {
                if (used[i]) {
                    used[i] = false;
                    return true;
                }
            }
        }
        return false;
    }
    
    public static void main(String[] args) throws InterruptedException {
        ResourcePoolExample pool = new ResourcePoolExample(2);
        ExecutorService executor = Executors.newFixedThreadPool(5);
        
        for (int i = 0; i < 8; i++) {
            final int taskId = i;
            executor.submit(() -> {
                try {
                    String resource = pool.acquireResource();
                    System.out.println("Tarea " + taskId + 
                        " usando " + resource);
                    
                    Thread.sleep(2000); // Usar el recurso
                    
                    pool.releaseResource(resource);
                    System.out.println("Tarea " + taskId + 
                        " liberó " + resource);
                        
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
        
        executor.shutdown();
        while (!executor.isTerminated()) {
            Thread.sleep(100);
        }
    }
}
```

---

## 6. CountDownLatch

### 6.1 CountDownLatch Básico

```java
package com.dam.threads.synchronization;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CountDownLatchExample {
    
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== COUNTDOWN LATCH EXAMPLE ===");
        
        final int numberOfServices = 3;
        CountDownLatch latch = new CountDownLatch(numberOfServices);
        
        ExecutorService executor = Executors.newFixedThreadPool(numberOfServices);
        
        // Simular servicios que deben iniciarse antes de continuar
        executor.submit(new Service("Base de Datos", 2000, latch));
        executor.submit(new Service("Cache", 1000, latch));
        executor.submit(new Service("Web Server", 3000, latch));
        
        System.out.println("Esperando que todos los servicios se inicien...");
        latch.await(); // Bloquea hasta que count llegue a 0
        
        System.out.println("¡Todos los servicios iniciados! La aplicación está lista.");
        
        executor.shutdown();
    }
    
    static class Service implements Runnable {
        private final String name;
        private final int initTime;
        private final CountDownLatch latch;
        
        public Service(String name, int initTime, CountDownLatch latch) {
            this.name = name;
            this.initTime = initTime;
            this.latch = latch;
        }
        
        @Override
        public void run() {
            try {
                System.out.println("Iniciando " + name + "...");
                Thread.sleep(initTime);
                System.out.println(name + " iniciado correctamente!");
                latch.countDown(); // Reducir el contador
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
```

### 6.2 Uso Avanzado de CountDownLatch

```java
package com.dam.threads.synchronization;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;

public class RaceExample {
    
    public static void main(String[] args) throws InterruptedException {
        final int numberOfRunners = 5;
        CountDownLatch startLatch = new CountDownLatch(1); // Para sincronizar inicio
        CountDownLatch finishLatch = new CountDownLatch(numberOfRunners); // Para esperar a todos
        
        // Crear corredores
        for (int i = 0; i < numberOfRunners; i++) {
            Thread runner = new Thread(new Runner(i + 1, startLatch, finishLatch));
            runner.start();
        }
        
        System.out.println("Preparándose para la carrera...");
        Thread.sleep(1000);
        
        System.out.println("¡3... 2... 1... SALIDA!");
        startLatch.countDown(); // ¡Comenzar la carrera!
        
        finishLatch.await(); // Esperar a que todos terminen
        System.out.println("¡Carrera terminada!");
    }
    
    static class Runner implements Runnable {
        private final int id;
        private final CountDownLatch startLatch;
        private final CountDownLatch finishLatch;
        
        public Runner(int id, CountDownLatch startLatch, CountDownLatch finishLatch) {
            this.id = id;
            this.startLatch = startLatch;
            this.finishLatch = finishLatch;
        }
        
        @Override
        public void run() {
            try {
                System.out.println("Corredor " + id + " listo en la línea de salida");
                startLatch.await(); // Esperar la señal de inicio
                
                // Simular tiempo de carrera
                int raceTime = ThreadLocalRandom.current().nextInt(1000, 3000);
                Thread.sleep(raceTime);
                
                System.out.println("¡Corredor " + id + " ha llegado a la meta! (Tiempo: " + raceTime + "ms)");
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                finishLatch.countDown(); // Marcar que ha terminado
            }
        }
    }
}
```

---

## 7. Otras Herramientas de Sincronización

### 7.1 CyclicBarrier

```java
package com.dam.threads.synchronization;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.ThreadLocalRandom;

public class CyclicBarrierExample {
    
    public static void main(String[] args) {
        final int numberOfWorkers = 3;
        final int numberOfPhases = 4;
        
        // Barrier que ejecuta una acción cuando todos llegan
        CyclicBarrier barrier = new CyclicBarrier(numberOfWorkers, () -> {
            System.out.println("*** TODOS HAN COMPLETADO LA FASE - CONTINUANDO ***\n");
        });
        
        // Crear workers
        for (int i = 0; i < numberOfWorkers; i++) {
            Thread worker = new Thread(new Worker(i + 1, numberOfPhases, barrier));
            worker.start();
        }
    }
    
    static class Worker implements Runnable {
        private final int id;
        private final int phases;
        private final CyclicBarrier barrier;
        
        public Worker(int id, int phases, CyclicBarrier barrier) {
            this.id = id;
            this.phases = phases;
            this.barrier = barrier;
        }
        
        @Override
        public void run() {
            try {
                for (int phase = 1; phase <= phases; phase++) {
                    // Simular trabajo
                    int workTime = ThreadLocalRandom.current().nextInt(1000, 3000);
                    System.out.println("Worker " + id + " trabajando en fase " + phase + 
                        " por " + workTime + "ms");
                    Thread.sleep(workTime);
                    
                    System.out.println("Worker " + id + " completó fase " + phase + 
                        " - Esperando a otros...");
                    
                    barrier.await(); // Esperar a todos los demás
                }
                
                System.out.println("Worker " + id + " terminó todas las fases!");
                
            } catch (InterruptedException | BrokenBarrierException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
```

### 7.2 Exchanger

```java
package com.dam.threads.synchronization;

import java.util.concurrent.Exchanger;
import java.util.List;
import java.util.ArrayList;

public class ExchangerExample {
    
    public static void main(String[] args) throws InterruptedException {
        Exchanger<List<String>> exchanger = new Exchanger<>();
        
        Thread producer = new Thread(new Producer(exchanger));
        Thread consumer = new Thread(new Consumer(exchanger));
        
        producer.start();
        consumer.start();
        
        producer.join();
        consumer.join();
    }
    
    static class Producer implements Runnable {
        private final Exchanger<List<String>> exchanger;
        
        public Producer(Exchanger<List<String>> exchanger) {
            this.exchanger = exchanger;
        }
        
        @Override
        public void run() {
            List<String> buffer = new ArrayList<>();
            
            try {
                for (int cycle = 1; cycle <= 3; cycle++) {
                    // Llenar el buffer
                    for (int i = 1; i <= 5; i++) {
                        buffer.add("Item-" + cycle + "-" + i);
                        System.out.println("Producido: Item-" + cycle + "-" + i);
                        Thread.sleep(200);
                    }
                    
                    System.out.println("Productor: Buffer lleno, intercambiando...");
                    buffer = exchanger.exchange(buffer);
                    System.out.println("Productor: Buffer intercambiado, continúo...\n");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
    
    static class Consumer implements Runnable {
        private final Exchanger<List<String>> exchanger;
        
        public Consumer(Exchanger<List<String>> exchanger) {
            this.exchanger = exchanger;
        }
        
        @Override
        public void run() {
            List<String> buffer = new ArrayList<>();
            
            try {
                for (int cycle = 1; cycle <= 3; cycle++) {
                    System.out.println("Consumidor: Esperando buffer lleno...");
                    buffer = exchanger.exchange(buffer);
                    
                    // Procesar items
                    for (String item : buffer) {
                        System.out.println("Consumido: " + item);
                        Thread.sleep(300);
                    }
                    
                    buffer.clear();
                    System.out.println("Consumidor: Buffer procesado\n");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
```

### 7.3 Phaser (Java 7+)

```java
package com.dam.threads.synchronization;

import java.util.concurrent.Phaser;
import java.util.concurrent.ThreadLocalRandom;

public class PhaserExample {
    
    public static void main(String[] args) {
        final int numberOfStudents = 4;
        
        // Phaser para coordinar las fases del examen
        Phaser examPhaser = new Phaser(1); // 1 para el coordinador (main)
        
        // Crear estudiantes
        for (int i = 0; i < numberOfStudents; i++) {
            examPhaser.register(); // Registrar cada estudiante
            Thread student = new Thread(new Student(i + 1, examPhaser));
            student.start();
        }
        
        // Coordinador del examen
        System.out.println("=== INICIANDO EXAMEN ===");
        examPhaser.arriveAndAwaitAdvance(); // Fase 0 -> 1
        
        System.out.println("=== PARTE 1 COMPLETADA - INICIANDO PARTE 2 ===");
        examPhaser.arriveAndAwaitAdvance(); // Fase 1 -> 2
        
        System.out.println("=== PARTE 2 COMPLETADA - INICIANDO PARTE 3 ===");
        examPhaser.arriveAndAwaitAdvance(); // Fase 2 -> 3
        
        System.out.println("=== EXAMEN COMPLETADO ===");
        examPhaser.arriveAndDeregister(); // El coordinador sale
    }
    
    static class Student implements Runnable {
        private final int id;
        private final Phaser phaser;
        
        public Student(int id, Phaser phaser) {
            this.id = id;
            this.phaser = phaser;
        }
        
        @Override
        public void run() {
            try {
                // Parte 1 del examen
                doExamPart(1);
                phaser.arriveAndAwaitAdvance();
                
                // Parte 2 del examen
                doExamPart(2);
                phaser.arriveAndAwaitAdvance();
                
                // Parte 3 del examen
                doExamPart(3);
                phaser.arriveAndAwaitAdvance();
                
                System.out.println("Estudiante " + id + " terminó el examen completo!");
                phaser.arriveAndDeregister(); // Salir del phaser
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        private void doExamPart(int part) throws InterruptedException {
            System.out.println("Estudiante " + id + " comenzó parte " + part);
            
            // Simular tiempo variable para completar cada parte
            int timeToComplete = ThreadLocalRandom.current().nextInt(1000, 3000);
            Thread.sleep(timeToComplete);
            
            System.out.println("Estudiante " + id + " completó parte " + part + 
                " en " + timeToComplete + "ms");
        }
    }
}
```

---

## 8. Hilos con Spring Boot 3.2

### 8.1 Configuración de Spring Boot

#### pom.xml para Spring Boot
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
        <relativePath/>
    </parent>

    <groupId>com.dam.springthreads</groupId>
    <artifactId>springboot-threads</artifactId>
    <version>1.0.0</version>
    <packaging>jar</packaging>

    <properties>
        <java.version>21</java.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter</artifactId>
        </dependency>
        
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

### 8.2 Configuración de TaskExecutor

```java
package com.dam.springthreads.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfiguration {
    
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("AsyncTask-");
        executor.setRejectedExecutionHandler(new ThreadPoolTaskExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
    
    @Bean(name = "customTaskExecutor")
    public Executor customTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("CustomTask-");
        executor.initialize();
        return executor;
    }
    
    @Bean
    public ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(5);
        scheduler.setThreadNamePrefix("Scheduled-");
        scheduler.initialize();
        return scheduler;
    }
}
```

### 8.3 Servicios Asíncronos

```java
package com.dam.springthreads.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class AsyncTaskService {
    
    @Async("taskExecutor")
    public void executeAsyncTask(String taskName) {
        System.out.println("Ejecutando tarea asíncrona: " + taskName + 
            " en hilo: " + Thread.currentThread().getName());
        
        try {
            Thread.sleep(2000 + ThreadLocalRandom.current().nextInt(3000));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        System.out.println("Tarea " + taskName + " completada!");
    }
    
    @Async("customTaskExecutor")
    public CompletableFuture<String> executeAsyncTaskWithResult(String input) {
        System.out.println("Procesando: " + input + 
            " en hilo: " + Thread.currentThread().getName());
        
        try {
            Thread.sleep(1000 + ThreadLocalRandom.current().nextInt(2000));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return CompletableFuture.completedFuture("Interrumpido");
        }
        
        String result = "Resultado procesado para: " + input;
        return CompletableFuture.completedFuture(result);
    }
    
    @Async
    public CompletableFuture<Integer> calculateFactorial(int number) {
        System.out.println("Calculando factorial de " + number + 
            " en: " + Thread.currentThread().getName());
        
        try {
            Thread.sleep(1000); // Simular trabajo pesado
            
            int result = 1;
            for (int i = 1; i <= number; i++) {
                result *= i;
                if (result < 0) { // Overflow
                    throw new ArithmeticException("Overflow calculating factorial");
                }
            }
            
            return CompletableFuture.completedFuture(result);
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return CompletableFuture.completedFuture(-1);
        }
    }
}
```

### 8.4 Tareas Programadas

```java
package com.dam.springthreads.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class ScheduledTaskService {
    
    private final AtomicInteger counter = new AtomicInteger(0);
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    
    @Scheduled(fixedRate = 5000) // Cada 5 segundos
    public void reportCurrentTime() {
        System.out.println("La hora actual es: " + 
            LocalDateTime.now().format(formatter) + 
            " - Contador: " + counter.incrementAndGet());
    }
    
    @Scheduled(fixedDelay = 3000, initialDelay = 1000) // 3 segundos después de completar, 1 segundo inicial
    public void performMaintenanceTask() {
        System.out.println("Ejecutando tarea de mantenimiento en: " + 
            Thread.currentThread().getName() + 
            " a las " + LocalDateTime.now().format(formatter));
        
        try {
            Thread.sleep(1000); // Simular trabajo
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    @Scheduled(cron = "0 */2 * * * *") // Cada 2 minutos
    public void generateReport() {
        System.out.println("Generando reporte cada 2 minutos: " + 
            LocalDateTime.now().format(formatter));
    }
}
```

### 8.5 Controller para Pruebas

```java
package com.dam.springthreads.controller;

import com.dam.springthreads.service.AsyncTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    
    @Autowired
    private AsyncTaskService asyncTaskService;
    
    @PostMapping("/async/{taskName}")
    public String executeAsyncTask(@PathVariable String taskName) {
        asyncTaskService.executeAsyncTask(taskName);
        return "Tarea " + taskName + " iniciada de forma asíncrona";
    }
    
    @PostMapping("/process/{input}")
    public CompletableFuture<String> processAsync(@PathVariable String input) {
        return asyncTaskService.executeAsyncTaskWithResult(input);
    }
    
    @GetMapping("/factorial/{number}")
    public String calculateFactorial(@PathVariable int number) 
            throws ExecutionException, InterruptedException {
        
        if (number < 0 || number > 12) {
            return "Error: Número debe estar entre 0 y 12";
        }
        
        CompletableFuture<Integer> future = asyncTaskService.calculateFactorial(number);
        Integer result = future.get(); // Bloquea hasta obtener resultado
        
        return "Factorial de " + number + " = " + result;
    }
    
    @GetMapping("/multiple-tasks")
    public String executeMultipleTasks() throws ExecutionException, InterruptedException {
        
        // Ejecutar múltiples tareas en paralelo
        CompletableFuture<String> task1 = asyncTaskService.executeAsyncTaskWithResult("Tarea-1");
        CompletableFuture<String> task2 = asyncTaskService.executeAsyncTaskWithResult("Tarea-2");
        CompletableFuture<String> task3 = asyncTaskService.executeAsyncTaskWithResult("Tarea-3");
        
        // Esperar que todas terminen
        CompletableFuture<Void> allTasks = CompletableFuture.allOf(task1, task2, task3);
        allTasks.get();
        
        return "Todas las tareas completadas: " + 
               task1.get() + ", " + 
               task2.get() + ", " + 
               task3.get();
    }
}
```

### 8.6 Aplicación Principal

```java
package com.dam.springthreads;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SpringThreadsApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringThreadsApplication.class, args);
        System.out.println("Aplicación Spring Boot iniciada!");
        System.out.println("Prueba los endpoints en: http://localhost:8080/api/tasks/");
    }
}
```

---

## 9. Ejercicios Prácticos

### 9.1 Ejercicio: Simulador de Banco

```java
package com.dam.threads.exercises;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.ThreadLocalRandom;

public class BankSimulator {
    
    static class BankAccount {
        private double balance;
        private final Lock lock = new ReentrantLock();
        private final String accountNumber;
        
        public BankAccount(String accountNumber, double initialBalance) {
            this.accountNumber = accountNumber;
            this.balance = initialBalance;
        }
        
        public boolean withdraw(double amount) {
            lock.lock();
            try {
                if (balance >= amount) {
                    System.out.println(Thread.currentThread().getName() + 
                        " retirando " + amount + " de " + accountNumber);
                    
                    Thread.sleep(100); // Simular procesamiento
                    balance -= amount;
                    
                    System.out.println("Retiro exitoso. Nuevo balance: " + balance);
                    return true;
                } else {
                    System.out.println("Fondos insuficientes en " + accountNumber + 
                        ". Balance actual: " + balance);
                    return false;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            } finally {
                lock.unlock();
            }
        }
        
        public void deposit(double amount) {
            lock.lock();
            try {
                System.out.println(Thread.currentThread().getName() + 
                    " depositando " + amount + " en " + accountNumber);
                
                Thread.sleep(100); // Simular procesamiento
                balance += amount;
                
                System.out.println("Depósito exitoso. Nuevo balance: " + balance);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                lock.unlock();
            }
        }
        
        public double getBalance() {
            lock.lock();
            try {
                return balance;
            } finally {
                lock.unlock();
            }
        }
    }
    
    static class Customer implements Runnable {
        private final BankAccount account;
        private final String name;
        
        public Customer(String name, BankAccount account) {
            this.name = name;
            this.account = account;
        }
        
        @Override
        public void run() {
            for (int i = 0; i < 5; i++) {
                if (ThreadLocalRandom.current().nextBoolean()) {
                    // Depósito
                    double amount = ThreadLocalRandom.current().nextDouble(10, 100);
                    account.deposit(amount);
                } else {
                    // Retiro
                    double amount = ThreadLocalRandom.current().nextDouble(10, 200);
                    account.withdraw(amount);
                }
                
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }
    
    public static void main(String[] args) throws InterruptedException {
        BankAccount account = new BankAccount("ACC-001", 1000.0);
        
        Thread[] customers = new Thread[3];
        customers[0] = new Thread(new Customer("Cliente-1", account), "Cliente-1");
        customers[1] = new Thread(new Customer("Cliente-2", account), "Cliente-2");
        customers[2] = new Thread(new Customer("Cliente-3", account), "Cliente-3");
        
        for (Thread customer : customers) {
            customer.start();
        }
        
        for (Thread customer : customers) {
            customer.join();
        }
        
        System.out.println("Balance final: " + account.getBalance());
    }
}
```

### 9.2 Ejercicio: Productor-Consumidor con BlockingQueue

```java
package com.dam.threads.exercises;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadLocalRandom;

public class ProducerConsumerExample {
    
    static class Product {
        private final int id;
        private final String name;
        
        public Product(int id) {
            this.id = id;
            this.name = "Producto-" + id;
        }
        
        @Override
        public String toString() {
            return name;
        }
    }
    
    static class Producer implements Runnable {
        private final BlockingQueue<Product> queue;
        private final int productsToGenerate;
        
        public Producer(BlockingQueue<Product> queue, int productsToGenerate) {
            this.queue = queue;
            this.productsToGenerate = productsToGenerate;
        }
        
        @Override
        public void run() {
            try {
                for (int i = 1; i <= productsToGenerate; i++) {
                    Product product = new Product(i);
                    
                    // Simular tiempo de producción
                    Thread.sleep(ThreadLocalRandom.current().nextInt(500, 1500));
                    
                    queue.put(product); // Bloquea si la cola está llena
                    System.out.println("Producido: " + product + 
                        " [Cola size: " + queue.size() + "]");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                System.out.println("Productor terminado");
            }
        }
    }
    
    static class Consumer implements Runnable {
        private final BlockingQueue<Product> queue;
        private final String name;
        private volatile boolean running = true;
        
        public Consumer(BlockingQueue<Product> queue, String name) {
            this.queue = queue;
            this.name = name;
        }
        
        @Override
        public void run() {
            try {
                while (running) {
                    Product product = queue.take(); // Bloquea si la cola está vacía
                    
                    // Simular tiempo de procesamiento
                    Thread.sleep(ThreadLocalRandom.current().nextInt(1000, 2000));
                    
                    System.out.println(name + " consumió: " + product + 
                        " [Cola size: " + queue.size() + "]");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                System.out.println(name + " terminado");
            }
        }
        
        public void stop() {
            running = false;
        }
    }
    
    public static void main(String[] args) throws InterruptedException {
        BlockingQueue<Product> queue = new LinkedBlockingQueue<>(5); // Capacidad máxima 5
        
        // Crear productor
        Thread producer = new Thread(new Producer(queue, 15));
        
        // Crear consumidores
        Consumer consumer1 = new Consumer(queue, "Consumidor-1");
        Consumer consumer2 = new Consumer(queue, "Consumidor-2");
        
        Thread consumerThread1 = new Thread(consumer1);
        Thread consumerThread2 = new Thread(consumer2);
        
        // Iniciar hilos
        producer.start();
        consumerThread1.start();
        consumerThread2.start();
        
        // Esperar que termine el productor
        producer.join();
        
        // Esperar un poco para que se consuman los productos restantes
        Thread.sleep(5000);
        
        // Detener consumidores
        consumer1.stop();
        consumer2.stop();
        
        // Interrumpir si están bloqueados en take()
        consumerThread1.interrupt();
        consumerThread2.interrupt();
        
        consumerThread1.join();
        consumerThread2.join();
        
        System.out.println("Productos restantes en cola: " + queue.size());
    }
}
```

### 9.3 Ejercicio Final: Sistema de Procesamiento de Archivos

```java
package com.dam.threads.exercises;

import java.util.concurrent.*;
import java.util.List;
import java.util.ArrayList;

public class FileProcessingSystem {
    
    static class FileTask {
        private final String fileName;
        private final int sizeInMB;
        
        public FileTask(String fileName, int sizeInMB) {
            this.fileName = fileName;
            this.sizeInMB = sizeInMB;
        }
        
        public String getFileName() { return fileName; }
        public int getSizeInMB() { return sizeInMB; }
        
        @Override
        public String toString() {
            return fileName + " (" + sizeInMB + " MB)";
        }
    }
    
    static class FileProcessor implements Callable<String> {
        private final FileTask task;
        
        public FileProcessor(FileTask task) {
            this.task = task;
        }
        
        @Override
        public String call() throws Exception {
            String threadName = Thread.currentThread().getName();
            System.out.println(threadName + " procesando: " + task);
            
            // Simular tiempo de procesamiento basado en el tamaño
            int processingTime = task.getSizeInMB() * 100; // 100ms por MB
            Thread.sleep(processingTime);
            
            String result = "Procesado: " + task.getFileName() + 
                " en " + processingTime + "ms por " + threadName;
            
            System.out.println(result);
            return result;
        }
    }
    
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        System.out.println("=== SISTEMA DE PROCESAMIENTO DE ARCHIVOS ===");
        
        // Crear lista de archivos a procesar
        List<FileTask> files = List.of(
            new FileTask("documento1.pdf", 5),
            new FileTask("imagen1.jpg", 3),
            new FileTask("video1.mp4", 25),
            new FileTask("documento2.pdf", 8),
            new FileTask("imagen2.jpg", 2),
            new FileTask("audio1.mp3", 4),
            new FileTask("video2.mp4", 30),
            new FileTask("documento3.pdf", 12)
        );
        
        // Configurar ExecutorService
        int numberOfThreads = 4;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        
        // Usar CountDownLatch para coordinar
        CountDownLatch latch = new CountDownLatch(files.size());
        
        // Usar Semaphore para limitar procesamiento concurrente de archivos grandes
        Semaphore largFileSemaphore = new Semaphore(2); // Máximo 2 archivos grandes simultáneos
        
        List<Future<String>> futures = new ArrayList<>();
        long startTime = System.currentTimeMillis();
        
        // Enviar todas las tareas
        for (FileTask file : files) {
            Future<String> future = executor.submit(() -> {
                try {
                    // Archivos grandes (>20MB) requieren permiso especial
                    if (file.getSizeInMB() > 20) {
                        largFileSemaphore.acquire();
                        System.out.println("Permiso obtenido para archivo grande: " + file);
                    }
                    
                    String result = new FileProcessor(file).call();
                    return result;
                    
                } catch (Exception e) {
                    return "Error procesando " + file + ": " + e.getMessage();
                } finally {
                    if (file.getSizeInMB() > 20) {
                        largFileSemaphore.release();
                        System.out.println("Permiso liberado para archivo grande: " + file);
                    }
                    latch.countDown();
                }
            });
            futures.add(future);
        }
        
        // Esperar que todas las tareas terminen
        System.out.println("Esperando que terminen todas las tareas...");
        latch.await();
        
        long endTime = System.currentTimeMillis();
        
        // Recoger resultados
        System.out.println("\n=== RESULTADOS ===");
        for (Future<String> future : futures) {
            System.out.println(future.get());
        }
        
        System.out.println("\nTiempo total de procesamiento: " + 
            (endTime - startTime) + " ms");
        
        executor.shutdown();
        
        // Demostración adicional con CompletableFuture
        demonstrateCompletableFuture();
    }
    
    private static void demonstrateCompletableFuture() {
        System.out.println("\n=== COMPLETABLE FUTURE DEMO ===");
        
        CompletableFuture<String> future1 = CompletableFuture
            .supplyAsync(() -> {
                try {
                    Thread.sleep(1000);
                    return "Procesamiento paso 1 completado";
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return "Paso 1 interrumpido";
                }
            })
            .thenApply(result -> {
                System.out.println("Aplicando transformación a: " + result);
                return result.toUpperCase();
            })
            .thenCompose(result -> {
                return CompletableFuture.supplyAsync(() -> {
                    try {
                        Thread.sleep(500);
                        return result + " -> PASO 2 COMPLETADO";
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return result + " -> PASO 2 INTERRUMPIDO";
                    }
                });
            });
        
        try {
            String finalResult = future1.get(3, TimeUnit.SECONDS);
            System.out.println("Resultado final: " + finalResult);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
```

---

## 10. Ejercicios Adicionales para Práctica

### 10.1 Ejercicio: Simulador de Restaurante

**Objetivo**: Crear un simulador de restaurante que use diferentes técnicas de sincronización.

**Requisitos**:
- Usar `Semaphore` para limitar mesas disponibles
- Usar `CountDownLatch` para coordinar el servicio
- Usar `BlockingQueue` para la cocina
- Implementar cocineros y camareros como hilos

```java
package com.dam.threads.exercises;

import java.util.concurrent.*;

public class RestaurantSimulator {
    // TODO: Implementar usando las técnicas aprendidas
    // - Clientes que llegan y esperan mesa
    // - Mesas limitadas (Semaphore)
    // - Órdenes que van a la cocina (BlockingQueue)
    // - Cocineros que preparan comida
    // - Camareros que sirven
}
```

### 10.2 Ejercicio: Sistema de Descarga de Archivos

**Objetivo**: Simular un gestor de descargas con diferentes prioridades.

**Requisitos**:
- Usar `PriorityBlockingQueue` para gestionar prioridades
- Usar `ThreadPoolExecutor` personalizado
- Implementar progreso de descarga
- Permitir pausar/reanudar descargas

### 10.3 Ejercicio Spring Boot: API de Procesamiento de Imágenes

**Objetivo**: Crear una API REST que procese imágenes de forma asíncrona.

**Requisitos**:
- Endpoints para subir imágenes
- Procesamiento asíncrono con diferentes filtros
- Sistema de notificaciones cuando termine el procesamiento
- Monitoreo del estado de las tareas

---

## 11. Mejores Prácticas y Consejos

### 11.1 Mejores Prácticas

1. **Siempre cerrar ExecutorService**:
   ```java
   ExecutorService executor = Executors.newFixedThreadPool(4);
   try {
       // usar executor
   } finally {
       executor.shutdown();
       if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
           executor.shutdownNow();
       }
   }
   ```

2. **Manejar InterruptedException correctamente**:
   ```java
   try {
       Thread.sleep(1000);
   } catch (InterruptedException e) {
       Thread.currentThread().interrupt(); // Restaurar el estado de interrupción
       return; // Salir del método
   }
   ```

3. **Usar try-finally con Locks**:
   ```java
   lock.lock();
   try {
       // código crítico
   } finally {
       lock.unlock();
   }
   ```

4. **Evitar synchronized si es posible, usar java.util.concurrent**

5. **Usar AtomicReference/AtomicInteger para variables compartidas simples**

### 11.2 Debugging y Monitoreo

```java
package com.dam.threads.monitoring;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ThreadMonitoring {
    
    public static void startMonitoring() {
        ScheduledExecutorService monitor = Executors.newScheduledThreadPool(1);
        
        monitor.scheduleAtFixedRate(() -> {
            ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
            
            System.out.println("=== THREAD MONITORING ===");
            System.out.println("Hilos activos: " + threadBean.getThreadCount());
            System.out.println("Hilos daemon: " + threadBean.getDaemonThreadCount());
            System.out.println("Pico de hilos: " + threadBean.getPeakThreadCount());
            System.out.println("Total de hilos creados: " + threadBean.getTotalStartedThreadCount());
            System.out.println("========================\n");
            
        }, 0, 5, TimeUnit.SECONDS);
    }
}
```

---

## 12. Recursos y Referencias

### 12.1 Documentación Oficial
- [Oracle Java Concurrency Tutorial](https://docs.oracle.com/javase/tutorial/essential/concurrency/)
- [Spring Boot Async Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.task-execution-and-scheduling)

### 12.2 Libros Recomendados
- "Java Concurrency in Practice" - Brian Goetz
- "Effective Java" - Joshua Bloch

### 12.3 Herramientas de Desarrollo
- **JConsole**: Para monitorear hilos en tiempo de ejecución
- **VisualVM**: Profiler de aplicaciones Java
- **JProfiler**: Herramienta comercial de profiling

---

## Conclusión

Este tutorial cubre los aspectos más importantes de la programación multihilo en Java 21 y Spring Boot 3.2. La clave está en:

1. **Entender cuándo usar cada herramienta**: ExecutorService para gestión de hilos, Locks para sincronización avanzada, Semáforos para recursos limitados, etc.

2. **Practicar con ejemplos reales**: Los ejercicios proporcionados simulan situaciones del mundo real.

3. **Seguir las mejores prácticas**: Manejo correcto de recursos, interrupciones y excepciones.

4. **Monitorear y debuggear**: Usar herramientas para entender el comportamiento de los hilos.

La programación concurrente es compleja, pero con práctica y entendimiento de estas herramientas, puedes crear aplicaciones eficientes y escalables.