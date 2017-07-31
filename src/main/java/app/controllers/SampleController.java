package app.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
public class SampleController {

  @RequestMapping(value = "/sample", method = RequestMethod.GET)
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws Exception {
    resp.getWriter().write("GET /sample");
  }

  @RequestMapping(value = "/sample", method = RequestMethod.POST)
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws Exception {
    resp.getWriter().write("POST /sample");
  }
}
