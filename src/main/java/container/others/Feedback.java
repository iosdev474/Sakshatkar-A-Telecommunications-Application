package container.others;

import lombok.Getter;
import lombok.Setter;

/**
 * <code>Container</code> used to hold <code>Feedback</code>
 */
@Getter
@Setter
public class Feedback {
    private String feedback;
    private int stars;

    /**
     * <code>Parameterised constructor</code>
     *
     * @param feedback Hold feedback message
     * @param stars    Number of stars given by <code>User</code>
     */
    public Feedback(String feedback, int stars) {
        this.feedback = feedback;
        this.stars = stars;
    }
}
