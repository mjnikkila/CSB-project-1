package sec.project.controller;

import java.util.Arrays;
import javax.servlet.http.HttpSession;
import org.apache.catalina.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.tomcat.TomcatContextCustomizer;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import sec.project.domain.Signup;
import sec.project.repository.SignupRepository;

@Controller
public class SignupController {

    @Autowired
    private SignupRepository signupRepository;

    @RequestMapping("*")
    public String defaultMapping() {
        return "redirect:/form";
    }

    @RequestMapping(value = "/form", method = RequestMethod.GET)
    public String loadForm() {
        return "form";
    }

    @RequestMapping(value = "/form", method = RequestMethod.POST)
    public String submitForm(@RequestParam String firstname, @RequestParam String lastname, 
            @RequestParam String email, @RequestParam String phone_number, @RequestParam String address, 
            @RequestParam String password) {
        signupRepository.save(new Signup(firstname, lastname, email, phone_number, address, password));
        return "done";
    }
    
    @RequestMapping(value = "/edit_login")
    public String editSignupLoginForm() {
        return "edit_login";
    }
    
    @RequestMapping(value = "/edit", method = RequestMethod.POST)
    public String editSignupLogin(@RequestParam String email, @RequestParam String password) {
        Signup signup = signupRepository.findByEmail(email);
        return "redirect:/edit?id="+signup.getId();
    }
    
    @RequestMapping(value = "/edit", method = RequestMethod.GET)
    public String editSignup(@RequestParam Long id, Model model) {
        Signup signup = signupRepository.findOne(id);
        model.addAttribute("signup", signup);
        return "edit";
    }
    
    @RequestMapping(value = "/edit/save", method = RequestMethod.POST)
    public String saveEdit(@RequestParam Long id, @RequestParam String firstname, @RequestParam String lastname, 
            @RequestParam String email, @RequestParam String phone_number, @RequestParam String address, HttpSession session) {
        
        Signup signup = signupRepository.findByEmail(email);
        signup.setFirstname(firstname);
        signup.setLastname(lastname);
        signup.setEmail(email);
        signup.setPhone_number(phone_number);
        signup.setAddress(address);
        signupRepository.save(signup);
        
        return "redirect:/edit?id="+id;
    }
    
    @Bean
    public EmbeddedServletContainerFactory servletContainer() {
        TomcatEmbeddedServletContainerFactory factory = new TomcatEmbeddedServletContainerFactory();
        factory.setTomcatContextCustomizers(Arrays.asList(new CustomCustomizer()));
        return factory;
    }

    static class CustomCustomizer implements TomcatContextCustomizer {
    @Override
    public void customize(Context context) {
        context.setUseHttpOnly(false);
    }
}
}
