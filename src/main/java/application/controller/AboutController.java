package application.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import application.model.About;
import application.repository.AboutService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/")
@Api(value = "About Customer service")
public class AboutController {
	
	@Autowired
    AboutService aboutService;

    /**
     * @return about customer
     */
    @ApiOperation(value = "Manages all customer data")
    @GetMapping(path = "/about", produces = "application/json")
    @ResponseBody
    public About aboutInventory() {
        return aboutService.getInfo();
    }

}
