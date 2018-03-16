# Anima

`Anima` allows you to query databases like `SQL` and `Stream`.

[![Travis Build](https://travis-ci.org/biezhi/anima.svg?branch=master)](https://travis-ci.org/biezhi/anima)
[![](https://img.shields.io/maven-central/v/io.github.biezhi/anima.svg)](https://mvnrepository.com/artifact/io.github.biezhi/anima) 
[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg)](http://makeapullrequest.com) 
[![License](https://img.shields.io/badge/license-Apache2-blue.svg)](https://github.com/biezhi/anima/blob/master/LICENSE)
[![@biezhi on zhihu](https://img.shields.io/badge/zhihu-%40biezhi-red.svg)](https://www.zhihu.com/people/biezhi)
[![Twitter URL](https://img.shields.io/twitter/url/https/twitter.com/biezhii.svg?style=social&label=Follow%20Twitter)](https://twitter.com/biezhii)
[![](https://img.shields.io/github/followers/biezhi.svg?style=social&label=Follow%20Github)](https://github.com/biezhi)

## Feature

- Simple DSL 
- Multiple database support 
- Paging support 
- Flexible configuration 
- Connection pool support 
- SQL performance statistics

# Usage

**As Gradle**

```java
compile 'io.github.biezhi:anima:0.0.1'
```

**As Maven**

```xml
<dependency>
    <groupId>io.github.biezhi</groupId>
    <artifactId>anima</artifactId>
    <version>0.0.1</version>
</dependency>
```

> Although `Anima` can also be used by adding a jar package, we do not recommend doing this.

## Examples

**Open Connection**

```java
Anima.open("jdbc:mysql://127.0.0.1:3306/demo", "root", "123456");
```

> ⚠️ This operation only needs one time

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

### Query

```java
long count = select().from(User.class).count();
// SELECT COUNT(*) FROM users

long count = select().from(User.class).where("age > ?", 15).isNotNull("user_name").count();
// SELECT COUNT(*) FROM users WHERE age > ? AND user_name IS NOT NULL

User user = select().from(User.class).findById(2);
// SELECT * FROM users WHERE id = ?

List<User> users = select().from(User.class).findByIds(1, 2, 3);
// SELECT * FROM users WHERE id IN (?, ?, ?)

String name = select().findBySQL(String.class, "select user_name from users limit 1");
// select user_name from users limit 1

List<String> names = select().findAllBySQL(String.class, "select user_name from users limit ?", 3);
// select user_name from users limit 3

List<User> users = select().from(User.class).all();
// SELECT * FROM users

List<User> users = select().from(User.class).like("user_name", "%o%").all();
// SELECT * FROM users WHERE user_name LIKE '%o%'
```

**limit**

```java
List<User> users = select().from(User.class).order("id desc").limit(5);
// SELECT * FROM users ORDER BY id desc LIMIT 0, 5   

List<User> users = select().from(User.class).order("id desc").limit(2, 3);
// SELECT * FROM users ORDER BY id desc LIMIT 2, 3
```

**paging**

```java
Page<User> userPage = select().from(User.class).order("id desc").page(1, 3);
// SELECT * FROM users ORDER BY id desc LIMIT ?, ?
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

### Transaction

```java
Anima.atomic(() -> {
    int a = 1 / 0;
    new User("apple", 666).save();
}).catchException(e -> Assert.assertEquals(ArithmeticException.class, e.getClass()));
```

> `Anima` uses the `atomic` method to complete a transaction. normally, the code will not throw an exception. 
> when a `RuntimeException` is caught, the transaction will be `rollback`.

## Test Code

See [here](https://github.com/biezhi/anima/tree/master/src/test/java/io/github/biezhi/anima)

## License

Apache2
