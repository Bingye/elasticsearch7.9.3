package com.bingye.elasticsearch.persistence;

import com.bingye.elasticsearch.domain.po.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.annotations.Highlight;
import org.springframework.data.elasticsearch.annotations.HighlightField;
import org.springframework.data.elasticsearch.annotations.HighlightParameters;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface UserEsRepository extends PagingAndSortingRepository<User, Integer>, ElasticsearchRepository<User, Integer> {

    @Highlight(fields = {
            @HighlightField(name = "username",
                    parameters = @HighlightParameters(
                            postTags = "</span>",
                            preTags = "<span style='color:red'>"
                    )),
            @HighlightField(name = "address",
                    parameters = @HighlightParameters(
                            postTags = "</a>",
                            preTags = "<a>"
                    ))
    })
    List<SearchHit<User>> findUsersByUsernameLikeAndAddressLike(String username, String address);

    @Highlight(fields = {
            @HighlightField(name = "username",
                    parameters = @HighlightParameters(
                            postTags = "</span>",
                            preTags = "<span style='color:red'>"
                    )),
            @HighlightField(name = "address",
                    parameters = @HighlightParameters(
                            postTags = "</a>",
                            preTags = "<a>"
                    ))
    })
    List<SearchHit<User>> findUsersByUsernameLikeOrAddressLike(String username,String address);

    @Highlight(fields = {
            @HighlightField(name = "address",
                    parameters = @HighlightParameters(
                            postTags = "</a>",
                            preTags = "<a>"
                    ))
    })
    Page<SearchHit<User>> findUsersByAddressLikeOrderByAgeDesc(String address, Pageable pageable);

}
