package com.sleekydz86.support.board.dto.response;

import com.sleekydz86.support.board.type.BoardType;
import com.sleekydz86.support.board.type.NoticeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.Map;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardStatisticsResponse {

    private LocalDate date;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long userId;
    private String userName;

    private Long totalBoards;
    private Long totalViews;
    private Long totalLikes;
    private Long totalComments;

    private Map<BoardType, Long> boardTypeStatistics;
    private Map<NoticeType, Long> noticeTypeStatistics;

    private Long averageViewsPerBoard;
    private Long averageLikesPerBoard;
    private Long averageCommentsPerBoard;

    private Long mostViewedBoardId;
    private String mostViewedBoardTitle;
    private Long mostLikedBoardId;
    private String mostLikedBoardTitle;
}
