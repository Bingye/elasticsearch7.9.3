# elasticsearch7.9.3
elasticsearch7.9.3

#查询所有文档+排序(默认10条)
GET /user/_search
{
  "query": {
    "match_all": {}
  },
  "sort": [
    {
      "age": {
        "order": "desc"
      }
    }
  ]
}

#查询所有文档+分数排序(默认10条)
GET /user/_search
{
  "query": {
    "match_all": {}
  },
  "sort": [
    {
      "_score": {
        "order": "desc"
      }
    }
  ]
}

#分页查询文档
GET user/_search
{
  "query": {
    "match_all": {}
  },
  "sort": [
    {
      "age": {
        "order": "desc"
      }
    }
  ], 
  "from": 0,
  "size": 5
}

#match
#条件查询
GET user/_search
{
  "query": {
    "match": {
      "address": "安徽"
    }
  }
}

#组合条件查询 [太_安]
GET user/_search
{
  "query": {
    "match": {
      "address": "太 安"
    }
  }
}

#match_phrase（精确匹配所有分词同时含有的语句，有先后顺序）
#短句条件查询
GET user/_search
{
  "query": {
    "match_phrase": {
      "address": "安徽太湖"
    }
  }
}
GET user/_search
{
  "query": {
    "match_phrase": {
      "address": {
        "query": "亳",
        "slop": 1   //调节因子
      }
    }
  }
}

#bool复杂查询
#与查询
GET user/_search
{
  "query": {
    "bool": {
      "must": [
        {
          "match": {
            "username": "叶兵"
          }
        },
        {
          "match": {
            "address": "安徽"
          }
        }
      ]
    }
  }
}

#非查询
GET user/_search
{
  "query": {
    "bool": {
      "must_not": [
        {
          "match": {
            "username": "叶兵"
          }
        }
      ]
    }
  }
}

#或查询
GET user/_search
{
  "query": {
    "bool": {
      "should": [
        {
          "match": {
            "username": "叶兵"
          }
        },
        {
          "match": {
            "address": "安徽"
          }
        }
      ]
    }
  }
}

#filter 显式指定过滤器查询
GET user/_search
{
  "query": {
    "bool": {
      "must": [
        {
          "match_all": {}
        }
      ],
      "filter": [
        {
          "range": {
            "age": {
              "gte": 10,
              "lte": 15
            }
          }
        }
      ]
    }
  }
}

#multi_match多字段匹配
GET user/_search
{
  "query": {
    "multi_match": {
      "query": "亳",
      "fields": ["username","address"]
    }
  }
}

#term完全匹配（不对查询内容分词）
如果希望完全匹配某字段，这需要将文档字段设置成keyword类型
GET user/_search
{
  "query": {
    "term": {
      "username": "叶兵"
    }
  }
}



#重新刷新建立索引
POST user/_update_by_query
{
  "query": {
    "bool": {
      "should": [
        {
          "bool": {
            "must": [
              {
                "term": {
                  "username": {
                    "value": "叶"
                  }
                }
              },
              {
                "term": {
                  "username": {
                    "value": "兵"
                  }
                }
              }
            ]
          }
        },
        {
          "bool": {
            "must": [
              {
                "term": {
                  "address": {
                    "value": "叶"
                  }
                }
              },
              {
                "term": {
                  "address": {
                    "value": "兵"
                  }
                }
              }
            ]
          }
        }
      ]
    }
  }
}


#自定义analyzer
#1,char_filter 将不需要的字符排除掉
#2,tokenizer
#3,filter
PUT test
{
  "settings": {
    "analysis": {
      "analyzer":{
        "my_pinyin_analyzer":{
          "char_filter":["html_strip"],
          "tokenizer":"keyword",
          "filter":"pinyin"
        }
      }
    }
  }
}

#验证
GET test/_analyze
{
  "analyzer": "my_pinyin_analyzer",
  "text": ["<span>叶兵</span>"]
}

DELETE test

#自定义自己的filter
#keep_first_letter:true  叶兵>yb
#keep_joined_full_pinyin:true 叶兵>yebing
#keep_full_pinyin:true 叶兵>[ye,bing]
#keep_original:true 保留原始输入
PUT test
{
  "settings": {
    "analysis": {
      "analyzer":{
        "my_pinyin_analyzer":{
          "char_filter":["html_strip"],
          "tokenizer":"keyword",
          "filter":"my_pinyin_filter"
        }
      },
      "filter": {
        "my_pinyin_filter":{
          "type":"pinyin",
          "keep_first_letter":true,
          "keep_joined_full_pinyin":true,
          "keep_full_pinyin":false,
          "keep_original":true
        }
      }
    }
  }
}

