package container.profile;

import com.google.gson.Gson;
import lombok.Getter;
import lombok.Setter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

/**
 * <code>Container</code> used to maintain a single <code>profile Picture</code> of <code>User</code>.
 */
@Getter
@Setter
public class ProfilePicture {
    byte[] profileBufferedImageArray;


    /**
     * <code>Parameterised constructor</code>
     *
     * @param profileBufferedImageArray byte [] of Profile Picture of User
     */
    public ProfilePicture(byte[] profileBufferedImageArray) {
        this.profileBufferedImageArray = profileBufferedImageArray;
    }

    public ProfilePicture(BufferedImage profileBufferedImage, String fileType) {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ImageIO.write(profileBufferedImage, fileType, byteArrayOutputStream);
            this.profileBufferedImageArray = byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}