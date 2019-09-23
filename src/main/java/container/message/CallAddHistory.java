package container.message;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CallAddHistory {
    private int gID;
    private Call call;

    public CallAddHistory(int gID, Call call){
        this.call=call;
        this.gID=gID;
    }
}
