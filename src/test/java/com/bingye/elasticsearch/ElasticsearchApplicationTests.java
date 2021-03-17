package com.bingye.elasticsearch;

import com.alibaba.fastjson.JSON;
import com.bingye.elasticsearch.domain.po.User;
import com.bingye.elasticsearch.persistence.UserEsRepository;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.*;
import org.elasticsearch.index.reindex.UpdateByQueryRequestBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@SpringBootTest
class ElasticsearchApplicationTests {

    @Autowired
    public UserEsRepository userEsRepository;

    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    public static final ArrayList<User> users = new ArrayList<>();

    static {
        users.add(new User(1,"叶兵","安徽太湖县",12));
        users.add(new User(2,"张三","安庆 太湖县",13));
        users.add(new User(3,"陆景岗","亳州市利辛县",14));
        users.add(new User(4,"刘家好","淮南市",15));
        users.add(new User(5,"程浩","合肥市肥东去",18));
        users.add(new User(6,"蒋家栋","亳州市谯城区",21));
        users.add(new User(7,"蒋家康","陕西省",33));
        users.add(new User(8,"李伟","亳州市谯城区",34));
        users.add(new User(9,"李东东","亳州市谯城区",45));
        users.add(new User(10,"李蕴兰","沙哈",12));
        users.add(new User(11,"王幸运","亳州",33));
        users.add(new User(12,"李婷","合肥",32));
    }

    //保存或更新一个文档
    @Test
    void addDocument(){
        User user = new User();
        user.setId(3);
        user.setAddress("安徽省蚌埠市");
        user.setAge(3);
        user.setUsername("李四");
        User save = userEsRepository.save(user);
        System.out.println(JSON.toJSONString(save));
    }

    //查询所有文档
    @Test
    void findAll(){
        Iterable<User> all = userEsRepository.findAll();
        all.forEach(user -> {
            System.out.println(JSON.toJSONString(user));
        });
    }

    @Test
    void save(){
        Iterable<User> users = userEsRepository.saveAll(ElasticsearchApplicationTests.users);
    }

    @Test
    void findById(){
        Optional<User> byId = userEsRepository.findById(1);
        //Java 8 Optional 类
        //Optional 类是一个可以为null的容器对象。如果值存在则isPresent()方法会返回true，调用get()方法会返回该对象。
        //Optional 是个容器：它可以保存类型T的值，或者仅仅保存null。Optional提供很多有用的方法，这样我们就不用显式进行空值检测。
        //Optional 类的引入很好的解决空指针异常。
        if(byId.isPresent()){
            User user = byId.get();
            System.out.println(JSON.toJSONString(user));
        }
    }

    /**
     * 与
     * 模糊查询
     */
    @Test
    void findUsersByUsernameLikeAndAddressLike(){
        List<SearchHit<User>> searchHits = userEsRepository.findUsersByUsernameLikeAndAddressLike("张三王八","安徽");
        System.out.println(JSON.toJSONString(searchHits));
    }

    /**
     * 或
     * 模糊查询
     */
    @Test
    void findUsersByUsernameLikeOrAddressLike(){
        List<SearchHit<User>> users = userEsRepository.findUsersByUsernameLikeOrAddressLike("张三王八","亳州市");
        System.out.println(JSON.toJSONString(users));
    }

    /**
     * 分页查询
     */
    @Test
    void findUsersByAddressLikeOrderByAgeDesc(){
        Page<SearchHit<User>> page = userEsRepository.findUsersByAddressLikeOrderByAgeDesc("安徽省大大大", PageRequest.of(0,5));
        System.out.println(JSON.toJSONString(page));
    }

    @Test
    void findAllByPage(){
        Page<User> page = userEsRepository.findAll(PageRequest.of(0,5));
        System.out.println(JSON.toJSONString(page));
    }

    /**
     * 满足系统要求的分页查询（高亮）
     */
    @Test
    void getHighlightPage(){
        //Query query, Class<T> clazz, IndexCoordinates index
        //MatchQueryBuilder matchQueryBuilder = QueryBuilders.matchQuery("address", "安");
        //MatchAllQueryBuilder matchAllQueryBuilder = QueryBuilders.matchAllQuery();

        //1,准备bool多条件查询
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery()
                .should(QueryBuilders.matchQuery("username","叶兵"))
                .should(QueryBuilders.matchQuery("address","安徽"));

        //2,准备查询对象
        //nativeSearchQuery = new NativeSearchQuery(matchAllQueryBuilder);
        //nativeSearchQuery = new NativeSearchQuery(matchQueryBuilder);
        NativeSearchQuery nativeSearchQuery = new NativeSearchQueryBuilder()
                .withQuery(boolQueryBuilder)
                .withHighlightFields(
                        new HighlightBuilder.Field("username").preTags("<span>").postTags("</span>"),
                        new HighlightBuilder.Field("address").preTags("<a>").postTags("</a>")
                ).build();

        //3,查询
        SearchHits<User> search = elasticsearchRestTemplate.search(nativeSearchQuery, User.class);

        //4,解析
        for (SearchHit<User> searchHit : search.getSearchHits()) {
            User user = searchHit.getContent();
            Map<String, List<String>> highlightFields = searchHit.getHighlightFields();
            List<String> usernameList = highlightFields.get("username");
            List<String> addressList = highlightFields.get("address");

            StringBuffer usernameBuffer = new StringBuffer();
            StringBuffer addressBuffer = new StringBuffer();
            if(usernameList !=null){
                for (String username : usernameList) {
                    usernameBuffer.append(username);
                }
            }
            if(addressList !=null){
                for (String address : addressList) {
                    addressBuffer.append(address);
                }
            }
            user.setUsername(usernameBuffer.toString());
            user.setAddress(addressBuffer.toString());
            System.out.println(JSON.toJSONString(user));
        }

    }

    @Test
    void matchAllQuery(){
        MatchAllQueryBuilder matchAllQueryBuilder = QueryBuilders.matchAllQuery();
        NativeSearchQuery nativeSearchQuery = new NativeSearchQuery(matchAllQueryBuilder);
        SearchHits<User> searchHits = elasticsearchRestTemplate.search(nativeSearchQuery, User.class);
        searchHits.get().forEach(userSearchHit -> {
            System.out.println(userSearchHit.getContent());
        });
    }

    //模糊搜索
    @Test
    void fuzzyQuery(){
        QueryBuilder queryBuilder = QueryBuilders.fuzzyQuery("username","张三王八");
        NativeSearchQuery nativeSearchQuery = new NativeSearchQuery(queryBuilder);
        SearchHits<User> searchHits = elasticsearchRestTemplate.search(nativeSearchQuery, User.class);
        searchHits.get().forEach(userSearchHit -> {
            System.out.println(userSearchHit.getContent());
        });
    }

    /**
     * 精确搜索
     * 查询条件不分词
     */
    @Test
    void matchPhraseQuery(){
        QueryBuilder queryBuilder = QueryBuilders.matchPhraseQuery("username","张三王八");
        NativeSearchQuery nativeSearchQuery = new NativeSearchQuery(queryBuilder);
        SearchHits<User> searchHits = elasticsearchRestTemplate.search(nativeSearchQuery, User.class);
        searchHits.get().forEach(userSearchHit -> {
            System.out.println(userSearchHit.getContent());
        });
    }

}
