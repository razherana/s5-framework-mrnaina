# Framework

Fruits Framework is a Java 17 servlet-based MVC framework.
It provides:

- annotation-based controller routing
- automatic method argument injection
- request body parsing (JSON, form-url-encoded, multipart, text)
- response handling (`view`, `write`, `json`, `redirect`, `error`)
- middleware pipeline
- resolver-based dependency system for `Giver`/`Middleware` with Byte Buddy proxies
- authentication primitives (`AbstractAuthGiver`, `Authenticated`, roles)
- app config loading from `@AppConfig` classes + servlet context
- JSP utility bridge and JSP preprocessing for callable utility functions

## Quick code examples

### 1) Bootstrapping (`basePackage` is required)

```xml
<!-- web.xml -->
<web-app xmlns="https://jakarta.ee/xml/ns/jakartaee"
         version="6.0">
  <context-param>
    <param-name>basePackage</param-name>
    <param-value>com.example.app</param-value>
  </context-param>
</web-app>
```

### 2) Basic routing + parameter injection + view/write/json

```java
package com.example.app.controllers;

import java.util.Map;

import mg.razherana.framework.web.annotations.Controller;
import mg.razherana.framework.web.annotations.JsonUrl;
import mg.razherana.framework.web.annotations.Url;
import mg.razherana.framework.web.annotations.parameters.ParamVar;
import mg.razherana.framework.web.annotations.parameters.PathVar;
import mg.razherana.framework.web.annotations.parameters.PathVars;
import mg.razherana.framework.web.containers.RoutingContainer.HttpMethod;
import mg.razherana.framework.web.utils.ModelView;

@Controller(value = "/users", alias = "users")
public class UserController {

  @Url("/")
  public String list() {
    return "Users page"; // handled by write response handler
  }

  @Url("/[id:\\d+]")
  public String show(@PathVar("id") int id, @PathVars Map<String, String> allPathVars) {
    return "User id=" + id + ", pathVars=" + allPathVars;
  }

  @Url("/search")
  public Object search(@ParamVar("q") String q,
      ModelView mv) {
    mv.attribute("query", q);
    return mv.view("/WEB-INF/views/users/search.jsp");
  }

  @JsonUrl
  @Url("/ping")
  public Object ping() {
    return Map.of("ok", true);
  }
}
```

### 3) Body mapping (`@ParamBody`) for JSON/form payload

```java
package com.example.app.controllers;

import mg.razherana.framework.web.annotations.Controller;
import mg.razherana.framework.web.annotations.JsonUrl;
import mg.razherana.framework.web.annotations.Url;
import mg.razherana.framework.web.annotations.parameters.ParamBody;
import mg.razherana.framework.web.containers.RoutingContainer.HttpMethod;

@Controller("/products")
public class ProductController {

  public static class CreateProductRequest {
    public String name;
    public Double price;
  }

  @JsonUrl
  @Url(value = "/", method = HttpMethod.POST)
  public Object create(@ParamBody CreateProductRequest body) {
    return java.util.Map.of(
        "name", body.name,
        "price", body.price,
        "created", true);
  }
}
```

### 4) Redirect and flash messages (`ModelView`)

```java
@Url(value = "/save", method = HttpMethod.POST)
public Object save(ModelView mv) throws java.io.IOException {
  mv.flash("success", "Saved successfully");
  return mv.routeRelative("list"); // redirects to another route in same controller
}

// Then in list route:
@Url("/list")
public Object list(ModelView mv) {
  String successMessage = mv.attribute("success"); // read flash message
  // ...
}
```

### 5) File upload (`multipart/form-data`)

```java
import jakarta.servlet.http.Part;
import mg.razherana.framework.web.annotations.parameters.ParamVar;

@Url(value = "/avatar", method = HttpMethod.POST)
public Object uploadAvatar(@ParamVar("avatar") Part file, ModelView mv) throws Exception {
  mv.saveTo(file, mv.realPath("/uploads/avatar.png"));
  return mv.write("uploaded");
}
```

