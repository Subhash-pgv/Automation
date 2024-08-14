package WebScrapers;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class JobScrapping2 {
    public static void main(String[] args) throws IOException, InterruptedException, SQLException, ClassNotFoundException {

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--window-size=1920x1080");
        options.addArguments("--disable-gpu");
        WebDriver driver = new ChromeDriver(options);
        
        
        JavascriptExecutor js = (JavascriptExecutor) driver;
        
        
        
       

        driver.get("https://weworkremotely.com/remote-jobs/search?search_uuid=&term=&sort=any_time&categories%5B%5D=2&categories%5B%5D=17&categories%5B%5D=18&region%5B%5D=1&region%5B%5D=5&region%5B%5D=6&region%5B%5D=7&company_size%5B%5D=1+-+10&company_size%5B%5D=11+-+50");
        driver.manage().window().maximize();
        sleepRandom();
        System.out.println("ADDING JOBS FROM \"weworkremotely.com\"");
        
        String source=null;

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        
        
    	List<String[]> jobDetailsList = new ArrayList<>();
    	Connection connection = null;

        wait.until(ExpectedConditions
                .presenceOfElementLocated(By.xpath("//div//ul//li/a//span[@class='title']")));

       List<WebElement> totalJobs = driver.findElements(By.xpath("//div//ul//li/a//span[@class='title']"));
       int totalJobCount = totalJobs.size();

        int[] sections = {2, 17, 18};
        int totalJobsAppended = 0;
        int totalJobFinds =0;
        
    	
        
        List<String> tabs = null;
      
        try {
        for (int sectionId : sections) {
        	
        	String companyName = null;
            String jobTitle = null;
            String jobLocation = null;
            String jobURL = null;
            String employeeCount=null;
            String companyWebsite= null;
            source= "weworkremotely.com";
            String dateCreated = null;
            
        	 System.out.println("Adding Jobs for "+source +" please wait until it shows completed.....");
        	List<WebElement> resultCountElement = driver.findElements(By.xpath("//section[@id='category-" + sectionId + "']//li/a//span[@class='title']"));
   
        	for (int i = 1; i <= resultCountElement.size(); i++) {

                // Handle each element and check if it exists
                WebElement companyNames = getElementIfExists(driver, "(//section[@id='category-" + sectionId + "']//li[" + i + "]/a//span[@class='company'][1])");
                if (companyNames != null) {
                    companyName = companyNames.getText();
                }

                WebElement jobTitles = getElementIfExists(driver, "(//section[@id='category-" + sectionId + "']//li[" + i + "]/a//span[@class='title'])");
                if (jobTitles != null) {
                    jobTitle = jobTitles.getText();
                }

                WebElement jobLocations = getElementIfExists(driver, "(//section[@id='category-" + sectionId + "']//li[" + i + "]/a//span[@class='region company'])");
                if (jobLocations != null) {
                    jobLocation = jobLocations.getText();
                }

                WebElement jobURLs = getElementIfExists(driver, "(//section[@id='category-" + sectionId + "']//li[" + i + "]/a//span[@class='region company'])/parent::a");
                if (jobURLs != null) {
                    jobURL = jobURLs.getAttribute("href");
                }
                
		        String script = "window.open(arguments[0], '_blank');";
		        js.executeScript(script, jobURL);
		        sleepRandom();

				tabs = new ArrayList<>(driver.getWindowHandles());
				driver.switchTo().window(tabs.get(1));
				
				
				WebElement CompanyWebsites  = getElementIfExists(driver, "//div[@class='company-card border-box']//a[normalize-space()='Website']");
				if (CompanyWebsites != null) {
					companyWebsite = CompanyWebsites.getAttribute("href");
                }
				
				LocalDateTime now = LocalDateTime.now();
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
				dateCreated = now.format(formatter);
                
				jobDetailsList.add(new String[] {jobTitle, jobLocation, jobURL, companyName, employeeCount,
						companyWebsite, source, dateCreated});
				
				totalJobFinds++;
        	}	
	
        }
        
        }catch(Exception e) {
        	System.out.println("Code did not execute completely.-- "+source);
			e.printStackTrace();
			
			// takeScreenshot( driver,"error");
			
			 File screenshotFile = takeScreenshotGit(driver, "error");
			 commitScreenshot(screenshotFile);
			 
        }finally {
        	
        	try {
				Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
				String connectionURL = "jdbc:sqlserver://10.0.2.34:1433;Database=Automation;User=mailscan;Password=MailScan@343260;encrypt=true;trustServerCertificate=true";
				connection = DriverManager.getConnection(connectionURL);

				// SQL queries
				String checkSQL = "SELECT COUNT(*) FROM JobListings WHERE jobUrl = ?";
				ResultSet resultSet = null;
				// Check and insert jobs into the database
				String insertSQL = "INSERT INTO JobListings (jobTitle, jobLocations, jobUrl, companyName, employeeCount, companyWebsite, source, dateCreated) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
				
				for (String[] jobDetails : jobDetailsList) {
					String jobURL = jobDetails[2];

					// Check if job URL already exists
					PreparedStatement checkStatement = connection.prepareStatement(checkSQL);
					checkStatement.setString(1, jobURL);
					resultSet = checkStatement.executeQuery();
					if (resultSet.next() && resultSet.getInt(1) == 0) {
						// Insert new job listing
						PreparedStatement insertStatement = connection.prepareStatement(insertSQL);
						for (int j = 0; j < jobDetails.length; j++) {
							insertStatement.setString(j + 1, jobDetails[j]);
						}
						insertStatement.executeUpdate();
						insertStatement.close();
						totalJobsAppended++;
					}
					resultSet.close();
					checkStatement.close();
				}

				
				 if (totalJobCount == totalJobFinds) {
			            System.out.println("Searched all companies for new jobs.-- "+source);
			        }
				 
				if (totalJobsAppended > 0) {
					System.out.println(totalJobsAppended + " jobs added to DB successfully. -"+source);
				} else {
					System.out.println("No new jobs found.-- "+source);
				}
				if (driver != null) {
					driver.quit();
				}
				if (connection != null) {
					try {
						connection.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

			} catch (Exception e) {
				System.out.println("Error in Jobs adding to data base - "+source);
				e.printStackTrace();
			}
        	
        }

        driver.quit(); // Make sure to quit the WebDriver
        connection.close();
    }
    
    

    private static WebElement getElementIfExists(WebDriver driver, String xpath) {
    	try {
			WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
			return wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(xpath)));
		} catch (Exception e) {
			return null;
		}
    }

    private static void sleepRandom() {
        try {
            int delay = new Random().nextInt(2000) + 1000; // Delay between 1 and 2 seconds
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
	private static void takeScreenshot(WebDriver driver, String fileName) {
		try {
			TakesScreenshot ts = (TakesScreenshot) driver;
			File source = ts.getScreenshotAs(OutputType.FILE);
			String timestamp = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").format(LocalDateTime.now());
			File destination = new File("C:/Users/svegi/eclipse-workspace/WebScrapers/ExtendReports/screenshots/"
					+ fileName + "_" + timestamp + ".png");
			FileUtils.copyFile(source, destination);
			System.out.println("Screenshot taken: " + destination.getPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	   private static File takeScreenshotGit(WebDriver driver, String fileName) {
	        File screenshotFile = null;
	        try {
	            TakesScreenshot ts = (TakesScreenshot) driver;
	            screenshotFile = ts.getScreenshotAs(OutputType.FILE);
	            String timestamp = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").format(LocalDateTime.now());
	            
	            // Modify this path to your Git folder path
	            File destination = new File("ExtendReports/screenshots"
	                    + fileName + "_" + timestamp + ".png");
	            
	            FileUtils.copyFile(screenshotFile, destination);
	            System.out.println("Screenshot taken: " + destination.getPath());
	            
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	        return screenshotFile;
	    }

	   private static void commitScreenshot(File screenshotFile) {
		    try {
		        String command = "cmd /c git add \"" + screenshotFile.getPath() + "\" && " +
		                         "git commit -m \"Added screenshot for error\" && " +
		                         "git push";
		        
		        // Run the command in the terminal
		        Process process = Runtime.getRuntime().exec(command);
		        process.waitFor(); // Wait for the process to finish
		        
		        // Optional: Check if there are any errors in the output
		        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		        String line;
		        while ((line = reader.readLine()) != null) {
		            System.out.println(line);
		        }
		        
		    } catch (IOException | InterruptedException e) {
		        e.printStackTrace();
		    }
		}
}
