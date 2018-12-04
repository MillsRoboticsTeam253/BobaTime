import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import java.io.ByteArrayInputStream;
import java.io.IOException;

class ImageProcess {
    private VideoCapture capture = new VideoCapture(0);
    private boolean stopCamera = false;
    private QRReader reader = new QRReader();
    UserProcess process = new UserProcess();

    void displayImage(GridPane root) {
        startWebCamStream(root);

    }

    private void startWebCamStream(GridPane root) {
        GridPane subRoot = new GridPane();
        stopCamera  = false;
        Size sz = new Size(600,400);

        Mat frame = new Mat();
        capture.read(frame);

        ImageView currentFrame = new ImageView();

        if (!capture.isOpened()) {
            System.out.println("Error opening camera");
            return;

        }

        Runnable frameGrabber = () -> {
            int prevID = 0;

            while (!stopCamera) {
                capture.read(frame);
                Imgproc.resize(frame, frame, sz);
                Core.flip(frame, frame, 1);
                MatOfByte buffer = new MatOfByte();
                Imgcodecs.imencode(".png", frame, buffer);
                Image imageToShow = new Image(new ByteArrayInputStream(buffer.toArray()));
                String data = QRReader.decodeQRCode(imageToShow);

                if (data != null) {
                    if (!(Integer.valueOf(data) == prevID)) {
                        if (process.isUserLoggedIn(data)) {
                            process.logoutUser(data);
                        } else {
                            process.loginUser(data);
                        }

                        prevID = Integer.valueOf(data);
                    }
                }

                Platform.runLater(() -> currentFrame.setImage(imageToShow));
            }
        };

        Thread t = new Thread(frameGrabber);
        t.setDaemon(true);
        t.start();

        subRoot.add(currentFrame, 0, 0);
        root.add(subRoot, 0, 0);
    }

}