### 6) Middleware + route protection

```java
package com.example.app.middlewares;

import mg.razherana.framework.web.middlewares.Middleware;
import mg.razherana.framework.web.utils.proxies.annotations.Impl;

public abstract class RequestLogMiddleware extends Middleware {
  @Impl
  public Object before(
    jakarta.servlet.http.HttpServletRequest req
    // Parameter injection works the same as in controller urls
  ) {
    System.out.println("[REQ] " + req.getMethod() + " " + req.getRequestURI());
    return null; // continue
  }

  @Impl
  public Object after() {
    return null;
  }
}
```

```java
import mg.razherana.framework.web.middlewares.annotations.Middlewares;

@Controller("/admin")
@Middlewares({ com.example.app.middlewares.RequestLogMiddleware.class })
public class AdminController {
  @Url(value = "/dashboard", method = HttpMethod.GET)
  public String dashboard() {
    return "admin";
  }
}
```

### 7) Authentication (`AbstractAuthGiver` + `Authenticated` + roles)

```java
package com.example.app.auth;

import java.util.Set;

import jakarta.servlet.http.HttpServletRequest;
import mg.razherana.framework.security.auth.AbstractAuthGiver;
import mg.razherana.framework.security.auth.AuthUser;
import mg.razherana.framework.security.auth.AnonUser;
import mg.razherana.framework.web.utils.ModelView;
import mg.razherana.framework.web.utils.proxies.annotations.Impl;

public abstract class AppAuthGiver extends AbstractAuthGiver {

  @Override
  public void init(jakarta.servlet.http.HttpServletRequest request,
      jakarta.servlet.http.HttpServletResponse response,
      ModelView modelView) {
    // optional pre-init
  }

  @Impl(LOGIN)
  public void doLogin() {
    // custom login logic
  }

  @Impl(EXTRACT_USER)
  public AuthUser doExtractUser(HttpServletRequest req) {
    Object username = req.getSession(false) != null ? req.getSession(false).getAttribute("username") : null;
    if (username == null) {
      return new AnonUser();
    }

    return new AuthUser() {
      public String getUsername() { return username.toString(); }
      public String getPassword() { return ""; }
      public Set<String> getRoles() { return Set.of("ADMIN"); }
    };
  }

  @Impl(ON_ERROR)
  public Object doOnError(ModelView mv) throws java.io.IOException {
    return mv.redirectRelative("/login");
  }

  @Override
  public Object onRoleNotFoundError(String missingRole) {
    // You may call some other @Resolve method here if you want to perform some logic based on the missing role
    return "missing role: " + missingRole;
  }
}
```

```java
import mg.razherana.framework.security.auth.annotations.HasRole;
import mg.razherana.framework.security.auth.middlewares.Authenticated;
import mg.razherana.framework.web.middlewares.annotations.Middlewares;

@Url(value = "/secure", method = HttpMethod.GET)
@Middlewares({Authenticated.class})
@HasRole("ADMIN")
public String secureEndpoint() {
  return "ok";
}
```

### 8) Custom Giver (inject reusable service into controllers)

```java
package com.example.app.givers;

import mg.razherana.framework.web.givers.Giver;
import mg.razherana.framework.web.utils.proxies.annotations.Impl;
import mg.razherana.framework.web.utils.proxies.annotations.Resolve;

public abstract class TimeGiver implements Giver {
  @Resolve("now")
  public abstract long now();

  @Impl("now")
  public long provideNow() {
    return System.currentTimeMillis();
  }
}
```

```java
@Url(value = "/time", method = HttpMethod.GET)
public String time(TimeGiver tg) {
  return "now=" + tg.now();
}
```

### 9) App config (`@AppConfig` + `@Config`)

