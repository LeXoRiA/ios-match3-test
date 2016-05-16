package QA;

/**
 * Created by Eligra on 23.2.2016.
 */

import io.appium.java_client.TouchAction;
import io.appium.java_client.ios.IOSDriver;
import nu.pattern.OpenCV;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.opencv.core.*;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.*;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;



public abstract class Helpers
{
    public static IOSDriver driver;

    public static URL serverAddress;
    private static WebDriverWait driverWait;
    public static String screenshotsFolder;

    /**
     * Initialize the webdriver. Must be called before using any helper methods. *
     */

    public static void init(IOSDriver MobileDriver, URL driverServerAddress)
    {
        driver = MobileDriver;
        serverAddress = driverServerAddress;
        int timeoutInSeconds = 60;
        // must wait at least 60 seconds for running on Sauce.
        // waiting for 30 seconds works locally however it fails on Sauce.
        driverWait = new WebDriverWait(MobileDriver, timeoutInSeconds);
    }

    /**
     * Set implicit wait in seconds *
     */
    public static void setWait(int seconds) {
        driver.manage().timeouts().implicitlyWait(seconds, TimeUnit.SECONDS);
    }

    /**
     * Return an element by locator *
     */
    public static WebElement element(By locator) {
        return driver.findElement(locator);
    }

    /**
     * Return a list of elements by locator *
     */
    public static List<WebElement> elements(By locator) {
        return driver.findElements(locator);
    }

    /**
     * Press the back button *
     */
    public static void back() {
        driver.navigate().back();
    }

    /**
     * Return a list of elements by tag name *
     */
    public static List<WebElement> tags(String tagName) {
        return elements(for_tags(tagName));
    }

    /**
     * Return a tag name locator *
     */
    public static By for_tags(String tagName) {
        return By.className(tagName);
    }

    /**
     * Return a static text element by xpath index *
     */
    public static WebElement s_text(int xpathIndex) {
        return element(for_text(xpathIndex));
    }

    /**
     * Return a static text locator by xpath index *
     */
    public static By for_text(int xpathIndex) {
        return By.xpath("//android.widget.TextView[" + xpathIndex + "]");
    }

    /**
     * Return a static text element that contains text *
     */
    public static WebElement text(String text) {
        return element(for_text(text));
    }

    /**
     * Return a static text locator that contains text *
     */
    public static By for_text(String text) {
        return By.xpath("//android.widget.TextView[contains(@text, '" + text + "')]");
    }

    /**
     * Return a static text element by exact text *
     */
    public static WebElement text_exact(String text) {
        return element(for_text_exact(text));
    }

    /**
     * Return a static text locator by exact text *
     */
    public static By for_text_exact(String text) {
        return By.xpath("//android.widget.TextView[@text='" + text + "']");
    }

    public static By for_find(String value) {
        return By.xpath("//*[@content-desc=\"" + value + "\" or @resource-id=\"" + value +
                "\" or @text=\"" + value + "\"] | //*[contains(translate(@content-desc,\"" + value +
                "\",\"" + value + "\"), \"" + value + "\") or contains(translate(@text,\"" + value +
                "\",\"" + value + "\"), \"" + value + "\") or @resource-id=\"" + value + "\"]");
    }

    public static WebElement find(String value) {
        return element(for_find(value));
    }

    /**
     * Return an element that contains name or text *
     */
    public static WebElement scroll_to(String value) {
        return driver.scrollTo(value);
    }

    /**
     * Return an element that exactly matches name or text *
     */
    public static WebElement scroll_to_exact(String value) {
        return driver.scrollToExact(value);
    }



    ////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////// EVERYTHING STARTS HERE /////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////


    public void log(String msg)
    {
        Date dNow = new Date();
        SimpleDateFormat tmfr = new SimpleDateFormat("kk:mm:ss");
        System.out.println(tmfr.format(dNow) + " - " + msg);
    }

    public void sleep(int seconds) throws Exception
    {
        Thread.sleep(seconds * 1000);
    }

    // Take screenshot of application while it's running.
    public boolean takeScreenshot(final String name, IOSDriver _driver2)
    {
        String screenshotDirectory = System.getProperty("appium.screenshots.dir", System.getProperty("java.io.tmpdir", ""));
        File screenshot = ((TakesScreenshot) _driver2).getScreenshotAs(OutputType.FILE);
        return screenshot.renameTo(new File(screenshotDirectory, String.format("/%s.png", name)));
    }

    // Save image from URL (AWS S3)
    public void saveImage (String imageUrl, String destinationFile, IOSDriver _driver2) throws Exception
    {
        String screenshotDirectory = System.getProperty("appium.screenshots.dir", System.getProperty("java.io.tmpdir", ""));

        URL url = new URL(imageUrl);
        InputStream is = url.openStream();
        OutputStream os = new FileOutputStream(destinationFile);

        byte[] b = new byte[2048];
        int length;

        while ((length = is.read(b)) != -1)
        {
            os.write(b, 0, length);
        }

        is.close();
        os.close();

        return;
    }

