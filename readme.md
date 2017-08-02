# Web application with Spring

In the last tutorial we built a simple forum application using the java servlet api.
The servlet api provided us with the `HttpServlet`, `HttpServletRequest` and `HttpServletResponse` classes.
It was enough to build a basic webapp, but there were some downsides:

* we had to write a lot of html in java strings (yuck!)
* writing data on disk took a lot of manual work
* getting the Request-URI from the request object was unintuitive
* sharing objects between servlets was not trivial
* the application was started using maven goals (`jetty:run`)

In this tutorial we're not going to add much new functionality.
Instead we're going to ditch the servlet api and move to a more complex and powerful api - [the Spring Framework](http://projects.spring.io/spring-boot/).

## Migrating the existing code

We'll be using the code written in the last practice session.
Follow these steps to migrate the code to *Spring*:

1. clone this repository
2. copy the files from `src/main/java` and `src/main/resources` to this repository
3. copy the files from `src/main/webapp` to `src/main/resources/public` in this repository
4. make sure not to overwrite the pom.xml - it has changed considerably
5. move the servlet classes to the `app.controllers` package (optional)

### Spring controllers

In the servlet api, the class that was handing the requests was called a *servlet*.
In Spring, the classes are called *controllers*.

To make your servlets Spring friendly, follow these steps:
* remove the `@WebServlet` annotation
* remove the `extends HttpServlet` declaration
* remove the `@Override` from `doGet` and `doPost`
* replace `throws ServletException, IOException` with `throws Exception` on `doGet` and `doPost` (and remove any unwanted catch blocks)
* add `@Controller` to the class
* add `@RequestMapping` to both `doGet` and `doPost`.
  set the RequestMapping value to whatever was in the `@WebServlet` annotation.
  set the RequestMapping method to either `RequestMethod.GET` or `RequestMethod.POST`.
  see the `SampleController` class for examples.

Note that the `doGet` and `doPost` methods no longer implement any interface methods.
When the server is starting up, Spring will look for any classes with the `@Controller` annotation.
From these classes, Spring will look for any methods with the `@RequestMapping` annotations (the methods are called *request handlers*).
When the server receives a request, Spring will go through all the methods it found earlier:

* if the Request-URI matches any request handler, then Spring will call that method
* if no methods match, then Spring will try to find a file with a matching name (it will search the /public folder in the classpath).

Since Spring only looks at the `@RequestMapping`, you can use any method name you want.
Indeed, you should rename `doGet` to something more descriptive, such as `listThreads`/`listPostsInThread`.
Finally, the request handlers can return different values and take different parameters - see the [RequestMapping docs](https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/web/bind/annotation/RequestMapping.html).

## Running the server

