package com.sleekydz86.support.board.service;

import com.sleekydz86.core.tenant.TenantContext;
import com.sleekydz86.domain.user.entity.UserEntity;
import com.sleekydz86.domain.user.repository.UserRepository;
import com.sleekydz86.support.board.dto.response.BoardStatisticsResponse;
import com.sleekydz86.support.board.entity.BoardEntity;
import com.sleekydz86.support.board.repository.BoardCommentRepository;
import com.sleekydz86.support.board.repository.BoardLikeRepository;
import com.sleekydz86.support.board.repository.BoardRepository;
import com.sleekydz86.support.board.repository.BoardViewRepository;
import com.sleekydz86.support.board.type.BoardType;
import com.sleekydz86.support.board.type.NoticeType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardStatisticsService {

    private final BoardRepository boardRepository;
    private final BoardViewRepository boardViewRepository;
    private final BoardLikeRepository boardLikeRepository;
    private final BoardCommentRepository boardCommentRepository;
    private final UserRepository userRepository;

    public BoardStatisticsResponse getDailyStatistics(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(23, 59, 59);

        List<BoardEntity> boards = boardRepository.findByDeletedFalse(
                org.springframework.data.domain.PageRequest.of(0, Integer.MAX_VALUE)
        ).getContent().stream()
                .filter(board -> !board.getCreatedAt().isBefore(start) && !board.getCreatedAt().isAfter(end))
                .collect(Collectors.toList());

        return buildStatisticsResponse(boards, date, null, null, null);
    }

    public BoardStatisticsResponse getWeeklyStatistics(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);

        List<BoardEntity> boards = boardRepository.findByDeletedFalse(
                org.springframework.data.domain.PageRequest.of(0, Integer.MAX_VALUE)
        ).getContent().stream()
                .filter(board -> !board.getCreatedAt().isBefore(start) && !board.getCreatedAt().isAfter(end))
                .collect(Collectors.toList());

        return buildStatisticsResponse(boards, null, startDate, endDate, null);
    }

    public BoardStatisticsResponse getMonthlyStatistics(int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);

        List<BoardEntity> boards = boardRepository.findByDeletedFalse(
                org.springframework.data.domain.PageRequest.of(0, Integer.MAX_VALUE)
        ).getContent().stream()
                .filter(board -> !board.getCreatedAt().isBefore(start) && !board.getCreatedAt().isAfter(end))
                .collect(Collectors.toList());

        return buildStatisticsResponse(boards, null, startDate, endDate, null);
    }

    public BoardStatisticsResponse getUserStatistics(Long userId, LocalDate startDate, LocalDate endDate) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        LocalDateTime start = startDate != null ? startDate.atStartOfDay() : LocalDateTime.now().minusMonths(1);
        LocalDateTime end = endDate != null ? endDate.atTime(23, 59, 59) : LocalDateTime.now();

        List<BoardEntity> boards = boardRepository.findByAuthor_IdAndDeletedFalse(userId).stream()
                .filter(board -> !board.getCreatedAt().isBefore(start) && !board.getCreatedAt().isAfter(end))
                .collect(Collectors.toList());

        return buildStatisticsResponse(boards, null, 
                startDate != null ? startDate : start.toLocalDate(),
                endDate != null ? endDate : end.toLocalDate(),
                userId);
    }

    public BoardStatisticsResponse getAdminStatistics(LocalDate startDate, LocalDate endDate) {
        if (!TenantContext.isAdmin()) {
            throw new IllegalArgumentException("관리자만 전체 통계를 조회할 수 있습니다.");
        }

        LocalDateTime start = startDate != null ? startDate.atStartOfDay() : LocalDateTime.now().minusMonths(1);
        LocalDateTime end = endDate != null ? endDate.atTime(23, 59, 59) : LocalDateTime.now();

        List<BoardEntity> boards = boardRepository.findByDeletedFalse(
                org.springframework.data.domain.PageRequest.of(0, Integer.MAX_VALUE)
        ).getContent().stream()
                .filter(board -> !board.getCreatedAt().isBefore(start) && !board.getCreatedAt().isAfter(end))
                .collect(Collectors.toList());

        return buildStatisticsResponse(boards, null, 
                startDate != null ? startDate : start.toLocalDate(),
                endDate != null ? endDate : end.toLocalDate(),
                null);
    }

    private BoardStatisticsResponse buildStatisticsResponse(
            List<BoardEntity> boards, LocalDate date, LocalDate startDate, LocalDate endDate, Long userId) {

        long totalBoards = boards.size();
        long totalViews = boards.stream().mapToLong(BoardEntity::getViewCount).sum();
        long totalLikes = boards.stream().mapToLong(BoardEntity::getLikeCount).sum();
        long totalComments = boards.stream().mapToLong(BoardEntity::getCommentCount).sum();

        Map<BoardType, Long> boardTypeStats = boards.stream()
                .filter(board -> board.getBoardType() != null)
                .collect(Collectors.groupingBy(BoardEntity::getBoardType, Collectors.counting()));

        Map<NoticeType, Long> noticeTypeStats = boards.stream()
                .filter(board -> board.getBoardType() == BoardType.NOTICE && board.getNoticeType() != null)
                .collect(Collectors.groupingBy(BoardEntity::getNoticeType, Collectors.counting()));

        long avgViews = totalBoards > 0 ? totalViews / totalBoards : 0;
        long avgLikes = totalBoards > 0 ? totalLikes / totalBoards : 0;
        long avgComments = totalBoards > 0 ? totalComments / totalBoards : 0;

        BoardEntity mostViewed = boards.stream()
                .max((a, b) -> Long.compare(a.getViewCount(), b.getViewCount()))
                .orElse(null);

        BoardEntity mostLiked = boards.stream()
                .max((a, b) -> Long.compare(a.getLikeCount(), b.getLikeCount()))
                .orElse(null);

        String userName = null;
        if (userId != null) {
            UserEntity user = userRepository.findById(userId).orElse(null);
            userName = user != null ? user.getName() : null;
        }

        return BoardStatisticsResponse.builder()
                .date(date)
                .startDate(startDate)
                .endDate(endDate)
                .userId(userId)
                .userName(userName)
                .totalBoards(totalBoards)
                .totalViews(totalViews)
                .totalLikes(totalLikes)
                .totalComments(totalComments)
                .boardTypeStatistics(boardTypeStats)
                .noticeTypeStatistics(noticeTypeStats)
                .averageViewsPerBoard(avgViews)
                .averageLikesPerBoard(avgLikes)
                .averageCommentsPerBoard(avgComments)
                .mostViewedBoardId(mostViewed != null ? mostViewed.getBoardId() : null)
                .mostViewedBoardTitle(mostViewed != null ? mostViewed.getTitle() : null)
                .mostLikedBoardId(mostLiked != null ? mostLiked.getBoardId() : null)
                .mostLikedBoardTitle(mostLiked != null ? mostLiked.getTitle() : null)
                .build();
    }
}

