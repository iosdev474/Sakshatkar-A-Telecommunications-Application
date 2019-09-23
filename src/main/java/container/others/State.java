package container.others;

import lombok.Getter;
import lombok.Setter;

import java.awt.image.BufferedImage;

/**
 * <code>Container</code> used to hold a <code>User</code>'s video.audio call data like <code>BufferedImage</code>, <code>Audio</code>
 */
@Getter
@Setter
public class State {
    private BufferedImage bufferedImage;

    /**
     * <code>Parameterised constructor</code>
     *
     * @param bufferedImage
     */
    public State(BufferedImage bufferedImage) {
        this.bufferedImage = bufferedImage;
    }
}
