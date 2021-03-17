package com.bingye.elasticsearch.domain.po;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * @Document：在类级别应用，以指示该类是映射到数据库的候选对象。最重要的属性是：
 *
 * indexName：用于存储此实体的索引的名称。它可以包含SpEL模板表达式，例如 "log-#{T(java.time.LocalDate).now().toString()}"
 *
 * type：映射类型。如果未设置，则使用小写的类的简单名称。（自4.0版弃用）
 *
 * shards：索引的分片数。
 *
 * replicas：索引的副本数。
 *
 * refreshIntervall：索引的刷新间隔。用于索引创建。默认值为“ 1s”。
 *
 * indexStoreType：索引的索引存储类型。用于索引创建。默认值为“ fs”。
 *
 * createIndex：标记是否在存储库引导中创建索引。默认值是true。请参阅使用相应的映射自动创建索引
 *
 * versionType：版本管理的配置。默认值为EXTERNAL。
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName = "user",shards = 5,replicas = 2)
public class User {

    @Id
    private Integer id;

    @Field(type= FieldType.Text)
    private String username;

    @Field(type= FieldType.Text)
    private String address;

    @Field(type= FieldType.Auto)
    private int age;

}
