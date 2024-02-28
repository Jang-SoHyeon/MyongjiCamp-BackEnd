package com.won.myongjiCamp.service;

import com.won.myongjiCamp.dto.request.ApplicationDto;
import com.won.myongjiCamp.exception.AlreadyProcessException;
import com.won.myongjiCamp.model.Member;
import com.won.myongjiCamp.model.application.Application;
import com.won.myongjiCamp.model.application.ApplicationFinalStatus;
import com.won.myongjiCamp.model.application.ApplicationStatus;
import com.won.myongjiCamp.model.board.Board;
import com.won.myongjiCamp.model.board.RecruitBoard;
import com.won.myongjiCamp.model.board.RecruitStatus;
import com.won.myongjiCamp.model.board.role.Role;
import com.won.myongjiCamp.model.board.role.RoleAssignment;
import com.won.myongjiCamp.repository.ApplicationRepository;
import com.won.myongjiCamp.repository.BoardRepository;
import com.won.myongjiCamp.repository.RoleAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.won.myongjiCamp.model.application.ApplicationFinalStatus.PENDING;
import static com.won.myongjiCamp.model.application.ApplicationStatus.*;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final BoardRepository boardRepository;
    private final ApplicationRepository applicationRepository;
    private final RoleAssignmentRepository roleAssignmentRepository;
    private final RecruitService recruitService;

    @Transactional
    public void apply(ApplicationDto request, Long id, Member member) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 글이 존재하지 않습니다."));
        Application findApplication = applicationRepository.findByApplicantAndBoard(member, board).orElse(null);
        if(findApplication != null) {
            throw new IllegalStateException("이미 지원한 글입니다.");
        }
        Application application = Application.builder()
                .applicant(member)
                .board(board)
                .content(request.getContent())
                .role(Role.valueOf(request.getRole()))
                .url(request.getUrl())
                .firstStatus(ApplicationStatus.PENDING)
                .build();
        applicationRepository.save(application);
    }

    @Transactional
    public void cancel(Long id, Member member) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 글이 존재하지 않습니다."));
        Application application = applicationRepository.findByApplicantAndBoard(member, board).
                orElseThrow(() -> new IllegalArgumentException("해당 지원이 존재하지 않습니다."));
        if (application.getFirstStatus() != ApplicationStatus.PENDING){
            throw new AlreadyProcessException("이미 처리된 지원입니다. 지원 결과를 확인하세요.");
        } else {
            applicationRepository.delete(application);
        }
    }

    //first 지원 수락 or 거절
    @Transactional
    public void firstResult(ApplicationDto request, Long id) {
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당하는 지원이 존재하지 않습니다."));
        RoleAssignment roleAssignment = roleAssignmentRepository.findByBoardAndRole(application.getBoard(), Role.valueOf(request.getRole()))
                .orElseThrow(() -> new IllegalArgumentException("해당하는 모집분야가 존재하지 않습니다."));
        extracted(roleAssignment);
        application.setFirstStatus(valueOf(request.getFirstStatus()));
        if (application.getFirstStatus() == ACCEPTED) {
            application.setFinalStatus(PENDING);
        }
    }

    //final 지원 수락 or 거절
    @Transactional
    public void finalResult(ApplicationDto request, Long id) {
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당하는 지원이 존재하지 않습니다."));
        application.setFinalStatus(ApplicationFinalStatus.valueOf(request.getFinalStatus()));
        RoleAssignment roleAssignment = roleAssignmentRepository.findByBoardAndRole(application.getBoard(), Role.valueOf(request.getRole()))
                .orElseThrow(() -> new IllegalArgumentException("해당하는 모집분야가 존재하지 않습니다."));
        extracted(roleAssignment);
        // 모든 모집 인원이 차면? -> 모집완료 상태로 변환
        Long boardId = application.getBoard().getId();
        RecruitBoard board = (RecruitBoard) boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("해당하는 글이 존재하지 않습니다."));
        List<RoleAssignment> role = roleAssignmentRepository.findByBoard(board);
        AtomicBoolean state = new AtomicBoolean(true);
        role.stream().forEach(r -> {
            if (r.getAppliedNumber() != r.getRequiredNumber()) {
                state.set(false);
            }
        });
        if (!state.get()) {
            board.setStatus(RecruitStatus.RECRUIT_COMPLETE);
        }
    }

    private void extracted(RoleAssignment roleAssignment) {
        // if 이미 해당 지원 분야가 꽉차면 있으면 예외 터지게
        int appliedNumber = roleAssignment.getAppliedNumber();
        int requiredNumber = roleAssignment.getRequiredNumber();
        if (appliedNumber >= requiredNumber) {
            throw new IllegalStateException("해당 분야의 모집이 마감되었습니다.");
        }
    }
}
