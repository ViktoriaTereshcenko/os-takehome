import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class NonBlockingRequests {
    public static void main(String[] args) {
        // Перенаправлення вводу та виводу
        String inputData = "Simulated user input\n";
        InputStream originalIn = System.in;
        PrintStream originalOut = System.out;

        ByteArrayInputStream in = null;
        ByteArrayOutputStream out = null;
        PrintStream ps = null;

        try {
            // Створюємо потоки для перенаправлення вводу/виводу
            in = new ByteArrayInputStream(inputData.getBytes());
            out = new ByteArrayOutputStream();
            ps = new PrintStream(out);

            System.setIn(in);
            System.setOut(ps);

            // Виконання неблокуючих запитів
            CompletableFuture<String> request1 = makeAsyncRequest("Task 1", 2000);
            CompletableFuture<String> request2 = makeAsyncRequest("Task 2", 1000);
            CompletableFuture<String> request3 = makeAsyncRequest("Task 3", 1500);

            // Очікуємо завершення всіх запитів
            CompletableFuture<Void> allTasks = CompletableFuture.allOf(request1, request2, request3);
            allTasks.thenRun(() -> {
                try {
                    System.out.println(request1.get());
                    System.out.println(request2.get());
                    System.out.println(request3.get());
                } catch (InterruptedException | ExecutionException e) {
                    System.err.println("Error processing requests: " + e.getMessage());
                }
            }).join();

            // Повертаємо результати перенаправленого виводу
            System.setOut(originalOut);
            System.out.println("Redirected Output:");
            System.out.println(out.toString());
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
        } finally {
            // Закриття потоків і відновлення оригінальних потоків вводу/виводу
            if (in != null) try { in.close(); } catch (Exception ignored) {}
            if (out != null) try { out.close(); } catch (Exception ignored) {}
            if (ps != null) ps.close();
            System.setIn(originalIn);
            System.setOut(originalOut);
        }
    }

    // Асинхронна функція для імітації запиту
    private static CompletableFuture<String> makeAsyncRequest(String taskName, int delayMs) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(delayMs); // Імітація затримки
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return taskName + " completed.";
        });
    }
}