```java
package com.example.app.config;

import jakarta.servlet.ServletContext;
import mg.razherana.framework.configs.AppConfig;
import mg.razherana.framework.configs.Config;

@AppConfig
public class FrameworkConfig {
  // Override is true by default
  @Config("viewsDirectory")
  public String viewsDirectory() {
    return "/WEB-INF/views";
  }

  // `Override = false` means that if `responseHandlers` is already set in servlet context (via web.xml), that value will be used instead of this method's return value
  @Config(value = "responseHandlers", override = false)
  public String customHandlers(ServletContext ctx) {
    return "csv:com.example.app.responses.CsvResponseHandler";
  }
}
```

### 10) JSP views using default util functions (`$route`, `$attribute`)

```jsp
<!-- /WEB-INF/views/users/list.jsp -->
<%@ page contentType="text/html; charset=UTF-8" %>

<h1>Users</h1>

<!-- Read a request attribute set from controller -->
<p>Current filter: <%= $attribute("query") %></p>

<!-- Build route URL by controllerAlias/urlAlias + path params + query params -->
<a href='<%= $route("users/show", "id=12", "?tab=profile") %>'>
  Open user #12
</a>

<!-- Get all attributes as a map -->
<pre><%= $attribute() %></pre>
```

```java
// Matching controller aliases for $route example above
@Controller(value = "/users", alias = "users")
public class UserController {
  @Url(value = "/[id:\\d+]", alias = "show")
  public Object show(@PathVar("id") int id, ModelView mv) {
    mv.attribute("query", "active");
    return mv.view("/WEB-INF/views/users/list.jsp");
  }
}
```

### 11) JSP utility extension (`jspUtils`)

```java
import mg.razherana.framework.web.utils.jsp.JspUtil;

public class CurrencyUtil extends JspUtil {
  @Override
  public String getViewName() {
    return "$currency";
  }

  @Override
  public Object run(Object... args) {
    if (args.length == 0) {
      return "0.00";
    }
    return String.format("%,.2f", Double.parseDouble(args[0].toString()));
  }
}
```

```xml
<!-- web.xml context-param or servlet context value -->
<context-param>
  <param-name>jspUtils</param-name>
  <param-value>com.example.app.jsp.CurrencyUtil</param-value>
</context-param>
```

```jsp
<!-- inside JSP (preprocessed by framework) -->
<%= $currency(12345.678) %>
```

---

## 1) Runtime flow

`FrontServlet` is the entrypoint (`@WebServlet("/")`, `@MultipartConfig`).

At startup (`init()`), it:

1. creates `App`
2. reads servlet init param `basePackage`
3. scans and loads configs (`@AppConfig` + `@Config`)
4. scans controllers (`@Controller`, `@Url`)
5. scans resolvers (`Giver`, `MotherResolv` subclasses)
6. initializes response handlers (default + optional custom from context)
7. builds route/resolver maps (`WebFinder`, `WebMapper`)
8. initializes JSP utils and preprocesses JSP files

For each request:

- static resource requests are delegated to default servlet behavior
- non-static paths are routed through `WebMapper`
- matched route is executed by `WebExecutor`
- exceptions are transformed through `ErrorResponseHandler`

On servlet destroy, JSP backups are restored.

---

## 2) Routing model

### Controller and route annotations

- `@Controller(value="/base", alias="...")` on class
- `@Url(value="/path", alias="...", method={...})` on methods
- `@JsonUrl` on method forces JSON response handler

### HTTP methods

`RoutingContainer.HttpMethod` supports:

- `GET`, `POST`, `PUT`, `DELETE`, `OPTIONS`, `HEAD`, `ALL`

### Path parameter syntax

Route segments can contain path parameters:

- `[id]` → captures any segment into `id`
- `[id:\\d+]` → captures only matching regex

`WebMapper` requires same segment count and validates regex constraints.

---

## 3) Controller lifecycle

`@Controller` classes are:

- **singleton by default** (one instance held in controller container)
- `@Prototype` → new instance per request
- `@Stateful(timeout=...)` → instance stored in HTTP session and auto-reset when timeout elapsed

`@Stateful` session key:

