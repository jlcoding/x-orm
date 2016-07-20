##前言
我是一个曾纠结springboot还是jfinal的渣渣程序员

jfinal的orm超级爽, 但是controller并不是十分方便而且官方并不支持注解.

jfinal-ext支持注解了但是写起单元测试来十分麻烦. jfinal也不支持restful

oscgit上也有数个基于jfinal的restful框架,但是没有在官方支持下感觉十分奇怪,

比如取PathParam要getAttr(),这样就感觉很奇怪了.

最终我败在了Spring的大生态下, 虽然Spring库有点大了, 但是总是有用的.

SpringBoot的简单配置实在让我非常心动, 加上SpringMVC强大又稳定, 所以我最终选择了SpringBoot

但是SpringBoot自带的JPA写起一些多表查询,动态查询实在会死人, 所以我决定写一个基于JDBC类似JFinal的ORM框架

##x-orm简介

跟JFinal一样有Model和Db+Record两种方式, 不过我在Model上加上了注解,这样配置就更加少了.

在注册Record的时候实在比不上波总的JFinal..小弟才疏学浅.感觉在服务启动的性能上比JFinal差多了

功能还在慢慢完善, 不废话了, 有兴趣的小伙伴来试试顺便给个星

##配置
x-orm是基于SpringBoot+Jdbc的 Maven就依赖这几个东西就好了
```
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>1.3.6.RELEASE</version>
  </parent>

  <dependencies>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <dependency>
      <groupId>mysql</groupId>
      <artifactId>mysql-connector-java</artifactId>
    </dependency>

    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-jdbc</artifactId>
    </dependency>
```



配置上面就非常简单了, 在SpringBoot的服务启动类加上两个注册的语句~如果不需要Record的就不需要填了

```
@Controller
@EnableAutoConfiguration
@ComponentScan(basePackages = "com.xdivo")
public class SpringBootStarter {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(SpringBootStarter.class, args);
        Register.registerModel("com.xdivo.model"); //扫描的包名
        Register.registerRecord("online_class"); //数据库名

    }
}

```

定义Model
```
/**
 * 用户类
 * Created by liujunjie on 16-7-19.
 */
@Entity(table = "c_user")
public class User extends Model<User> {

    @PK
    @Column(name = "id_")
    private String id;

    @Column(name = "mobile_")
    private String mobile;

    @Column(name = "password_")
    private String password;

    public String getId() {
        return id;
    }

    public User setId(String id) {
        this.id = id;
        return this;
    }

    public String getMobile() {
        return mobile;
    }

    public User setMobile(String mobile) {
        this.mobile = mobile;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public User setPassword(String password) {
        this.password = password;
        return this;
    }
}
```

在使用的时候就跟JFinal基本一样了
```
//保存User对象
User user = new User();
user.setMobile("abc");
user.setPassword("123");
user.save();

//异步保存到数据(更新也一样)
user.asyncSave();

//查询record
Record record = Db.findById("c_user", 23);

//保存Record对象
Record record = new Record()
    .set("mobile_", "abc")
    .set("password_", "123");
Db.save("c_user", record);
```

像事务那些东西就是基于SpringBoot了.省了一笔功夫



##联系方式
###QQ: 41369927
###邮箱: 41369927@qq.com