You will no longer need to use `jetty:run`.
Find the class `Application` and start the main method.
The server will start at the usual [localhost:8080](http://localhost:8080/) address.
You can debug the server like any regular java application.

Migrate your code and make sure the application is still working.

## Use path variables

Our thread viewing servlet had a mapping "/thread/threadName", where threadName is a variable.
To get the thread name, we had to use the `getRequestURI` method and do some string magic on it.
This is rather clumsy and Spring agrees.

You should have a request handler with `@RequestMapping(value = "/threads/*")`.
Replace it with `@RequestMapping(value = "/threads/{threadName}")`.
The curly braces indicate to Spring that this is a variable.
Next, add a new parameter to the method: `@PathVariable String threadName` (the parameter itself is annotated).
As long as the parameter name matches the variable in the RequestMapping, Spring can match them and automatically provide the parameter value.
See the [Spring docs](https://docs.spring.io/spring/docs/current/spring-framework-reference/html/mvc.html#mvc-ann-requestmapping-uri-templates) if needed.

Change your code to use `@PathVariable` where appropriate.

## Use `redirect:`

The *Location* header with the 3xx status code can be used to redirect the browser to another page.
Setting the header and status manually is a lot of work.
In Spring, you can redirect more easily:

```java
@RequestMapping("/this/page")
public String redirected(HttpServletRequest req) {
  return "redirect:/other/page";
}
```

Update your controllers to use Spring redirect.

## Use templates instead of html strings

Currently you likely use code like this:

```java
PrintWriter pw = resp.getWriter();
pw.print("<!DOCTYPE html><html><head></head><body><h1>Threads</h1><ul>");
for (String thread : threads) {
  pw.print("<li>" + thread + "</li>");
}
pw.print("</ul></body></html>");
```

This is not very fun or pretty and Spring agrees.
Instead of writing html snippets into the controller class, most real web applications use html templates.
A template is a html file with placeholders.
We will use the [*Thymeleaf* template engine](http://www.thymeleaf.org/) to get rid of the html in our controllers.

This is how it works.
First you create a html template for the page - an almost regular html file:

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head></head>
<body>
  <h1>Threads</h1>
  <ul>
    <block th:each="thread : ${threads}">
      <li th:text="${thread}"></li>
    </block>
  </ul>
</body>
</html>
```

The weird attributes `th:text` and `th:each` are thymeleaf's template magic.
The `<block>` element is not a standard html element - it's a special thymeleaf element that tells thymeleaf to repeat the contents of the `<block>` for each element of some collection.
The collection itself is specified by the `th:each` attribute.
The `th:text` attribute tells thymeleaf to replace the entire contents of the element with the specified text.

Where does thymeleaf get the threads collection?
You must provide it in the request handler:

```java
@RequestMapping
public ModelAndView doGet(HttpServletRequest req, HttpServletResponse resp) {
  List<String> threads = loadThreads();
  return new ModelAndView("templateName").addObject("threads", threads);
}
```

The above code does some new things.
Some notes:

* we never touch the response object - writing the response is left for thymeleaf.
  you can remove the response parameter entirely, or keep it if you need to add any headers or like.
* we return a `ModelAndView` object. this specifies the template's name ("templateName" in the example) and the values that should be available in the template.
* thymeleaf will search for the template from the `/templates` directory in the classpath.
  in this example, it would use the file `src/main/resources/templates/templateName.html` if it existed.
* you can add any objects to the `ModelAndView`.
  you can call `addObject` multiple times.
  you can add strings, numbers and objects of your own classes to the `ModelAndView` (and use them in the template: `${myObject.myField}`).

Thymeleaf syntax allows you to do almost everything.
This also makes it quite complicated.
See the [thymeleaf docs](http://www.thymeleaf.org/doc/tutorials/3.0/usingthymeleaf.html) for details.

This repository already contains some thymeleaf configuration:

* the pom.xml contains the thymeleaf dependency
* the application.properties contains `spring.thymeleaf.cache=false`, which will allow you to reload thymeleaf templates without restarting the application (use "Build project").

Update your application to use thymeleaf:

* create the templates directory
* move all html from the controllers to separate files in the templates directory
* change the controllers to return ModelAndView
* change your html to use thymeleaf's features.
  see `th:href` for links and css, `th:action` for forms, `th:text` for text.
  use the `@{}` syntax for URLs and `${}` syntax for variables.
* make sure the application is still working like before

## Note about html escaping

Your application allows users to post arbitary text to the forum threads.
What if someone posts html to the forum?
Try to post the following text to your forum `<h1>surprise!</h1>`.
Is it rendered as html or as-written?
See the page sources in the developer tools.
Thymeleaf should automatically replace all `<` and `>` symbols with their html-encoded variants: `&gt;` and `&lt;`.
This is super important from security point of view: if random people can add html to your page, then they can also add javascript code and bad things will happen.
If you really need to add html to the template, then use `th:utext`, but be very careful!

## Note about ModelAndView

What does Model and View mean?
It's rather common to split web applications into three components:

* models are classes that represents your data (forum threads, user account, orders, bills, etc)
* views are the html templates that render some part of the model to the browser
* controllers are the classes that accept requests, change the model objects and choose the right view

The critical part here is that the model classes should know nothing about the controllers and views.
Similarly, the views should know nothing about the controller logic, nor should they ever change a model object.
Views only render the objects that are passed to them.
Finally, controllers know about how to change the model objects, but not how to render them to the browser.

Such a split is called the MVC (Model-View-Controller) pattern and its purpose is to make the code more maintainable.
Note that MVC is just one (and not always the best) option for organizing your application.
