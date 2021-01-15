import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgcodecs.Imgcodecs;

public class RingRecognition {

    public static void main(String[] args) {
        try {
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

            Imgcodecs image = new Imgcodecs();

            String base = "C:\\Users\\conta\\RingOpenCV\\src\\Images";
            String path = base + "\\g888XYG.jpg";
            Mat mat = image.imread(path);

            RingAnalysis ring = new RingAnalysis();

            ring.init(mat);
            Mat res = ring.processFrame(mat);

            Imgcodecs.imwrite("analyze.jpg", res);

            System.out.println(ring.getPosition());

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }

    }

    public static class RingAnalysis
    {

        public enum RingPosition
        {
            FOUR,
            ONE,
            NONE
        }

        static final Scalar BLUE = new Scalar(0, 0, 255);
        static final Scalar GREEN = new Scalar(0, 255, 0);

//        VARIABLES TO EDIT AND TEST
        static final Point REGION1_TOPLEFT_ANCHOR_POINT = new Point(1700,1300);

        static final int REGION_WIDTH = 800;
        static final int REGION_HEIGHT = 750;

        final int FOUR_RING_THRESHOLD = 117;
        final int ONE_RING_THRESHOLD = 122;
//        END OF VARIABLES TO TEST

        Point region1_pointA = new Point(
                REGION1_TOPLEFT_ANCHOR_POINT.x,
                REGION1_TOPLEFT_ANCHOR_POINT.y);
        Point region1_pointB = new Point(
                REGION1_TOPLEFT_ANCHOR_POINT.x + REGION_WIDTH,
                REGION1_TOPLEFT_ANCHOR_POINT.y + REGION_HEIGHT);

        /*
         * Working variables
         */
        Mat region1_Cb;
        Mat YCrCb = new Mat();
        Mat Cb = new Mat();
        int avg1;

        // Volatile since accessed by OpMode thread w/o synchronization
        private volatile RingPosition position = RingPosition.FOUR;

        public RingPosition getPosition() {
            return (position);
        }
        /*
         * This function takes the RGB frame, converts to YCrCb,
         * and extracts the Cb channel to the 'Cb' variable
         */
        void inputToCb(Mat input)
        {
            Imgproc.cvtColor(input, YCrCb, Imgproc.COLOR_RGB2YCrCb);
            Core.extractChannel(YCrCb, Cb, 1);
        }

        public void init(Mat firstFrame)
        {
            inputToCb(firstFrame);

            region1_Cb = Cb.submat(new Rect(region1_pointA, region1_pointB));
        }

        public Mat processFrame(Mat input)
        {
            inputToCb(input);

            avg1 = (int) Core.mean(region1_Cb).val[0];

            Imgproc.rectangle(
                    input,
                    region1_pointA,
                    region1_pointB,
                    BLUE,
                    2
            );

            position = RingPosition.FOUR;
            if(avg1 < FOUR_RING_THRESHOLD){
                position = RingPosition.FOUR;
            }else if (avg1 < ONE_RING_THRESHOLD){
                position = RingPosition.ONE;
            }else{
                position = RingPosition.NONE;
            }

            Imgproc.rectangle(
                    input,
                    region1_pointA,
                    region1_pointB,
                    GREEN,
                    -1
            );

            return input;
        }

//        USE TO FINETUNE THE THRESHHOLDS
        public int getAnalysis()
        {
            return avg1;
        }
    }

}
