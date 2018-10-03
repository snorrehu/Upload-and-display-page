import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
public class ViewController {

    @RequestMapping("/")
    public String index() {
        return "This is the upload page!";
    }

    @RequestMapping("/display")
    public String index() {
        return "This is the display page!";
    }

}