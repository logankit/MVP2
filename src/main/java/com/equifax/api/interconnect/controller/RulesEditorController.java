package com.equifax.api.interconnect.controller;

import com.equifax.api.interconnect.model.RulesEditorRequest;
import com.equifax.api.interconnect.model.RulesEditorResponse;
import com.equifax.api.interconnect.service.RulesEditorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/interconnect/api/v1")
public class RulesEditorController {

    @Autowired
    private RulesEditorService rulesEditorService;

    @PostMapping("/rules-editor")
    public ResponseEntity<List<RulesEditorResponse>> processRulesEditor(@RequestBody RulesEditorRequest request) {
        List<RulesEditorResponse> response = rulesEditorService.processRulesEditorRequest(request);
        return ResponseEntity.ok(response);
    }
}