#验证
GET test/_analyze
{
  "analyzer": "my_pinyin_analyzer",
  "text": ["<span>叶兵</span>"]
}

#明星姓名搜索样例：
DELETE stars
#自定义自己的filter
#keep_first_letter:true  叶兵>yb
#keep_joined_full_pinyin:true 叶兵>yebing
#keep_full_pinyin:true 叶兵>[ye,bing]
#keep_original:true 保留原始输入
PUT stars
{
  "settings": {
    "analysis": {
      "analyzer": {
        "star_name_analyzer": {
          "char_filter": [
            "html_strip"
          ],
          "tokenizer": "keyword",
          "filter": "star_name_filter"
        }
      },
      "filter": {
        "star_name_filter": {
          "type": "pinyin",
          "keep_first_letter": true,
          "keep_joined_full_pinyin": true,
          "keep_full_pinyin": false,
          "keep_original": true
        }
      }
    }
  },
  "mappings": {
    "properties": {
      "username": {
        "type": "completion",
        "analyzer": "star_name_analyzer",
        "search_analyzer": "keyword"
      }
    }
  }
}

PUT stars/_bulk
{"index":{}}
{"username":"叶兵"}
{"index":{}}
{"username":"蒋家栋"}
{"index":{}}
{"username":"李伟"}
{"index":{}}
{"username":"李东东"}
{"index":{}}
{"username":"李亚刚"}
{"index":{}}
{"username":"陆景岗"}
{"index":{}}
{"username":"刘家好"}
{"index":{}}
{"username":"陈浩"}
{"index":{}}
{"username":"刘玉杰"}
{"index":{}}
{"username":"李婷"}

GET stars/_search
{
  "_source": false, 
  "suggest": {
    "star_name_suggest": {
      "prefix": "lt",
      "completion": {
        "field": "username",
        "size":15,
        "skip_duplicates":true
      }
    }
  }
}




#logstash 抽取数据样例
GET news/_search
{
  "_source": false, 
  "suggest": {
    "baidu_news_suggest": {
      "prefix": "xian",
      "completion": {
        "field": "content",
        "size":5,
        "skip_duplicates":true
      }
    }
  }
}

PUT news
{
  "settings": {
    "analysis": {
      "analyzer": {
        "baidu_news_analyzer": {
          "char_filter": [
            "html_strip"
          ],
          "tokenizer": "keyword",
          "filter": "baidu_news_filter"
        }
      },
      "filter": {
        "baidu_news_filter": {
          "type": "pinyin",
          "keep_first_letter": true,
          "keep_joined_full_pinyin": true,
          "keep_full_pinyin": false,
          "keep_original": true
        }
      }
    }
  },
  "mappings": {
    "properties": {
      "content": {
        "type": "completion",
        "analyzer": "baidu_news_analyzer",
        "search_analyzer": "keyword"
      },
      "href":{
        "type": "keyword"
      },
      "id":{
        "type": "long"
      } 
    }
  }
}

PUT news2
{
  "settings": {
    "analysis": {
      "analyzer": {
        "baidu_news_analyzer": {
          "char_filter": [
            "html_strip"
          ],
          "tokenizer": "keyword",
          "filter": "baidu_news_filter"
        }
      },
      "filter": {
        "baidu_news_filter": {
          "type": "pinyin",
          "keep_first_letter": true,
          "keep_joined_full_pinyin": true,
          "keep_full_pinyin": false,
          "keep_original": true
        }
      }
    }
  },
  "mappings": {
    "properties": {
      "content": {
        "type": "text",
        "analyzer": "ik_max_word",
        "search_analyzer": "ik_smart"
      },
      "href":{
        "type": "keyword"
      },
      "id":{
        "type": "long"
      } 
    }
  }
}

DELETE news2

GET news2/_search
{
  "_source": ["content"], 
  "query": {
    "bool": {
      "must": [
        {
          "match": {
            "content": "腾讯"
          }
        }
      ]
    }
  }
}
