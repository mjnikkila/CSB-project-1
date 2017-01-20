package sec.project.controller;

import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import sec.project.domain.Signup;
import sec.project.repository.SignupRepository;

@Controller
public class AdminController {

    @Autowired
    private SignupRepository signupRepository;

    @RequestMapping(value = "/admin")
    public String admin(HttpSession session) {
        session.setAttribute("login_ok", false);
        return "admin/login";
    }
    
    @RequestMapping(value = "/admin/login", method = RequestMethod.POST)
    public String adminLogin(@RequestParam String password, HttpSession session) {
        if(password.matches("victory")) {
            session.setAttribute("login_ok", true);
            return "redirect:/admin/list";
        }
        
        return "redirect:/admin";
    }
    
    @RequestMapping(value = "/admin/list")
    public String list(Model model, HttpSession session) {
        if(!this.check_login(session)) return "redirect:/admin";
        
        model.addAttribute("signups", signupRepository.findAll());
        return "admin/list";
    }
    
    @RequestMapping(value = "/admin/delete", method = RequestMethod.GET)
    public String delete(@RequestParam Long id, Model model, HttpSession session) {
        if(!this.check_login(session)) return "redirect:/admin";
   
        Signup signup = signupRepository.findOne(id);
        
        if(signup != null) {
            signupRepository.delete(id);
        }
        
        return "redirect:/admin/list";
        
    }
    
    @RequestMapping(value = "/admin/details", method = RequestMethod.GET)
    public String details(@RequestParam Long id, Model model, HttpSession session) {
        if(!this.check_login(session)) return "redirect:/admin";
        model.addAttribute("signup", signupRepository.findOne(id));
        return "admin/details";
    }
    
    @RequestMapping(value = "/admin/details", method = RequestMethod.POST)
    public String saveDetails(@RequestParam Long id, @RequestParam String firstname, @RequestParam String lastname, 
            @RequestParam String email, @RequestParam String phone_number, @RequestParam String address, HttpSession session) {
        if(!this.check_login(session)) return "redirect:/admin";
        
        Signup signup = signupRepository.findOne(id);
        signup.setFirstname(firstname);
        signup.setLastname(lastname);
        signup.setEmail(email);
        signup.setPhone_number(phone_number);
        signup.setAddress(address);
        signupRepository.save(signup);
        
        return "redirect:/admin/list";
    }
    
    private Boolean check_login(HttpSession session) {
        Boolean ok = (Boolean) session.getAttribute("login_ok");
        return ok;
    }
}