- `MRNAINA_FRAMEWORK_STATEFUL_CONTROLLER_CONTAINER_KEY`

Default `@Stateful` timeout is `18_000_000 ms` (5 hours).

---

## 4) Method argument injection

`WebExecutor` uses `ArgResolver` + providers to inject parameters.

Supported argument patterns:

### Request/servlet objects

- `HttpServletRequest`
- `HttpServletResponse`
- `ServletContext`
- `HttpSession` (with `@CreateSession` controlling `getSession(boolean)`)
- `Method` (current controller method reflection)

### Framework objects

- `ModelView`
- `RequestBody`

### Route/query/body binding

- `@PathVar("name")` → single path variable (type-converted)
- `@PathVars` on `Map<String, String>`
- `@ParamVar(...)` from parsed request body map (required/default/forceString supported)
- `@ParamBody` supports:
  - `Map<String, Object>`
  - `JsonElement`
  - arbitrary POJO/record conversion via map-to-object converter

### Context binding

- `@ContextVar("name")` → `ServletContext` attribute injection (with conversion if String)

### Giver/middleware meta binding

- parameters assignable to `Giver`
- parameters assignable to `Annotation` (injects same-typed annotation from method parameter)

If no provider supports a parameter, framework throws `MalformedWebAnnotationException`.

---

## 5) Request body parsing

`RequestBody.from(request)` aggregates data from:

1. URL query string
2. request body (by content type)

Supported content types:

- `application/json`
- `application/x-www-form-urlencoded`
- `multipart/form-data`
- `text/plain`

Notes:

- multipart fields are read via `Part`; non-file text fields become strings
- JSON multipart fields are parsed into `JsonElement`
- urlencoded keys support nested dot/array syntax (e.g. `a.b[0].c=value`)
- parsed body is cached in request attribute

---

## 6) Return values and response handlers

Controller execution result is mapped as follows:

- method annotated `@JsonUrl` → JSON handler
- `String` return → write handler (`text` directly)
- `ResponseContainer` return → uses explicit type
- `JsonElement` return → JSON handler
- `ResponseBody` return → JSON handler
- `null` response container → no write performed

Default handlers registered by `App`:

- `view` → `JspViewResponseHandler` (`RequestDispatcher.forward`)
- `write` → `WriteResponseHandler`
- `json` → `JsonResponseHandler`
- `error` → `ErrorResponseHandler`
- `redirect` → `RedirectResponseHandler`

Custom handlers can be declared using servlet context/init param key `responseHandlers` with format:

`type:fully.qualified.ClassName,type2:fully.qualified.ClassName`

Class must implement `ResponseHandler` and have no-arg constructor.

---

## 7) `ModelView` capabilities

`ModelView` provides helpers for:

- request header read/write (`header(...)`)
- session get/set (`session(...)`)
- request attribute get/set (`attribute(...)`)
- return builders:
  - `view(String)`
  - `write(String)`
  - `json()` / `json(JsonElement)`
  - `redirectAbsolute(...)`
  - `redirectRelative(...)`
  - `routeRelative(...)`
  - `routeAbsolute(...)`
- file save from multipart `Part` (`saveTo(...)`)
- flash attributes (`flash(...)`) for next request

`ModelView.attribute(...)` also accumulates values into `ResponseBody` for JSON responses.

---

## 8) Middleware system

`Middleware` is an abstract resolver (`MotherResolv`) with:

- `@Resolve abstract Object before()`
- `@Resolve abstract Object after()`

`WebExecutor` pipeline:

1. instantiate middleware proxies
2. execute `before()` (iteration order is not guaranteed)
3. if any `before()` returns non-null, controller is short-circuited
4. otherwise invoke controller
5. execute `after()`; first non-null return overrides controller result

Global middleware list currently includes:

- `SessionFlashMiddleware`

Additional middleware can be attached via `@Middlewares(...)` on controller class or method.

### Built-in `SessionFlashMiddleware`

