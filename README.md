# Anima

<p align="center">
    <a href="https://github.com/biezhi/anima/wiki"><img src="screenshot/cover.png" width="623"/></a>
</p>

`Anima` allows you to query database like `SQL` and `Stream`.
a simple DSL syntax, supports multiple databases, integrates well with Java8, 
supports multiple relational mappings, and is a database manipulation tool.

**[Document](https://github.com/biezhi/anima/wiki)**

[![Travis Build](https://travis-ci.org/hellokaton/anima.svg?branch=master)](https://travis-ci.org/hellokaton/anima)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/3abf98fb260340cea9808d169cc47d8b)](https://www.codacy.com/app/hellokaton/anima?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=hellokaton/anima&amp;utm_campaign=Badge_Grade)
[![](https://img.shields.io/maven-central/v/com.hellokaton/anima.svg)](https://mvnrepository.com/artifact/com.hellokaton/anima)
[![codecov](https://codecov.io/gh/hellokaton/anima/branch/master/graph/badge.svg)](https://codecov.io/gh/hellokaton/anima) 
[![License](https://img.shields.io/badge/license-Apache2-blue.svg)](https://github.com/biezhi/anima/blob/master/LICENSE)
[![Twitter URL](https://img.shields.io/twitter/url/https/twitter.com/hellokaton.svg?style=social&label=Follow%20Twitter)](https://twitter.com/hellokaton)

## Feature

- Simple DSL 
- H2、MySQL、SQLite、PostgreSQL、Oracle、SqlServer 
- Paging support 
- Flexible configuration 
- Connection pool support
- Support `LocalDate`、`LocalDateTime`
- Support lambda expression
- Relationship (`hasOne`、`hasMany`、`belongsTo`)
- SQL performance statistics
- Based Java8

# Usage

**Latest snapshot version**

> If you want to prioritize new features or some BUG fixes you can use it, you need to specify the snapshot repository in `pom.xml`

```xml
<repository>
    <id>snapshots-repo</id>
    <url>https://oss.sonatype.org/content/repositories/snapshots</url>
    <releases>
        <enabled>false</enabled>
    </releases>
    <snapshots>
        <enabled>true</enabled>
    </snapshots>
</repository>

<dependency>
    <groupId>com.hellokaton</groupId>
    <artifactId>anima</artifactId>
    <version>0.3.1</version>
</dependency>
```

Here's the `RELEASE` version.

**As Gradle**

```java
compile 'com.hellokaton:anima:0.3.1'
```

**As Maven**

```xml
<dependency>
    <groupId>com.hellokaton</groupId>
    <artifactId>anima</artifactId>
    <version>0.3.1</version>
</dependency>
```

> 📒 Although `Anima` can also be used by adding a jar package, we do not recommend doing this.

## Examples

**Open Connection**

```java
// MySQL
Anima.open("jdbc:mysql://127.0.0.1:3306/demo", "root", "123456");

// SQLite
Anima.open("jdbc:sqlite:./demo.db");

// H2
Anima.open("jdbc:h2:file:~/demo;FILE_LOCK=FS;PAGE_SIZE=1024;CACHE_SIZE=8192", "sa", "");

// DataSource
DruidDataSource dataSource = new DruidDataSource();
dataSource.setDriverClassName("com.mysql.jdbc.Driver");
dataSource.setUrl(blade.environment().getOrNull("jdbc.url"));
dataSource.setUsername(blade.environment().getOrNull("jdbc.username"));
dataSource.setPassword(blade.environment().getOrNull("jdbc.password"));
Anima.open(dataSource);
```

> 📕 This operation only needs one time

```java
public class User extends Model {
    
    private Integer id;
    private String  userName;
    private Integer age;
    
    public User() {
    }
    
    public User(String userName, Integer age) {
        this.userName = userName;
        this.age = age;
    }
    
}
```

Table Structure

```sql
CREATE TABLE `users` (
  `id` IDENTITY PRIMARY KEY,
  `user_name` varchar(50) NOT NULL,
  `age` int(11)
)
```

### Query

```java
long count = select().from(User.class).count();
// SELECT COUNT(*) FROM users

long count = select().from(User.class).where("age > ?", 15).isNotNull("user_name").count();
// SELECT COUNT(*) FROM users WHERE age > ? AND user_name IS NOT NULL

User user = select().from(User.class).byId(2);
// SELECT * FROM users WHERE id = ?

List<User> users = select().from(User.class).byIds(1, 2, 3);
// SELECT * FROM users WHERE id IN (?, ?, ?)

String name = select().bySQL(String.class, "select user_name from users limit 1").one();

List<String> names = select().bySQL(String.class, "select user_name from users limit ?", 3);

List<User> users = select().from(User.class).all();
// SELECT * FROM users

List<User> users = select().from(User.class).like("user_name", "%o%").all();
// SELECT * FROM users WHERE user_name LIKE ?
```

**Limit**

```java
List<User> users = select().from(User.class).order("id desc").limit(5);
// SELECT * FROM users ORDER BY id desc
```

**Paging**

```java
Page<User> userPage = select().from(User.class).order("id desc").page(1, 3);
// SELECT * FROM users ORDER BY id desc LIMIT ?, ?
```

**Map**

```java
select().from(User.class).map(User::getUserName).limit(3).collect(Collectors.toList());
```

**Filter**

```java
select().from(User.class).filter(u -> u.getAge() > 10).collect(Collectors.toList());
```

**Lambda**

```java
User user = select().from(User.class).where(User::getUserName).eq("jack").one();
// SELECT * FROM users WHERE user_name = ?
```

```java
List<User> user = select().from(User.class)
                .where(User::getUserName).notNull()
                .and(User::getAge).gt(10)
                .all();
// SELECT * FROM users WHERE user_name IS NOT NULL AND age > ?
```

```java
select().from(User.class).order(User::getId, OrderBy.DESC).order(User::getAge, OrderBy.ASC).all();
// SELECT * FROM users ORDER BY  id DESC, age ASC
```

**Join**

```java
@Table(name = "order_info")
@Data
public class OrderInfo extends Model {

    private Long id;

    private Integer uid;

    @Column(name = "productname")
    private String productName;

    private LocalDateTime createTime;

    @Ignore
    private User user;
    
    @Ignore
    private Address address;

}
```

```java
// HasOne
OrderInfo orderInfo = select().from(OrderInfo.class)
        .join(
            Joins.with(Address.class).as(OrderInfo::getAddress)
                 .on(OrderInfo::getId, Address::getOrderId)
        ).byId(3);

orderInfo = select().from(OrderInfo.class)
        .join(
            Joins.with(Address.class).as(OrderInfo::getAddress)
                 .on(OrderInfo::getId, Address::getOrderId)
        )
        .join(
                Joins.with(User.class).as(OrderInfo::getUser)
                        .on(OrderInfo::getUid, User::getId)
        ).byId(3);

// ManyToOne
orderInfo = select().from(OrderInfo.class)
        .join(
            Joins.with(User.class).as(OrderInfo::getUser)
                 .on(OrderInfo::getUid, User::getId)
        ).byId(3);

// OneToMany
UserDto userDto = select().from(UserDto.class).join(
            Joins.with(OrderInfo.class).as(UserDto::getOrders)
                 .on(UserDto::getId, OrderInfo::getUid)
        ).byId(1);
```

### Insert

```java
Integer id = new User("biezhi", 100).save().asInt();
// INSERT INTO users(id,user_name,age) VALUES (?,?,?)
```

or

```java
Anima.save(new User("jack", 100));
```

**Batch Save**

```java
List<User> users = new ArrayList<>();
users.add(new User("user1", 10));
users.add(new User("user2", 11));
users.add(new User("user3", 12));
Anima.saveBatch(users);
```

> 📘 This operation will begin a transaction and rollback when there is a transaction that is unsuccessful.

### Update

```java
int result  = update().from(User.class).set("user_name", newName).where("id", 1).execute();
// UPDATE users SET username = ? WHERE id = ?
```

or

```java
int result = update().from(User.class).set("user_name", newName).where("id", 1).execute();
// UPDATE users SET user_name = ? WHERE id = ?
```

or

```java
User user = new User();
user.setId(1);
user.setUserName("jack");
user.update();
// UPDATE users SET user_name = ? WHERE id = ?
```

```java
update().from(User.class).set(User::getUserName, "base64").updateById(2);
```

```java
update().from(User.class).set(User::getUserName, "base64").where(User::getId).eq(2).execute();
```

### Delete

```java
int result = delete().from(User.class).where("id", 1).execute();
// DELETE FROM users WHERE id = ?
```

or

```java
User user = new User();
user.setAge(15);
user.setUserName("jack");
user.delete();
// DELETE FROM users WHERE user_name = ? and age = ?
```

```java
delete().from(User.class).where(User::getId).deleteById(3);
delete().from(User.class).where(User::getId).eq(1).execute();
delete().from(User.class).where(User::getAge).lte(20).execute();
```

### Transaction

```java
Anima.atomic(() -> {
    int a = 1 / 0;
    new User("apple", 666).save();
}).catchException(e -> Assert.assertEquals(ArithmeticException.class, e.getClass()));
```

> 📗 `Anima` uses the `atomic` method to complete a transaction. normally, the code will not throw an exception. 
> when a `RuntimeException` is caught, the transaction will be `rollback`.

## Test Code

See [here](https://github.com/biezhi/anima/tree/master/src/test/java/io/github/biezhi/anima)

## License

[Apache2](https://github.com/hellokaton/anima/blob/dev/LICENSE)