    // Resize image to Canny and match.
    public String resizeCanny(String imageCanny, String resizedCanny, String resultCanny, int width, int height, int inter) throws Exception
    {
        String screenshotDirectory = System.getProperty("appium.screenshots.dir", System.getProperty("java.io.tmpdir", ""));

        String imageCannyStr = screenshotDirectory + imageCanny;
        String resizedCannyStr = screenshotDirectory + resizedCanny;
        String resultCannyStr = screenshotDirectory + resultCanny;

        // Create array for new dimensions and get H and W of image.
        int[] dim = new int[2];
        int hCan = Highgui.imread(imageCannyStr).height();
        int wCan = Highgui.imread(imageCannyStr).width();

        // Conditions:
        if (width == 0 && height == 0)
        {
            return imageCannyStr;
        } // end if
        else if (width == 0)
        {
            float r = height / (float) hCan;
            dim[0] = (int)(wCan*r);
            dim[1] = height;
        } // end else if
        else
        {
            float r = width/(float)wCan;
            dim[0] = width;
            dim[1] = (int)(hCan*r);
        } // end else

        // Some matrix conversions.
        Mat imageCannyMat = Highgui.imread(imageCannyStr);
        Mat resizedCannyMat = Highgui.imread(resizedCannyStr);

        // Get new dimension after conditions to resize image.
        int nW = dim[0];
        int nH = dim[1];

        // Resize and write the image.
        Size newDim = new Size(nW, nH);
        Imgproc.resize(imageCannyMat, resizedCannyMat, newDim);
        Highgui.imwrite(resizedCannyStr, resizedCannyMat);

        return resizedCannyStr;
    } // end resizeCanny

