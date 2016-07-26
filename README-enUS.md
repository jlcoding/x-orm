<hr/>
##Note 2016-07-24 04:08 &nbsp; update 0.0.2-SNAPSHOT

1. add Model reference
2. optimize register structure
3. add toModel method to Record, after convertion Model still have its reference


<hr/>
##Note &nbsp; 0.0.1-SNAPSHOT
contain serious bug
<hr/>

##Notice

this tools or framework, whatever, i just made it for myself and practice how to start a open source project
i hope predecessors can give me some advice, if u dont like it, tell me why, thanks 

##Foreword

i used to hesitate use springboot or JFinal

JFinal has a super convenient ORM, but the author think Annotation will increase learning cost, but i'm not a starter, 

i think annotation will decrease a lot setting.

finally i choose SpringBoot. but JPA is not born for dynamic query. So i want to code an orm just like JFinal.

##x-orm Introduce

x-orm is build on JDBCTemplate, so u don't worry about transaction or datasource, it just like jdbc.

Model is an entity+dao Object, just like domain in DDD but not all the same.

Record is just like a non-mapping object, it can execute sql query without a entity. In some business is very convenient.

##Setting
x-orm is base on SpringBoot + Jdbc, so u just need to add these to Maven dependencies
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



besides jdbc and spring setting, startup setting is very easy, you just need add 
```
Register.registerModel("com.xdivo.model"); //scan package name
```
and 
```
Register.registerRecord("online_class"); //database name
```
just like
```
@Controller
@EnableAutoConfiguration
@ComponentScan(basePackages = "com.xdivo")
public class SpringBootStarter {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(SpringBootStarter.class, args);
        Register.registerModel("com.xdivo.model"); //scan package name
        Register.registerRecord("online_class"); //database name
        Register.initThreadPool(100, 100, 1000); //init theadpool 0为使用默认值
    }
}

```

if you don't need to use Record or ThreadPool, you can delete those line;

define Model
```
/**
 * User
 * Created by liujunjie on 16-7-19.
 */
@Entity(table = "c_user")
public class User extends Model<User> {

    @PK
    @Column(name = "id_")
    private long id;

    @Column(name = "mobile_")
    private String mobile;

    @Column(name = "password_")
    private String password;

    @Join(refColumn = "id")
    @Column(name = "room_id_")
    private Room room;

    @Join(refColumn = "id")
    @Column(name = "student_id_")
    private Student student;

    public long getId() {
        return id;
    }

    public User setId(long id) {
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

    public Room getRoom() {
        return room;
    }

    public User setRoom(Room room) {
        this.room = room;
        return this;
    }

    public Student getStudent() {
        return student;
    }

    public User setStudent(Student student) {
        this.student = student;
        return this;
    }
}
```

When you use it, it just like JFinal
```
//save User
new User().setMobile("abc")
      .setPassword("123")
      .save();

//find User by primary key
User user = new User().findById(id);

//get reference object
user.getStudent();

//async save to database
user.asyncSave();

//async update to database
user.asyncUpdate();
```

```
//use record
Record record = Db.findById("c_user", 23);

//convert to Model u can still use model to get reference object
User user = record.toModel(User.class);

//save Record
Record record = new Record()
    .set("mobile_", "abc")
    .set("password_", "123");
Db.save("c_user", record);
```

```
//directly use jdbcTemplate and convert to Model
Map<String, Object> resultMap = jdbcTemplate.queryForMap("SELECT * FROM user WHERE id = ?", 1);
User user = new User().mapping(resultMap);

List<Map<String, Object>> resultList = jdbcTemplate.queryForList("SELECT * FROM user");
List<User> users = new User().mappingList(resultList);

```

```
/**
     * scroll pagation (just support colunm instance of Number)
     *
     * @param orderColName  排序列名
     * @param orderColValue 排序列值
     * @param direction     方向
     * @param params        参数
     * @param pageSize      每页数量
     * @return ScrollResult
     */
    public ScrollResult scroll(String orderColName, Number orderColValue, String direction, Map<String, Object> params, int pageSize)


    //滚动分页方法
    ScrollResult result = user.scroll("id", id, Model.Direction.DESC, null, 2);

```



##Contact
###QQ: 41369927
###E-Mail: 41369927@qq.com
###WeChat: jay41369927
