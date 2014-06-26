# Tech Tip #1:  Getting Started with Spring Boot 

Spring Boot is a convention-over-configuration [centric approach to application development with Spring](http://spring.io/projects/spring-boot).  

There are a few ways to get started. 

In principal, the easiest way to get started is to just reuse somebody else's handcrafted build file and project setup. There are code-generators that make this easy. The Node-ecosystem tool `yo` offers a  code-generator  [ called `generator-jhipster`](http://jhipster.github.io/). The projects generated with JHipster are web applications built using Maven, Spring, and Angular.js. The Groovy-language ecosystem codegenerator [Lazy Bones](https://github.com/pledbrook/lazybones) can code-generate Spring Boot applications for you, as well. 

For me, nothing beats the [Spring Initializr](http://start.spring.io). It's simply a small form that's pre-filled out with useful values. You might make sure to specify Java 1.8, and check the boxes for `Web`, `JPA` (or any of the other supported data-access technologies like MongoDB), and `Actuator`. This is a safe first-application. Once you've specified your Java revision (you *are* on Java 1.8, aren't you?), specify the type of project you'd like. Many people will know what to do with the default, a `Maven Project`. This will be importable into any IDE, straight-Eclipse, IntelliJ (Community or Ultimate), NetBeans, etc. I'd leave the version ( - the latest stable release) and the project type  set to the defaults. Click `Generate` to download a an archive. Unzip it and then import it into your favorite IDE as a Maven project. 

If you're using  our open-source Eclipse distribution [Spring Tool Suite (STS)](https://spring.io/tools/sts), there are many ways to get started. We base STS on the latest-and-greatest cut of the Eclipse Java EE distribution, so it represents a stable, well-integrated distibution of Eclipse.  The Spring Tool Suite provides extra tools and niceties. One such nicety is  a dialog within the IDE that acts as a  front for the Spring Initializr.  It's nice skipping the download, unzip, and import steps! 

The [spring.io guides](http://spring,io/guides) provide easy-to-digest introductions to using Spring (or Spring ecosystem technologies). Each guide is backed by a Github repository that demonstrates the finished project as well as provides a base template that you can fill out when completing the guide. There's a nice feature in STS that lets you import a *Getting Started* guide directly from the IDE, by going to `File` -> `New` -> `Import Spring Getting Started Content`. This saves you the `git clone`, and IDE-import. 
 

I've [put together a video that shows what it looks like to use the tools in  Spring Tool Suite (STS)](//www.youtube.com/embed/p8AdyMlpmPk),

<iframe width="560" height="315" src="//www.youtube.com/embed/p8AdyMlpmPk" frameborder="0" allowfullscreen></iframe> 

