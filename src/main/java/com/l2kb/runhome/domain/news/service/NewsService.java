package com.l2kb.runhome.domain.news.service;

import com.l2kb.runhome.domain.news.dto.NewsResponse;
import com.l2kb.runhome.domain.team.entity.Team;
import com.l2kb.runhome.domain.user.repository.UserRepository;
import com.l2kb.runhome.global.exception.BusinessException;
import com.l2kb.runhome.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewsService {

    private final RestClient restClient;
    private final UserRepository userRepository;

    private static final String BASE_URL   = "https://www.koreabaseball.com";
    private static final String NEWS_BASE  = BASE_URL + "/MediaNews/News/BreakingNews/";
    private static final String LIST_URL   = NEWS_BASE + "List.aspx";

    private static final Pattern LI_PATTERN      = Pattern.compile("<li>(.*?)</li>", Pattern.DOTALL);
    private static final Pattern THUMB_PATTERN   = Pattern.compile("<span class=\"photo\">.*?<img\\s+src=[\"']([^\"']+)[\"']", Pattern.DOTALL);
    private static final Pattern HREF_PATTERN    = Pattern.compile("href=[\"']View\\.aspx\\?bdSe=(\\d+)[\"']");
    private static final Pattern TITLE_PATTERN   = Pattern.compile("<strong>.*?<a[^>]*>([^<]+)</a>", Pattern.DOTALL);
    private static final Pattern DATE_PATTERN    = Pattern.compile("<span class=\"date\">([^<]+)</span>");
    private static final Pattern SUMMARY_PATTERN = Pattern.compile("<p[^>]*>\\s*\"?(.*?)\"?\\s*<span class=\"date\">", Pattern.DOTALL);

    public List<NewsResponse> getNews(int pageNo, int listCn, Long userId) {
        List<NewsResponse> news = scrape(pageNo);

        if (userId != null) {
            Team team = resolveTeam(userId);
            if (team != null) {
                news = news.stream()
                        .filter(n -> containsTeam(n.title(), team))
                        .collect(Collectors.toList());
            }
        }

        return news.stream().limit(listCn).toList();
    }

    private List<NewsResponse> scrape(int pageNo) {
        try {
            String html = restClient.get()
                    .uri(LIST_URL + "?pageNo=" + pageNo)
                    .header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/148.0.0.0 Safari/537.36")
                    .header("Referer", BASE_URL + "/")
                    .retrieve()
                    .body(String.class);

            int boardStart = html.indexOf("class=\"boardPhoto\"");
            if (boardStart < 0) return List.of();

            List<NewsResponse> items = new ArrayList<>();
            Matcher liMatcher = LI_PATTERN.matcher(html.substring(boardStart));
            while (liMatcher.find()) {
                NewsResponse item = parseItem(liMatcher.group(1));
                if (item != null) items.add(item);
            }
            return items;

        } catch (Exception e) {
            log.warn("뉴스 스크래핑 실패: pageNo={}", pageNo);
            throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR);
        }
    }

    private NewsResponse parseItem(String liHtml) {
        Matcher hrefMatcher = HREF_PATTERN.matcher(liHtml);
        if (!hrefMatcher.find()) return null;
        String url = NEWS_BASE + "View.aspx?bdSe=" + hrefMatcher.group(1);

        Matcher thumbMatcher = THUMB_PATTERN.matcher(liHtml);
        String thumbnailUrl = thumbMatcher.find() ? toHttps(thumbMatcher.group(1)) : null;

        Matcher titleMatcher = TITLE_PATTERN.matcher(liHtml);
        String title = titleMatcher.find() ? titleMatcher.group(1).trim() : "";

        Matcher dateMatcher = DATE_PATTERN.matcher(liHtml);
        String date = dateMatcher.find() ? dateMatcher.group(1).trim() : "";

        Matcher summaryMatcher = SUMMARY_PATTERN.matcher(liHtml);
        String summary = "";
        if (summaryMatcher.find()) {
            summary = summaryMatcher.group(1).replaceAll("<[^>]+>", "").trim();
        }

        return new NewsResponse(title, url, "속보", thumbnailUrl, summary, date);
    }

    private Team resolveTeam(Long userId) {
        return userRepository.findWithTeamById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND))
                .getFavoriteTeam();
    }

    private boolean containsTeam(String title, Team team) {
        return title.contains(team.getShortName()) || title.contains(team.getAbbreviation());
    }

    private String toHttps(String url) {
        return url.startsWith("//") ? "https:" + url : url;
    }
}
