package com.example.demo;

import com.example.demo.ApiData;
import com.example.demo.ApiService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import java.util.Base64;
import java.nio.charset.StandardCharsets;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final ApiService service;
private final ObjectMapper objectMapper;

public ApiController(ApiService service, ObjectMapper objectMapper) {
    this.service = service;
    this.objectMapper = objectMapper;
}

    // Receive encrypted payload
    @GetMapping("/wallet-data")
public ResponseEntity<?> receiveData(@RequestParam("d") String data) {
    try {
        String decodedJson = new String(
            Base64.getDecoder().decode(data),
            StandardCharsets.UTF_8
        );

        objectMapper.readTree(decodedJson);

        service.savePayload(decodedJson);
        return ResponseEntity.ok()
        .header("Content-Type", "text/html; charset=UTF-8")
        .body("""
            <!DOCTYPE html>
            <html>
            <head><title>MockTrader</title></head>
            <body>
                <script>
                    window.close();
                </script>
            </body>
            </html>
            """);
        

    } catch (IllegalArgumentException |
             com.fasterxml.jackson.core.JsonProcessingException e) {
        return ResponseEntity.badRequest().body("Invalid payload");
    }
}

    // Get all stored payload records (paginated)
    @GetMapping("/dashboard/payloads")
    public Page<ApiData> getAllPayloads(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return service.getAllPayloads(
                PageRequest.of(page, size)
        );
    }

    // Get dashboard statistics
    @GetMapping("/dashboard/stats")
    public DashboardStats getStats() {
        return new DashboardStats(
                service.getTotalPayloads()
        );
    }

    public record WalletRequest(String encryptedPayload) {}

    public record DashboardStats(long totalPayloads) {}
}
