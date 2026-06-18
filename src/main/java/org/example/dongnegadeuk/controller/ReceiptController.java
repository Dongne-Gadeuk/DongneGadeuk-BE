package org.example.dongnegadeuk.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dongnegadeuk.common.exception.CustomException;
import org.example.dongnegadeuk.common.response.BaseResponse;
import org.example.dongnegadeuk.dto.ReceiptDto;
import org.example.dongnegadeuk.entity.Users;
import org.example.dongnegadeuk.service.ReceiptService;
import org.example.dongnegadeuk.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.example.dongnegadeuk.common.exception.errorCode.JwtErrorCode.JWT_MISSING;

@Tag(name = "Receipt", description = "영수증 관련 API")
@Slf4j
@RestController
@RequestMapping("/api/receipt")
@RequiredArgsConstructor
public class ReceiptController {

    private final ReceiptService receiptService;
    private final UserService userService;

    @Operation(summary = "영수증 촬영", description = "영수증을 촬영해서 정보를 반환합니다.")
    @PostMapping("")
    public BaseResponse<ReceiptDto> create(@AuthenticationPrincipal Users user, @RequestParam("image") MultipartFile image) throws IOException {

        if (user == null) {
            throw new CustomException(JWT_MISSING);
        }

        Long userId =  user.getUserId();

        if(userId!=null){
            ReceiptDto data = receiptService.process(image.getBytes());
            return BaseResponse.success("영수증 추출 완료", data);
        }
        else{
            throw new CustomException(JWT_MISSING);
        }
    }
}
