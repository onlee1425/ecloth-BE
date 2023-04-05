package com.ecloth.beta.domain.post.posting.controller;

import com.ecloth.beta.domain.post.posting.dto.PostingLikeRequest;
import com.ecloth.beta.domain.post.posting.dto.*;
import com.ecloth.beta.domain.post.posting.repository.PostingRepository;
import com.ecloth.beta.domain.post.posting.service.PostingService;
import com.ecloth.beta.security.memberDetail.MemberDetails;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import springfox.documentation.annotations.ApiIgnore;

import javax.persistence.EntityNotFoundException;
import java.io.IOException;

/**
 * 포스트 API
 * - 포스트 CRUD *조회수 +1
 * - 포스트 좋아요
 */
@RestController
@RequestMapping("/api")
@Api(tags = "포스트 API")
@RequiredArgsConstructor
public class PostingController {

    private final PostingService postingService;
    private final PostingRepository postingRepository;

    // 포스트 등록
    @PostMapping(value = "/feed/post",consumes = {"multipart/form-data"})
    public ResponseEntity<Void> postCreate(@RequestParam("images") MultipartFile[] images,
                                           @ApiIgnore @AuthenticationPrincipal MemberDetails memberDetails,
                                           @RequestParam("title") String title,
                                           @RequestParam("content") String content) throws Exception {

        PostingCreateRequest request = new PostingCreateRequest();
        request.setImages(images);
        request.setMemberId(memberDetails.getMemberId());
        request.setTitle(title);
        request.setContent(content);

        postingService.createPost(request);

        return ResponseEntity.ok().build();
    }


    // 게시글 목록 조회 - 필터 : 조회수순, 좋아요순, 최신순
    @GetMapping("/feed/post")
    public ResponseEntity<PostingListResponse> getLatestPostingList(PostingListRequest request) {

        PostingListResponse response = postingService.getPostListByPage(request);

        return ResponseEntity.ok(response);
    }

    // 회원이 작성한 포스트 목록 조회
    @GetMapping("/feed/post/member/{memberId}")
    public ResponseEntity<MemberPostingListResponse> memberPostList(@PathVariable Long memberId,
                                                                    MemberPostingListRequest request) {

        MemberPostingListResponse response = postingService.getMemberPostList(memberId,request);

        return ResponseEntity.ok(response);
    }

    // 포스트 상세 조회
    @GetMapping("/feed/post/{postingId}")
    public ResponseEntity<PostingDetailResponse> postDetail(@PathVariable Long postingId) {

        PostingDetailResponse response = postingService.getPostDetail(postingId);

        return ResponseEntity.ok(response);
    }

    // 포스트 수정
    @PutMapping("/feed/post/{postingId}")
    public ResponseEntity<Void> postUpdate(@RequestBody PostingUpdateRequest request,

                                           @PathVariable Long postingId) throws Exception {

        postingService.updatePost(postingId, request);

        return ResponseEntity.ok().build();
    }

    // 포스트 좋아요 on/off
    @PutMapping("/feed/post/{postingId}/like")
    public ResponseEntity<Void> postLike(@PathVariable Long postingId,
                                         @RequestBody PostingLikeRequest request) {

        postingService.checkOrUnCheckLike(postingId, request.getMemberId());

        return ResponseEntity.ok().build();
    }


    @DeleteMapping("/feed/post/{postingId}")
    public ResponseEntity<Void> postDelete(@ApiIgnore @AuthenticationPrincipal MemberDetails memberDetails, @PathVariable Long postingId) {
        try {
            postingService.deletePost(postingId, memberDetails.getMemberId());
            return ResponseEntity.ok().build();
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

}

