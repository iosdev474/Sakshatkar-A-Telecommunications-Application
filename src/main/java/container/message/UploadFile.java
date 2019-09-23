package container.message;

import lombok.Getter;
import lombok.Setter;

import java.io.File;

@Getter
@Setter
public class UploadFile {
    private byte[] file;
    public UploadFile(byte[] file){
        this.file=file;
    }
}