- key: `session_flash`
- `before()` copies flash entries from session to request attributes and clears session flash
- `after()` is no-op

---

## 9) Giver + resolver/proxy system

### Giver

`Giver` is a pre-controller injectable component with optional `init(request, response, modelView)`.

### Resolver mechanics

Framework scans resolv classes for `Giver` and `MotherResolv` descendants.
Resolver classes must:

- be abstract classes
- implement/extend origin type
- have no-arg constructor
- not be enum/record

Matching rules (`WebFinder`):

- `@Resolve` methods must be abstract and zero-arg
- `@Impl` methods must be concrete
- alias/name must pair `@Resolve` ↔ `@Impl`
- return type compatibility is validated

`MethodInterceptor` (Byte Buddy) creates runtime subclasses:

- calls to abstract `@Resolve` methods are intercepted
- interceptor locates paired `@Impl` method
- `@Impl` parameters are resolved through same argument resolver chain
- then invokes implementation method with injected dependencies

This mechanism powers injectable middlewares and givers with rich method signatures.

---

## 10) Authentication primitives

### `AbstractAuthGiver`

Defines abstract resolve points:

- `login()`
- `extractUser()`
- `onError()`
- `onRoleNotFoundError(String missingRole)`

`hasRole(String)` helper checks extracted user roles.

### `Authenticated` middleware

Built-in abstract middleware implementation:

- `before(AbstractAuthGiver, Roles)`:
  - rejects anonymous/absent user via `onError()`
  - validates all required roles (`@HasRole` grouped in `@Roles`)
  - role failure delegates to `onRoleNotFoundError(...)`
- `after()` returns `null`

`AnonUser` is provided as anonymous `AuthUser` implementation.

---

## 11) JSP utility bridge and preprocessing

### JspUtil model

- Extend `JspUtil`
- implement `getViewName()` and `run(Object... args)`
- no-arg constructor required

`JspFunctionBridge` registers utils and exposes runtime function invocation.

### Default JSP utils

- `$route` (`RouteUtil`): build route URL from `controllerAlias/urlAlias` + params/query
- `$attribute` (`AttributeUtil`): read request attributes

### JSP preprocessing

`ManualJSPPreprocessor` scans `.jsp` files in configured views directory and rewrites function calls into bridge invocation expressions.

- creates `.backup` before first rewrite
- supports restoring backups via `restoreBackups(...)`
- avoids replacing inside JSP comments/directives and handles scriptlet/EL segments

Default views directory if not set: `/WEB-INF/views`

---

## 12) App configuration system

### Annotations

- `@AppConfig` on class
- `@Config(value="name", override=true|false)` on methods

### Loading behavior

`App.scanConfigs(...)`:

1. imports servlet init params into servlet context attributes
2. scans `@AppConfig` classes
3. executes `@Config` methods
4. resolves value precedence with `override` flag:
   - `override=true`: method value preferred over context value
   - `override=false`: existing context value preferred
5. stores resolved value back into servlet context attributes

`@Config` methods may receive `ServletContext` as parameter (resolved by config arg resolver).

---

## 13) Error handling

- `HttpException` hierarchy carries HTTP status code
  - `BadRequestException` (400)
  - `NotFoundException` (404)
  - `UnauthorizedException` (401)
- `ErrorResponseHandler`:
  - if root cause is `HttpException` → `response.sendError(status, message)`
  - else returns rich HTML debug page (exception info + stack trace + request info)

---

## 14) Servlet init/context keys used by framework

Defined in `App.InitKey`:

- `basePackage` (**required**)
- `responseHandlers`
- `viewsDirectory`
- `jspUtils`

`basePackage` must be present, otherwise servlet init fails.

---

## 15) Build info

- Java: 17
- Maven packaging: `jar`
- Dependencies:
  - `jakarta.servlet-api:6.0.0` (provided)
  - `net.bytebuddy:byte-buddy` (runtime proxy generation)
  - `junit:3.8.1` (tests)

---
