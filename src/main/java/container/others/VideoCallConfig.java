package container.others;

import lombok.Getter;
import lombok.Setter;

/**
 * <code>Container</code> used to hold a <code>VideoCallConfig</code>
 */
@Getter
@Setter
public class VideoCallConfig {
    private String password;
    private int port;
    private boolean videoEnabled = true;
    private boolean audioEnabled = true;
    private boolean presentEnabled = false;
    private String ip;

    /**
     * <code>Parameterised constructor</code>
     *
     * @param password Used to secure a video/audio call
     * @param port     Port Number at which video call server is running
     */
    public VideoCallConfig(String password, String ip, int port) {
        this.password = password;
        this.port = port;
        this.ip = ip;
    }

    public void setVideoEnabled(Boolean videoEnabled) {
        this.videoEnabled = videoEnabled;
    }

    public void setAudioEnabled(Boolean audioEnabled) {
        this.audioEnabled = audioEnabled;
    }

    public void setPresentEnabled(Boolean presentEnabled) {
        this.presentEnabled = presentEnabled;
    }

    public Boolean getVideoEnabled() {
        return videoEnabled;
    }

    public Boolean getAudioEnabled() {
        return audioEnabled;
    }

    public Boolean getPresentEnabled() {
        return presentEnabled;
    }


}
