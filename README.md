# Anima

`Anima` is an Java `ActiveRecord` library implement by modify bytecodes.
`Anima` allows you to query databases like `Stream`.

[![Travis Build](https://travis-ci.org/biezhi/anima.svg?branch=master)](https://travis-ci.org/biezhi/anima) 
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

## Example

Open DB

```java
Anima.open("jdbc:mysql://127.0.0.1:3306/demo", "root", "123456");
```

> ⚠️ This operation only needs one time

**READ**

```java
// query count
User.count();

// query by id
User.findById(9);

// query all
User.all()

// query one
User.orderBy("id desc").one();

// query by condition
User.where("age > ?", 15).notNull("name").all();

```

**Delete**

```java
// delete by id
User.deleteById(9);

// delete all
User.deleteAll();

// delete by condition
User.where("id > ?", 20).delete();
```

**Insert**

```java
User user = new User("jack", 20);
user.save();
```

**Update**

```java
User.set("name", "rose").where("id = ?", 9).update();

User u = new User(9);
u.setName("rose");
u.update();
```

## Test Code

See [here](https://github.com/biezhi/anima/tree/master/src/test/java/io/github/biezhi/anima)

## License

Apache2
