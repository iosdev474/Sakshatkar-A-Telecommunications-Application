package scene;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamException;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXToggleButton;
import com.sun.org.apache.xml.internal.security.Init;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import lombok.extern.slf4j.Slf4j;
import main.DAO;
import main.Utility;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

@Slf4j
public class VideoHomePageController extends SceneManager implements Initializable {

    @FXML
    private ImageView videoImageView;
    @FXML
    private JFXButton join;
    @FXML
    private JFXToggleButton video;
    @FXML
    private JFXToggleButton audio;
    @FXML
    private JFXToggleButton present;
    @FXML
    private JFXButton back;

    volatile boolean showVideo = true;

    @FXML
    void back(ActionEvent event) {
        showVideo = false;
        webcam.close();
        changeScene(event, DAO.homePage);
    }

    @FXML
    void join(ActionEvent event) {
        DAO.currentVideoCallConfig.setAudioEnabled(audio.isSelected());
        DAO.currentVideoCallConfig.setVideoEnabled(video.isSelected());
        DAO.currentVideoCallConfig.setPresentEnabled(present.isSelected());
        showVideo = false;
        changeScene(event, DAO.videoPage);
    }

    boolean audioEnabled = false;
    boolean videoEnabled = false;
    boolean presentEnabled = false;

    @FXML
    void audioT(ActionEvent event) {
        if (audio.isSelected()) {
            log.info("Audio Enabled");
        }
    }


    @FXML
    void presentT(ActionEvent event) {
        if (present.isSelected()) {
            log.info("Present Enabled");
            video.setSelected(false);
        }
    }

    @FXML
    void videoT(ActionEvent event) {
        if (video.isSelected()) {
            webcam = Webcam.getDefault();
            webcam.open();
            log.info("video Enabled");
            present.setSelected(false);
        }
    }

    volatile Webcam webcam;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        audio.setSelected(DAO.currentVideoCallConfig.getAudioEnabled());
        video.setSelected(DAO.currentVideoCallConfig.getVideoEnabled());
        present.setSelected(DAO.currentVideoCallConfig.getPresentEnabled());
        log.info("DAO currentVideoConfig loaded A:{} V:{} P:{}", DAO.currentVideoCallConfig.getAudioEnabled(), DAO.currentVideoCallConfig.getVideoEnabled(), DAO.currentVideoCallConfig.getPresentEnabled());
        Thread thread = new Thread(() -> {
            BufferedImage grabbedImage;
            webcam = Webcam.getDefault();
            if (videoEnabled) {
                try {
                    webcam.open();
                    DAO.webcamLocked = false;
                } catch (WebcamException e) {
                    DAO.webcamLocked = true;
                }
            } else {

            }

            ObjectProperty<Image> imageProperty = new SimpleObjectProperty<Image>();
            videoImageView.imageProperty().bind(imageProperty);
            while (showVideo) {
                try {
                    if (DAO.webcamLocked) {
                        grabbedImage = ImageIO.read(new File(DAO.testJPGImage));
                        final Image img = SwingFXUtils.toFXImage(grabbedImage, null);
                        imageProperty.set(img);
                        grabbedImage.flush();
                    } else if (video.isSelected()) {
                        grabbedImage = webcam.getImage();
                        if(grabbedImage==null)
                            continue;
                        final Image img = SwingFXUtils.toFXImage(grabbedImage, null);
                        imageProperty.set(img);
                        grabbedImage.flush();
                    } else if (present.isSelected()) {
                        grabbedImage = Utility.getDesktop();
                        if(grabbedImage==null)
                            continue;
                        final Image img = SwingFXUtils.toFXImage(grabbedImage, null);
                        imageProperty.set(img);
                        grabbedImage.flush();
                    } else {
                        grabbedImage = ImageIO.read(new File(DAO.emptyJPGImage));
                        final Image img = SwingFXUtils.toFXImage(grabbedImage, null);
                        imageProperty.set(img);
                        grabbedImage.flush();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }
}