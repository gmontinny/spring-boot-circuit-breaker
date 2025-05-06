package com.gmontinny.springbootcircuitbreaker.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controlador para a página inicial que redireciona para o dashboard.
 */
@Controller
public class HomeController {

    /**
     * Redireciona a URL raiz para o dashboard.
     *
     * @return Redirecionamento para o dashboard
     */
    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }
}
