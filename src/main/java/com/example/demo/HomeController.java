package com.example.demo;

import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

@Controller
public class HomeController {
    @Autowired
    MessageRespository messageRespository;

    @Autowired
    CloudinaryConfig cloudc;

    @RequestMapping("/")
    public String MessageList(Model model) {
        model.addAttribute("messages", messageRespository.findAll());
        return "message";
    }

    @GetMapping("/add")
    public String listForm(Model model) {
        model.addAttribute("message", new Message());
        return "listform";
    }

    @PostMapping("/process")
    public String processForm(@Valid @ModelAttribute("message") Message message,
                              BindingResult result,
                              @RequestParam("file") MultipartFile file, @RequestParam("postedDate") String postedDate) {
        System.out.println("object = " + message );
        //check for errors on the form
        if (result.hasErrors() ){
            for (ObjectError e : result.getAllErrors()){
                System.out.println(e);
            }
            return "messageform";
        }
        Date date = new Date();

        try {
            date = new SimpleDateFormat("yyyy-MM-dd").parse(postedDate);
            message.setPostedDate(date);
        }
        catch(Exception e){
            e.printStackTrace();
            return "redirect:/listform";
        }

        //if there is a picture path and file is empty then save message
        if(message.getPicture() != null && file.isEmpty()){
            messageRespository.save(message);
            return "redirect:/";
        }

        if(file.isEmpty()){
            messageRespository.save(message);
            return "redirect:/";
        }
        else {
            Map uploadResult;
            try {
                uploadResult = cloudc.upload(
                        file.getBytes(), ObjectUtils.asMap("resourcetype", "auto"));
            } catch (IOException e) {
                e.printStackTrace();
                return "redirect:/messageform";
            }
            String url = uploadResult.get("url").toString();
            int i = url.lastIndexOf('/');
            url = url.substring(i + 1);
            url = "http://res.cloudinary.com/ajkmonster/image/upload/w_300,h_300/" + url;
            message.setPicture(url);
            messageRespository.save(message);
            return "redirect:/";
        }
    }
    @RequestMapping("/detail/{id}")
    public String showToDoList(@PathVariable("id") long id, Model model) {
        model.addAttribute("message", messageRespository.findById(id).get());
        return "show";
    }

    @RequestMapping("/update/{id}")
    public String updateToDoList(@PathVariable("id") long id, Model model) {
        model.addAttribute("message", messageRespository.findById(id).get());
        return "listform";
    }

    @RequestMapping("/delete/{id}")
    public String delToDoList(@PathVariable("id") long id) {
        messageRespository.deleteById(id);
        return "redirect:/";
    }
}
