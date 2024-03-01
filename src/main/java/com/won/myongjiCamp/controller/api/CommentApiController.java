package com.won.myongjiCamp.controller.api;

import com.won.myongjiCamp.config.auth.PrincipalDetail;
import com.won.myongjiCamp.dto.CommentDto;
import com.won.myongjiCamp.dto.CommentResponseDto;
import com.won.myongjiCamp.dto.RecruitDto;
import com.won.myongjiCamp.dto.ResponseDto;
import com.won.myongjiCamp.model.Comment;
import com.won.myongjiCamp.model.Member;
import com.won.myongjiCamp.model.board.Board;
import com.won.myongjiCamp.model.board.RecruitBoard;
import com.won.myongjiCamp.repository.CommentRepository;
import com.won.myongjiCamp.repository.MemberRepository;
import com.won.myongjiCamp.repository.RecruitRepository;
import com.won.myongjiCamp.service.CommentService;
import com.won.myongjiCamp.service.RecruitService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class CommentApiController {

    private final CommentService commentService;
    private final MemberRepository memberRepository;

    private final RecruitRepository recruitRepository;

    private final CommentRepository commentRepository;

    //댓글 작성
/*    @PostMapping("/api/auth/recruit/{id}/comment")
    public ResponseDto<String> createComment(@RequestBody @Valid CommentDto commentDto, @AuthenticationPrincipal PrincipalDetail principal, @PathVariable Long id){
        commentService.create(commentDto,principal.getMember(),id);
        return new ResponseDto<String>(HttpStatus.OK.value(), "댓글 작성 완료");
    }*/

    //댓글 작성 테스트용
    @PostMapping("/api/auth/recruit/{id}/comment")
    public ResponseDto<String> createComment(@RequestBody @Valid CommentDto commentDto, @PathVariable Long id) {
        Member member = memberRepository.findById(1L)
                .orElseThrow(() -> new IllegalArgumentException("해당 멤버가 존재하지 않습니다."));
        commentService.create(commentDto, member, id);
        return new ResponseDto<String>(HttpStatus.OK.value(), "댓글 작성 완료");
    }

    //댓글 삭제
    @DeleteMapping("/api/auth/recruit/{board_id}/comment/{comment_id}")
    public ResponseDto<String> deleteComment(@PathVariable("comment_id") Long comment_id){
        commentService.delete(comment_id);
        return new ResponseDto<String>(HttpStatus.OK.value(), "댓글이 삭제되었습니다.");
    }


    //대댓글 목록 ai

/*        private CommentRepository commentRepository;

        @GetMapping("/api/auth/recruit/{board_id}/comment/{comment_id}")
        public List<Comment> getChildren(@PathVariable("comment_id") Comment parentId) {
            return commentRepository.findByParentId(parentId);
        }*/

    //댓글 전체 조회
    @GetMapping("/api/auth/recruit/{board_id}/comment")
    private Result CommentList(@PathVariable("board_id") Long id){
        Board board = recruitRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다."));


        List<CommentResponseDto> result = new ArrayList<>();
        Map<Long, CommentResponseDto> map = new HashMap<>();
        List<Comment> commentList = commentService.commentAll(id);

        commentList.stream().forEach(c->{
            CommentResponseDto rDto = convertCommentToDto(c);
            map.put(c.getId(), rDto);
            if(c.getCdepth() == 1){// 댓글이 부모가 있으면
                map.get(c.getParent().getId()).getChildren().add(rDto);
            }
            else{
                result.add(rDto);
            }

        });

        return new Result(result);
    }

    @Data
    @AllArgsConstructor
    static class Result<T> {
        private T data;
    }


    public CommentResponseDto convertCommentToDto(Comment comment){
        return new CommentResponseDto(
                comment.getContent(),
                comment.getCreateDate(),
                comment.getWriter().getId(),
                new ArrayList<>()
        );
    }
/*
    public CommentResponseDto childrenToDto(Comment child){ // comment entity의 children을 comment -> CommentResponseDto
        CommentResponseDto c = CommentResponseDto(child.getId());
        c.setContent(child.getContent());
        c.setCommentCreateDate(child.getCreateDate());
        c.setWriterId(child.getWriter().getId());

    }
*/


}


