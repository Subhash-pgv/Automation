package WebScrapers;

import WebScrapers.testCoordinator.JobScraperTask;

public class testCoordinator {
    public static void main(String[] args) throws ClassNotFoundException, InterruptedException {
     
        Class<?>[] scrapers = {
        		  WebScrapers.testscrap1.class,
                  WebScrapers.testscrap2.class,
                  WebScrapers.testscrap3.class,
                  WebScrapers.testscrap4.class   
        };

        Thread[] threads = new Thread[scrapers.length];
        
        for (int i = 0; i < scrapers.length; i++) {
            threads[i] = new Thread(new JobScraperTask(scrapers[i], args));
            threads[i].start();
        }

       
        for (Thread thread : threads) {
            thread.join();
        }

        System.out.println("All scraping tasks completed.");
    }

   
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
                jobScraperClass.getMethod("main", String[].class).invoke(null, (Object) args);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}