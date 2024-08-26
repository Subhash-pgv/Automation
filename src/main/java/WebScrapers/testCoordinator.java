package WebScrapers;

public class testCoordinator {
    public static void main(String[] args) throws ClassNotFoundException, InterruptedException {
        // List of scraper classes to run
        Class<?>[] scrapers = {
            WebScrapers.testscrap1.class,
            WebScrapers.testscrap2.class,
            WebScrapers.testscrap3.class,
            WebScrapers.testscrap4.class
        };

        // Array to hold threads for concurrent execution
        Thread[] threads = new Thread[scrapers.length];

        // Initialize and start threads for each scraper class
        for (int i = 0; i < scrapers.length; i++) {
            threads[i] = new Thread(new JobScraperTask(scrapers[i], args));
            threads[i].start();
        }

        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join();
        }

        System.out.println("All scraping tasks completed.");
    }

    // Runnable task to invoke the main method of the scraper class
    static class JobScraperTask implements Runnable {
        private Class<?> jobScraperClass;
        private String[] args;

        public JobScraperTask(Class<?> jobScraperClass, String[] args) {
            this.jobScraperClass = jobScraperClass;
            this.args = args;
        }

        @Override
        public void run() {
            try {
                // Invoke the main method of the scraper class
                jobScraperClass.getMethod("main", String[].class).invoke(null, (Object) args);
            } catch (NoSuchMethodException e) {
                System.err.println("No such method found: " + e.getMessage());
            } catch (IllegalAccessException e) {
                System.err.println("Illegal access: " + e.getMessage());
            } catch (java.lang.reflect.InvocationTargetException e) {
                System.err.println("Error invoking main method: " + e.getCause().getMessage());
            } catch (Exception e) {
                System.err.println("Unexpected error: " + e.getMessage());
            }
        }
    }
}
