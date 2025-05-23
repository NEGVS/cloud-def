package com.jan.base.util.bankOfCommunications.test;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @Description 定义 Java 对象（带 JAXB 注解）
 * 创建一个简单的 Person 类，用 JAXB 注解标记：
 * @Author Andy Fan
 * @Date 2025/5/16 11:47
 * @ClassName Person
 */


@XmlRootElement(name = "person")       // 指定XML根元素名称
@XmlAccessorType(XmlAccessType.FIELD)  // 指定基于字段（而非getter/setter）绑定，控制JAXB如何访问字段（FIELD直接访问字段，PROPERTY通过getter/setter）

public class Person {
    @XmlAttribute// 标记为XML属性（而非元素）
    private int id;

    @XmlElement(name = "full-name")    // 自定义元素名称
    private String name;

    @XmlElement
    private int age;

    // 必须有无参构造函数
    public Person() {
    }

    public Person(int id, String name, int age) {
        this.id = id;
        this.name = name;
        this.age = age;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}