    // Match template and image and then click/swipe.
    public void Canny (String template, String image, String imageGray, String imageCanny, String resizedCanny, String resultCanny, String matchCase,
                       String outFile, IOSDriver _driver2) throws Exception
    {
        String screenshotDirectory = System.getProperty("appium.screenshots.dir", System.getProperty("java.io.tmpdir", ""));
        OpenCV.loadShared();

/*Keep Appium alive*/ _driver2.getOrientation();

        String templateStr = screenshotDirectory + template;
        String imageStr = screenshotDirectory + image;
        String imageGrayStr = screenshotDirectory + imageGray;
        String imageCannyStr = screenshotDirectory + imageCanny;
        String resizedCannyStr = screenshotDirectory + resizedCanny;
        String resultCannyStr = screenshotDirectory + resultCanny;
        String outFileStr = screenshotDirectory + outFile;

        // Variables for linspace.
        double linStart;
        double linEnd;
        double counter;
        double space;

        // Read template, convert it to gray and Canny it.
        Mat templateMat = Highgui.imread(templateStr);

        Imgproc.cvtColor(templateMat, templateMat, Imgproc.COLOR_BGR2GRAY);
        Highgui.imwrite(templateStr, templateMat);

        Imgproc.Canny(templateMat, templateMat, 50, 200);
        Highgui.imwrite(templateStr, templateMat);

        Mat templateMatchMat = Highgui.imread(templateStr);

        // Get Height and Width of template image.
        int tH = Highgui.imread(templateStr).height();
        int tW = Highgui.imread(templateStr).width();

        // Start counter
        int ctr = 0;

        // Read image and convert it to gray.
        Mat imageMat = Highgui.imread(imageStr);
        Mat imgGryMat = Highgui.imread(imageStr);

        Imgproc.cvtColor(imageMat, imgGryMat, Imgproc.COLOR_BGR2GRAY);
        Highgui.imwrite(imageGrayStr, imgGryMat);

        // Some parameters to use in every turn of for loop.
        double[] found = new double[4];
        Point mLoc = null;
        float r = 0;

        // Values for linspace and for loop.
        linStart = 1.0;
        linEnd = 0.2;
        counter = 20;
        space = (linStart - linEnd) / counter;

        // For loop. The mothership of automation.
        for (double scale = linStart; scale >= linEnd; scale = scale - space)
        {
/*Keep Appium alive*/ _driver2.getOrientation();

            // Get H and W of grayed image. And multiply the width with scale for multi-scale.
            int gryW = Highgui.imread(imageGrayStr).width();
            double newWidth = gryW * scale;
            int gryH = Highgui.imread(imageGrayStr).height();

            // Change the name for easy use in resizeCanny() function.
            imageCanny = imageGray;

            // Start resizeCanny function. It resizes the image to Canny and match for later.
            resizeCanny(imageCanny, resizedCanny, resultCanny, (int) newWidth, gryH, Imgproc.INTER_AREA);

            // Get H and W of resized image.
            int rszH = Highgui.imread(resizedCannyStr).height();
            int rszW = Highgui.imread(resizedCannyStr).width();

            // r = grayed image's width / resized image's width.
            r = gryW / (float) rszW;

            // If resized image is smaller than template, then break.
            if (rszH < tH || rszW < tW)
            {
                break;
            }

            // Some matrix conversion.
            Mat resizedCannyMat = Highgui.imread(resizedCannyStr);
            String edged = resizedCannyStr;
            Mat edgedMat = Highgui.imread(edged);

            // Canny and write the image that has been resized.
            Imgproc.Canny(resizedCannyMat, edgedMat, 50, 200);
            Highgui.imwrite(resultCannyStr, edgedMat);

            // Some matrix conversions.
            Mat resultCannyMat = Highgui.imread(resultCannyStr);
            String matchResult = resultCannyStr;
            Mat matchResultMat = Highgui.imread(matchResult);

            // Match Canny'd template and Canny'd image.
            Imgproc.matchTemplate(resultCannyMat, templateMatchMat, matchResultMat, Imgproc.TM_CCOEFF);

            // Get maximum value and maximum location.
            Core.MinMaxLocResult mmrValues = Core.minMaxLoc(matchResultMat);
            mLoc = mmrValues.maxLoc;
            double mVal = mmrValues.maxVal;

            // If found array is empty maximum value is bigger than previous max value, then update the variables.
            if (found == null || mVal > found[0])
            {
                found[0] = mVal;
                found[1] = mLoc.x;
                found[2] = mLoc.y;
                found[3] = (double) r;
            } // end if
        } // end for

/*Keep Appium alive*/ _driver2.getOrientation();

        // After for loop; update maximum location pointers (x,y) with found array to choose/show.
        mLoc.x = found[1];
        mLoc.y = found[2];
        r = (float)found[3];

        // Found template's edges.
        int startX, startY;
        startX = (int) ((mLoc.x) * r);
        startY = (int) ((mLoc.y) * r);
        int endX, endY;
        endX = (int) ((mLoc.x + tW) * r);
        endY = (int) ((mLoc.y + tH) * r);

//        // Draw rectangle on match.
////////////        Core.rectangle(imageMat, new Point(startX, startY), new Point(endX, endY), new Scalar(0, 0, 255));
//
//        // Write the matched imaged to show if it's true or not.
////////////        log("Writing image as " + outFile);
////////////        Highgui.imwrite(outFileStr, imageMat);


        // Get dimensions

        // Get device dimensions
        double deviceResH = _driver2.manage().window().getSize().getHeight();
        double deviceResW = _driver2.manage().window().getSize().getWidth();
        log("Device dimensions: " + deviceResH + "x" + deviceResW);

        // Take temproray screenshot
        String temprSSname = "temprSS";
        File screenshot = ((TakesScreenshot) _driver2).getScreenshotAs(OutputType.FILE);
        screenshot.renameTo(new File(screenshotDirectory, String.format("/%s.png", temprSSname)));

        String newSS = screenshotDirectory + "/" + temprSSname + ".png";

        //Get screenshot dimensions
        double ssH = Highgui.imread(newSS).height();
        double ssW = Highgui.imread(newSS).width();

        // Calculate the point for iOS devices
        int k = (int)ssH / (int)deviceResH;
        int m = (int)ssW / (int)deviceResW;

        int startXF = (startX/m) + 5;
        int startYF = (startY/k) + 5;

    /*Keep Appium alive*/ _driver2.getOrientation();

        // Make your move
        if (matchCase.equalsIgnoreCase("Down"))
        {
            TouchAction action = new TouchAction (_driver2);
            action.longPress(startXF, startYF).moveTo(0, 15).release().perform();
        }
        else if (matchCase.equalsIgnoreCase("Right"))
        {
            TouchAction action = new TouchAction (_driver2);
            action.longPress(startXF, startYF).moveTo(15, 0).release().perform();
        }
        else if (matchCase.equalsIgnoreCase("Up"))
        {
            TouchAction action = new TouchAction(_driver2);
            action.longPress(startXF, startYF).moveTo(0, -20).release().perform();
        }
        else if (matchCase.equalsIgnoreCase("Left"))
        {
            TouchAction action = new TouchAction(_driver2);
            action.longPress(startXF, startYF).moveTo(-20, 0).release().perform();
        }
        else
        {
            _driver2.tap(1, startXF, startYF, 20);
        }



        return;
    } // end Canny

