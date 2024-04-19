package gov.nist.hit.hl7.codeset.adapter.configuration;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.io.IOException;

@Controller
class ForwardingController {

    @RequestMapping(value = "/**")
    public void redirect(HttpServletResponse response) throws IOException {
        response.sendError(404, "Error");
    }
}