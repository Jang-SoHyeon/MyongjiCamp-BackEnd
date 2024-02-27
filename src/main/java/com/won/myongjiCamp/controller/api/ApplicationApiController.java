package com.won.myongjiCamp.controller.api;

import com.won.myongjiCamp.config.auth.PrincipalDetail;
import com.won.myongjiCamp.service.ApplicationService;
import com.won.myongjiCamp.dto.request.ApplicationDto;
import com.won.myongjiCamp.dto.ResponseDto;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class ApplicationApiController {

    private final ApplicationService applicationService;

    //지원 (id = board id)
    @PostMapping("/api/auth/apply/{id}")
    public ResponseDto apply(@RequestBody @Valid ApplicationDto request, @PathVariable Long id, @AuthenticationPrincipal PrincipalDetail principal) {
        applicationService.apply(request, id, principal.getMember());
        return new ResponseDto(HttpStatus.OK.value(), "지원이 완료되었습니다.");
    }
    //지원 취소 (id = board id)
    @DeleteMapping("/api/auth/apply/{id}")
    public ResponseDto cancel(@RequestBody @Valid ApplicationDto request, @PathVariable Long id, @AuthenticationPrincipal PrincipalDetail principal) {
        applicationService.cancel(request, id, principal.getMember());
        return new ResponseDto(HttpStatus.OK.value(), "지원이 취소되었습니다.");
    }
    //first 지원 수락 or 거절 (id = application id) -> applicationstatus 수정
    @PutMapping("/api/auth/apply/first/{id}")
    public ResponseDto firstResult(@RequestBody @Valid ApplicationDto request, @PathVariable Long id) {
        applicationService.firstResult(request, id);
        return new ResponseDto(HttpStatus.OK.value(), "해당 지원 처리가 완료되었습니다.");
    }
    //first 지원 수락 or 거절 (id = application id) -> applicationstatus 수정
    @PutMapping("/api/auth/apply/final/{id}")
    public ResponseDto finalResult(@RequestBody @Valid ApplicationDto request, @PathVariable Long id) {
        applicationService.finalResult(request, id);
        return new ResponseDto(HttpStatus.OK.value(), "해당 지원 처리가 완료되었습니다.");
    }
    
//    //모집자 입장에서의 지원 확인 (id = 모집자 id)
//    @GetMapping("/api/auth/apply/first")
//    public
//    //지원자 입장에서의 지원 확인 (id = 지원자 id)
//    @GetMapping("/api/auth/apply/final")
//
//    @Data
//    @AllArgsConstructor
//    static class Result<T> {
//        private T data;
//    }

}