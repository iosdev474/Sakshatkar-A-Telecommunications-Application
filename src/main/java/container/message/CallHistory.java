package container.message;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CallHistory {
    private List<String> history;

    public CallHistory(List<String> history) {
        this.history = history;
    }
}
