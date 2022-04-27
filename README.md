# Kala Template

[![](https://img.shields.io/maven-central/v/org.glavo.kala/kala-template?label=Maven%20Central)](https://search.maven.org/artifact/org.glavo.kala/kala-template)

This is a lightweight (about 1KB), streaming templating engine.

If you're tired of overly bloated template engines, then it's probably what you want.

## Why I choose it?

* It's very lightweight: it's only a jar of about 1KB, and it doesn't have any dependencies;
* It is compiled with Java 8, but also provides `module-info.class`, so it has full support for JPMS;
* It's streaming and doesn't need to read the entire file into memory, so it can handle very large files with ease;
* It is easy to expand and can meet most needs on its basis;

## Adding it to your build

It's already published on Maven Central, you can add it to the build like this:

Maven:
```xml
<dependency>
  <groupId>org.glavo.kala</groupId>
  <artifactId>kala-template</artifactId>
  <version>0.1.0</version>
</dependency>
```

Gradle:
```groovy
implementation("org.glavo.kala:kala-template:0.1.0")
```

## Getting Started

Here is a simple example:

```java
TemplateEngine engine = TemplateEngine.getDefault();

engine.process("Hello ${name}!", Map.of("name", "Glavo")); // --> "Hello Glavo!"
```

`${name}` is replaced by `Glavo`. `${...}` is the default template tag, you can easily customize it:

```java
TemplateEngine engine = TemplateEngine.builder()
        .tag("{%", "%}")
        .build();

engine.process("Hello {%name%}!", Map.of("name", "Glavo")); // --> "Hello Glavo!"
```

---

The above examples all pass a `Map` for the template engine, but you can also pass a `ResourceBundle`:

`simple.properties`:
```properties
user.name=Glavo
user.site=https://glavo.site
```

Java Code:
```java
ResourceBundle bundle = ResourceBundle.getBundle("simple");

TemplateEngine.getDefault().process("Hello ${user.name}! Welcome to ${user.site}", bundle); // --> "Hello Glavo! Welcome to https://glavo.site"
```

And, a more flexible approach is to pass a `Function`:

```java
TemplateEngine.getDefault().process("Welcome to ${Glavo}!", it -> it + "'s site"); // --> "Welcome to Glavo's site!"
```

This gives you extreme flexibility, you can use it to achieve any function you want.

---

Of course, we can do more than just use `String` as input and output as above.
`TemplateEngine` accepts `Reader` as input and `Appendable` as output, so it can accept more input and output forms:

```java
try (Reader reader = Files.newBufferedReader(Paths.get("MyTemplate.xml.template"));
     Writer writer = Files.newBufferedWriter(Paths.get("MyTemplate.xml"))) {
        TemplateEngine.getDefault().process(reader, writer, Map.of(...));
}
```

`TemplateEngine` will read and process the template file streaming, without needing to read the entire file in at once,
and the generated content is also written directly to the `writer`.
Therefore, it can handle large template files efficiently.

To simplify writing input from a template file and output to another file,
`TemplateEngine` provides a builtin overload that allows you to achieve the same functionality as above:

```java
TemplateEngine.getDefault().process(Paths.get("MyTemplate.xml.template"), Paths.get("MyTemplate.xml"), Map.of(...));
```

---

By default `TemplateEngine` throws an exception when an unknown marker is encountered:

```java
try {
    TemplateEngine.getDefault().process("Hello ${someone}!", Map.of("name", "Glavo")); // Failed
} catch (TemplateProcessException e) {
    System.out.println("Failed 😭");
}
```

However, you can also modify the default behavior:

```java
TemplateEngine engine = TemplateEngine.builder()
        .errorMode(TemplateEngine.ErrorMode.SANITIZE)
        .build();

engine.process("Hello ${someone}!", Map.of("name", "Glavo")); // --> "Hello ${someone}!"
```

Or:

```java
TemplateEngine engine = TemplateEngine.builder()
        .errorMode(TemplateEngine.ErrorMode.STRIP)
        .build();

engine.process("Hello ${someone}!", Map.of("name", "Glavo")); // --> "Hello !"
```

## Donate

If you like this library, donating to me is my greatest support!

Due to payment method restrictions, donations are currently only supported through payment channels in Chinese mainland (微信，支付宝，爱发电等).

Here are the ways to donate: [捐赠支持 Glavo](https://donate.glavo.site/)