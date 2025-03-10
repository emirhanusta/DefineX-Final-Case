package patika.defineX.exception.custom;

public class ProjectNotFoundException extends RuntimeException {
    public ProjectNotFoundException(String s) {
        super(s);
    }
}
