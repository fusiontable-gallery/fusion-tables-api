# Fusion Tables API demo using OAuth

This example demonstrates how to use the Fusion Tables API from a foreign web
server with delegated authorization. It is a simple application demonstrating
two possible access patterns: First, a user can see a listing of their own
Fusion Tables on a third party site. Second, the web site can retain the access
privilege and show the snippet of registered tables to anyone who receives the
snippet URL.

## Installation

This example uses Maven for ease of development and versatility. Obtain Maven
from http://maven.apache.org/download.html, or your package manager, say, on
Debian based Linux distributions
<pre>apt-get install maven2
</pre>
or on Mac OS with developer tools
<pre>port install maven2
</pre>

IDEs like Eclipse, Intellij, and Netbeans support Maven natively or through
extensions. Initially, Maven will download numerous dependencies to your local
repository.

-   To create an Intellij project: "Open Project" and navigate to the pom.xml
    file.
-   To create an Eclipse project
    <pre>mvn eclipse:eclipse
    </pre>
    Eclipse requires an extra step. Configure a classpath variable M2_REPO to the folder
    .m2/repository under your home directory.
-   To run the example
    <pre>mvn jetty:run
    </pre>
    Then point your browser at [http://localhost:8080/snippet](http://localhost:8080/snippet).
