package org.example.bootsecurity.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.bootsecurity.model.domain.Memo;
import org.example.bootsecurity.model.domain.MemoForm;
import org.example.bootsecurity.service.MemoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.RequestContextUtils;

import java.util.Map;


@Controller
public class MainController {
    private final MemoService memoService;

    public MainController(MemoService memoService) {
        this.memoService = memoService;
    }

    @GetMapping("/")
    public String index(Model model, HttpServletRequest request) {
        model.addAttribute("memoList", memoService.findAll());

        // *** Add an empty form object for the 'add' form ***
        if (!model.containsAttribute("memoForm")) { // Add only if not already present (e.g., from a failed validation redirect)
            model.addAttribute("memoForm", new MemoForm());
        }

        // *** Explicitly check for flash attributes (Optional but good practice) ***
        // Spring usually handles this automatically when using RedirectAttributes
        // but doing it manually ensures it's clear.
        Map<String, ?> flashMap = RequestContextUtils.getInputFlashMap(request);
        if (flashMap != null) {
            String msg = (String) flashMap.get("msg");
            if (msg != null) {
                model.addAttribute("msg", msg);
            }
            // If you add validation errors via flash attributes, handle them here too
            BindingResult bindingResult = (BindingResult) flashMap.get(BindingResult.MODEL_KEY_PREFIX + "memoForm");
            if (bindingResult != null) {
                model.addAttribute(BindingResult.MODEL_KEY_PREFIX + "memoForm", bindingResult);
            }
        }


        return "index";
    }

    // The separate GET /add endpoint is no longer strictly necessary
    // if adding is only done inline on the index page.
    // You can comment it out or remove it if desired.
    /*
    @GetMapping("/add")
    public String add(Model model) {
        model.addAttribute("memoForm", new MemoForm());
        return "add"; // This template (add.html) might be removed too
    }
    */


    @PostMapping("/add")
    // Added @Valid and BindingResult for potential validation
    public String save(@Validated @ModelAttribute("memoForm") MemoForm form, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        // *** Handle Validation Errors ***
        if (bindingResult.hasErrors()) {
            // Add the form and errors to flash attributes to survive the redirect
            redirectAttributes.addFlashAttribute("memoForm", form);
            redirectAttributes.addFlashAttribute(BindingResult.MODEL_KEY_PREFIX + "memoForm", bindingResult);
            redirectAttributes.addFlashAttribute("msg", "메모 추가 실패: 내용을 확인하세요."); // Optional error message
            return "redirect:/"; // Redirect back to index to display errors
        }

        try {
            Memo memo = Memo.fromText(form.getText());
            memoService.create(memo);
            redirectAttributes.addFlashAttribute("msg", "메모가 성공적으로 추가되었습니다.");
        } catch (Exception e) {
            // Log the exception (e.g., using SLF4J logger)
            // log.error("Error saving memo", e);
            redirectAttributes.addFlashAttribute("msg", "메모 추가 중 오류가 발생했습니다.");
            // Optionally add the form back to flash attributes if you want to retain input on error
            redirectAttributes.addFlashAttribute("memoForm", form);
        }
        return "redirect:/";
    }

//    @PostMapping("/add")
//    public String save(MemoForm form) throws Exception {
////        Memo memo = new Memo(0L, form.getText(), "");
//        Memo memo = Memo.fromText(form.getText());
//        memoService.create(memo);
//        return "redirect:/";
//    }



@PostMapping("/delete/{id}")
public String delete(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
    try {
        memoService.delete(id);
        String msg = "메모 #%d 를 정상적으로 삭제하였습니다.".formatted(id);
        redirectAttributes.addFlashAttribute("msg", msg);
    } catch (Exception e) {
        // Log the exception
        // log.error("Error deleting memo with id {}", id, e);
        redirectAttributes.addFlashAttribute("msg", "메모 삭제 중 오류가 발생했습니다.");
    }
    return "redirect:/";
}

//    @PostMapping("/delete/{id}")
//    public String delete(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) throws Exception {
//        String msg = "%d를 정상적으로 삭제하였습니다.".formatted(id);
//        redirectAttributes.addFlashAttribute("msg", msg);
//        memoService.delete(id);
//        return "redirect:/";
//    }


    @PostMapping("/delete-all")
    public String deleteAll(RedirectAttributes redirectAttributes) {
        try {
            memoService.deleteAll();
            redirectAttributes.addFlashAttribute("msg", "모든 메모를 삭제했습니다.");
        } catch (Exception e) {
            // Log the exception
            // log.error("Error deleting all memos", e);
            redirectAttributes.addFlashAttribute("msg", "전체 메모 삭제 중 오류가 발생했습니다.");
        }
        return "redirect:/";
    }

//    @PostMapping("/delete-all")
//    public String deleteAll(RedirectAttributes redirectAttributes) throws Exception {
//        memoService.deleteAll();
//        // addAttribute -> 주소창 => controller가 한 번 더 받아줘야함
//        redirectAttributes.addFlashAttribute("msg", "전체 삭제");
//        return "redirect:/";
//    }


    @PostMapping("/update/{id}")
    public String update(
            @PathVariable("id") Long id,
            @RequestParam String text, // Input name must be "text" in the form
            RedirectAttributes redirectAttributes) {
        try {
            // Basic validation: check if text is empty
            if (text == null || text.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("msg", "수정 실패: 내용은 비어 있을 수 없습니다. (ID: " + id + ")");
                // Optionally store the failed text in flash attributes if needed
                return "redirect:/";
            }

            Memo oldMemo = memoService.findById(id);
            if (oldMemo == null) {
                redirectAttributes.addFlashAttribute("msg", "수정 실패: 해당 메모를 찾을 수 없습니다. (ID: " + id + ")");
                return "redirect:/";
            }
            // Create new Memo object - Assuming ID and createdAt should be preserved
            Memo newMemo = new Memo(oldMemo.id(), text.trim(), oldMemo.createdAt());
            memoService.update(newMemo);
            redirectAttributes.addFlashAttribute("msg", "메모 #%d 가 정상적으로 수정되었습니다!".formatted(id));
        } catch (Exception e) {
            // Log the exception
            // log.error("Error updating memo with id {}", id, e);
            redirectAttributes.addFlashAttribute("msg", "메모 수정 중 오류가 발생했습니다. (ID: " + id + ")");
        }
        return "redirect:/";
    }


//    @GetMapping("/update/{id}")
//    public String update(@PathVariable("id") Long id, Model model) {
//        Memo memo = memoService.findById(id);
//        model.addAttribute("memo", memo);
//        return "update";
//    }
//
//    @PostMapping("/update/{id}")
//    public String update(
//            @PathVariable("id") Long id,
//            @RequestParam String text,
//            RedirectAttributes redirectAttributes) {
//        Memo oldMemo = memoService.findById(id);
//        Memo newMemo = new Memo(oldMemo.id(), text, oldMemo.createdAt());
//        memoService.update(newMemo);
//        redirectAttributes.addFlashAttribute("msg", "정상적으로 수정되었습니다!");
//        return "redirect:/";
//    }
}