    // JSON Collector and FINISHER
    public void actionStations(String fileName, IOSDriver _driver2) throws Exception
    {
        String screenshotDirectory = System.getProperty("appium.screenshots.dir", System.getProperty("java.io.tmpdir", ""));

        try
        {
            String jsonFile = screenshotDirectory + "/" + fileName;
            URL link = new URL("https://s3.amazonaws.com/infosfer-ab-test/jsonfiles/" + fileName + ".json");

            //Code to download JSON and read it
            InputStream in = new BufferedInputStream(link.openStream());
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int q = 0;
            while (-1 != (q = in.read(buf)))
            {
                out.write(buf, 0, q);
            }
            out.close();
            in.close();
            byte[] response = out.toByteArray();

            FileOutputStream fos = new FileOutputStream(jsonFile);
            fos.write(response);
            fos.close();
            //End download code

            log("JSON File has been saved!");

            FileReader reader = new FileReader(jsonFile);
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(reader);

            JSONObject jsonObject = (JSONObject) obj;

            JSONArray functionList = (JSONArray) jsonObject.get("Functions");

            int n = 0;
            while (n < functionList.size())
            {
                JSONObject jObject = (JSONObject) functionList.get(n);

                String methodType = (String) jObject.get("methodType");

                if (methodType.equals("imageRec"))
                {
                    log("IR action started");

                    String name = (String) jObject.get("screenshotNameObj");
                    String imageUrl = "http://infosfer-ab-test.s3-website-us-east-1.amazonaws.com/tmpics/" + (String) jObject.get("imageURLObj") + ".png";
                    String destinationFile = screenshotDirectory + "/" + jObject.get("destinationImageObj") + ".png";
                    String template = "/" + (String) jObject.get("templateNameObj") + ".png";
                    String image = "/" + (String) jObject.get("sourceNameObj") + ".png";
                    String imageGray = "/" + (String) jObject.get("grayedSourceObj") + ".png";
                    String imageCanny = "/" + (String) jObject.get("cannySourceObj") + ".png";
                    String resizedCanny = "/" + (String) jObject.get("resizedCannyObj") + ".png";
                    String resultCanny = "/" + (String) jObject.get("cannyResultObj") + ".png";
                    String matchCase = (String) jObject.get("actionObj");
                    String outFile = "/" + (String) jObject.get("outImageObj") + ".png";
                    long seconds = (Long) jObject.get("sleepTimeObj");
                    int second = (int) seconds;

                    takeScreenshot(name, _driver2);
                    log("Screenshot captured");
                    saveImage(imageUrl, destinationFile, _driver2);
                    log("Template has been saved from server");
                    Canny(template, image, imageGray, imageCanny, resizedCanny, resultCanny, matchCase, outFile, _driver2);
                    n++;
                    log("Action done (IR)");
                    sleep(second);

                }
                else if (methodType.equals("location"))
                {

                    log("Loc action started");

                    long locStartL = (Long) jObject.get("startPositionObj");
                    int locStart = (int) locStartL;

                    long locEndL = (Long) jObject.get("endPositionObj");
                    int locEnd = (int) locEndL;

                    long seconds = (Long) jObject.get("sleepTimeObj");
                    int second = (int) seconds;

                    String resolution = new String();

                    int originH, originW;
                    int startXL, startYL;
                    int endXL, endYL;

                    originH = _driver2.manage().window().getSize().getHeight();
                    originW = _driver2.manage().window().getSize().getWidth();

                    int origin[] = new int[2];

                    origin[0] = originW / 2;
                    origin[1] = originH / 2;

                    int tempA, tempB;
                    int k = 20; //px (for CK)
                    double m;

                    /* Ratio for piece coordinates */
                    m = ((float) originH / 2560.f) * 4.5;

                    log("m: " + m);
                    double E = k * m;

                    int pointA, pointB;

                    Point[] ckCells = new Point[64];
                    int i = -7;
                    int j = -7;

                    for (int a = 0; a < 64; a++)
                    {
                        tempA = (int) (E * j);
                        tempB = (int) (E * i);

                        pointA = origin[0] + tempA;
                        pointB = origin[1] + tempB;

                        Point cellLoc = new Point(pointA, pointB);
                        ckCells[a] = cellLoc;

                        cellLoc.x = pointA;
                        cellLoc.y = pointB;

                        j = j + 2;

                        if ((a + 1) % 8 == 0)
                        {
                            i = i + 2;
                            j = -7;
                        } //end if
                    } //end for

                    Point cellStart = ckCells[locStart];

                    startXL = (int) cellStart.x;
                    startYL = (int) cellStart.y;

                    Point cellEnd = ckCells[locEnd];

                    endXL = (int) cellEnd.x;
                    endYL = (int) cellEnd.y;

                    log("startX: " + startXL);
                    log("startY: " + startYL);
                    log("endX: " + endXL);
                    log("endY: " + endYL);

                    TouchAction action = new TouchAction (_driver2);
                    action.longPress(startXL, startYL).moveTo((endXL - startXL), (endYL - startYL)).release().perform();

                    n++;

                    log("Action done (Loc)");
                    sleep(second);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}




