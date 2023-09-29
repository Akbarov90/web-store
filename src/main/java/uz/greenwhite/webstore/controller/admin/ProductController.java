package uz.greenwhite.webstore.controller.admin;


import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import uz.greenwhite.webstore.entity.Category;
import uz.greenwhite.webstore.entity.Product;
import uz.greenwhite.webstore.service.CategoryService;
import uz.greenwhite.webstore.service.ProductService;

import java.io.*;


@Controller
@AllArgsConstructor
@RequestMapping("/admin/data/product")
public class ProductController {

    private final ProductService service;

    private final CategoryService categoryService;

    private final String FILE_ROOT = "FILES";

    @GetMapping()
    public String listPage(Model model, Pageable pageable) {
        model.addAttribute("products", service.getAll(pageable));
        return "/admin/data/product/list";
    }


    @GetMapping("/add")
    public String addPage(Model model, Pageable pageable) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryService.getAll(pageable));
        return "/admin/data/product/add";
    }
//    @PostMapping("/add")
//    public String addProduct(@ModelAttribute Product product) {
//        service.create(product);
//
//        return "redirect:/admin/data/product";
//    }

    @PostMapping("/add")
    public String addProduct(@ModelAttribute Product product, @RequestParam("file") MultipartFile file) throws IOException {
        product = service.save(product);
        saveFile(product, file);
        return "redirect:/admin/data/product";
    }

    @GetMapping("/edit")
    public String editPage(Model model, @RequestParam("id") Long id,Pageable pageable) {
        Product product = service.getById(id);
        model.addAttribute("product", product);
        model.addAttribute("categories", categoryService.getAll(pageable));
        return "admin/data/product/add";
    }

    @PostMapping("/edit")
    public String editProduct(@ModelAttribute Product product,  @RequestParam("file") MultipartFile file) throws IOException {
        product = service.update(product);
        saveFile(product, file);
        return "redirect:/admin/data/product";
    }
    @GetMapping("/delete/{id}")
    public String deleteProduct(@PathVariable long id) {
        try {
            service.deleteById(id);
        } catch (RuntimeException e) {
            Product product = service.getById(id);
            product.setIsActive(false);
            service.update(product);
        }

        return "redirect:/admin/data/product";
    }

    @GetMapping("/image/{id}")
    public void image(@PathVariable Long id, HttpServletResponse response) {
        Product category = service.getById(id);
        File file = new File(FILE_ROOT + "/product/" + category.getPhoto());
        if (file.exists()) {
            response.setContentType("application/octet-stream");
            String headerKey = "Content-Disposition";
            String headerValue = String.format("inline; filename=\"%s\"", file.getName());
            response.setHeader(headerKey, headerValue);
            FileInputStream inputStream;
            try {
                inputStream = new FileInputStream(file);
                try {
                    int c;
                    while ((c = inputStream.read()) != -1) {
                        response.getWriter().write(c);
                    }
                } finally {
                    if (inputStream != null)
                        try {
                            inputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    response.getWriter().close();
                }
            } catch (IOException e) {
                e.printStackTrace();

            }
        } else {
            throw new RuntimeException("File not exist");
        }

    }


    public void saveFile(Product product, MultipartFile file) throws IOException {

        new File(FILE_ROOT + "/product").mkdir();

        String fileName = file.getOriginalFilename();
        String ext = "";
        if (fileName != null && fileName.contains(".")) {
            ext = fileName.substring(fileName.lastIndexOf("."));
        }
        File sf = new File(FILE_ROOT + "/product/" + product.getProductId() + ext);
        FileOutputStream fos = new FileOutputStream(sf);
        BufferedOutputStream bw = new BufferedOutputStream(fos);

        bw.write(file.getBytes());
        bw.close();
        fos.close();

        product.setPhoto(sf.getName());
        service.update(product);

    }

}